package com.doerapispring.storage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class TodoListDaoTest {
    @Autowired
    private TodoListDao todoListDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String listId;

    @Before
    public void setUp() throws Exception {
        listId = "someUuid";
        jdbcTemplate.update("INSERT INTO " +
            "users (email, password_digest, default_list_id, created_at, updated_at) " +
            "VALUES ('someEmail', 'somePasswordDigest', '" + listId + "', now(), now())");
        jdbcTemplate.update("INSERT INTO " +
            "lists (uuid, name, user_identifier, last_unlocked_at, demarcation_index) " +
            "VALUES ('" + listId + "', 'someName','someEmail', now(), 0)");
    }

    @Test
    public void findsTodoList_withTodosOrderedByPosition() {
        jdbcTemplate.update("INSERT INTO " +
            "todos (list_id, uuid, task, position, created_at, updated_at) " +
            "VALUES ('" + listId + "', 'uuid2', 'task2', 2, now(), now())");
        jdbcTemplate.update("INSERT INTO " +
            "todos (list_id, uuid, task, position, created_at, updated_at) " +
            "VALUES ('" + listId + "', 'uuid1', 'task1', 1, now(), now())");

        List<TodoEntity> todoEntities = todoListDao.findByEmail("someEmail").get(0).todoEntities;
        assertThat(todoEntities).hasSize(2);
        assertThat(todoEntities.get(0).task).isEqualTo("task1");
        assertThat(todoEntities.get(1).task).isEqualTo("task2");
    }
}