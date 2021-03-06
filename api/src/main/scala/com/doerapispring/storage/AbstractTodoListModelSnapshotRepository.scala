package com.doerapispring.storage

import java.sql.{PreparedStatement, ResultSet}
import java.util.Date

import com.doerapispring.domain._
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.{JdbcTemplate, RowMapper}

abstract class AbstractTodoListModelSnapshotRepository[T](private val clazz: Class[T],
                                                          private val jdbcTemplate: JdbcTemplate,
                                                          private val objectMapper: ObjectMapper)
  extends OwnedObjectWriteRepository[Snapshot[T], UserId, ListId]
    with OwnedObjectReadRepository[Snapshot[T], UserId, ListId] {

  override def saveAll(userId: UserId, listId: ListId, snapshots: List[Snapshot[T]]): Unit = {
    snapshots.foreach(snapshot => this.save(userId, listId, snapshot))
  }

  override def save(userId: UserId, listId: ListId, snapshot: Snapshot[T]): Unit = {
    jdbcTemplate.update(
      "INSERT INTO todo_lists (user_id, list_id, data, created_at) " +
        "VALUES (?, ?, ?, ?) " +
        "ON CONFLICT (user_id, list_id) DO UPDATE " +
        "SET data = excluded.data, created_at = excluded.created_at",
      (ps: PreparedStatement) => {
        ps.setString(1, userId.get())
        ps.setString(2, listId.get())
        ps.setString(3, objectMapper.writeValueAsString(snapshot.model))
        ps.setTimestamp(4, java.sql.Timestamp.from(snapshot.createdAt.toInstant))
      })
  }

  override def find(userId: UserId, listId: ListId): Option[Snapshot[T]] = {
    val rowMapper: RowMapper[Snapshot[T]] = (rs: ResultSet, _: Int) => {
      Snapshot(
        objectMapper.readValue(rs.getString("data"), clazz),
        Date.from(rs.getTimestamp("created_at").toInstant))
    }

    jdbcTemplate.query(
      "SELECT data, created_at FROM todo_lists WHERE user_id = ? AND list_id = ?",
      rowMapper,
      userId.get(),
      listId.get()
    )
      .stream()
      .findFirst()
      .map(snapshot => Option.apply(snapshot))
      .orElse(None)
  }
}
