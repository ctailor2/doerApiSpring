package com.doerapispring

import java.time.Instant
import java.util
import java.util.Date

import com.doerapispring.domain._
import com.doerapispring.domain.events._
import com.doerapispring.storage.{TodoListEventRepository, TodoListRepository, UserRepository}
import org.assertj.core.api.Assertions.assertThat
import org.junit.runner.RunWith
import org.junit.{Before, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringRunner

import scala.jdk.javaapi.CollectionConverters.asScala
import scala.jdk.javaapi.OptionConverters.toJava

@SpringBootTest
@Sql(scripts = Array("/cleanup.sql"), executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = Array("/cleanup.sql"), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RunWith(classOf[SpringRunner])
@ActiveProfiles(value = Array("test")) class CompletedTodoListEventSourcedRepositoryTest {
  @Autowired private val userRepository: UserRepository = null
  @Autowired private val todoListRepository: TodoListRepository = null
  @Autowired private val completedTodoListEventSourcedRepository: CompletedTodoListEventSourcedRepository = null
  @Autowired private val todoListEventRepository: TodoListEventRepository = null

  private var userId: UserId = _
  private var listId: ListId = _
  private var todoList: TodoList = _

  @Before
  @throws[Exception]
  def setUp(): Unit = {
    userId = new UserId("someUserId")
    listId = new ListId("someListId")
    userRepository.save(new User(userId, listId))
    todoList = new TodoList(userId, listId, "someName")
    todoListRepository.save(todoList)
  }

  @Test def retrievesTheCompletedTodoListMatchingTheUserIdAndListId(): Unit = {
    val otherListId = new ListId("someOtherListId")
    val otherUserId = new UserId("someOtherUserId")
    userRepository.save(new User(otherUserId, otherListId))
    val usersOtherList = new TodoList(userId, otherListId, "someName")
    todoListRepository.save(usersOtherList)
    val todoId1 = "someCompletedTodoId"
    todoListEventRepository.saveAll(userId, listId, asScala(util.Arrays.asList[TodoListEvent](DeprecatedTodoAddedEvent(todoId1, "someTask"), DeprecatedTodoCompletedEvent(todoId1))).toList)
    val todoId2 = "someOtherCompletedTodoId"
    todoListEventRepository.saveAll(userId, otherListId, asScala(util.Arrays.asList[TodoListEvent](DeprecatedTodoAddedEvent(todoId2, "someOtherTask"), DeprecatedTodoCompletedEvent(todoId2))).toList)
    val optionalCompletedTodoList = completedTodoListEventSourcedRepository.find(userId, listId)
    assertThat(toJava(optionalCompletedTodoList)).isNotEmpty
    assertThat(optionalCompletedTodoList.get.getTodos).usingElementComparatorIgnoringFields("completedAt").containsExactly(new DeprecatedCompletedTodo(new CompletedTodoId(todoId1), "someTask", Date.from(Instant.EPOCH)))
  }

  @Test def includesCompletedTodosFromAllOrigins(): Unit = {
    val todoId1 = "todoId1"
    val todoId2 = "todoId2"
    val displacingTodoId = "displacingTodoId"
    val deferredTodoId = "deferredTodoId"
    todoListEventRepository.saveAll(userId, listId, asScala(util.Arrays.asList[TodoListEvent](DeprecatedTodoAddedEvent(todoId1, "task1"), DeprecatedTodoAddedEvent(todoId2, "task2"),
      DeprecatedTodoDisplacedEvent(displacingTodoId, "displacingTask"), DeprecatedDeferredTodoAddedEvent(deferredTodoId, "deferredTask"), DeprecatedTodoCompletedEvent(todoId2), DeprecatedTodoCompletedEvent(displacingTodoId), DeprecatedTodoCompletedEvent(deferredTodoId))).toList)
    val optionalCompletedTodoList = completedTodoListEventSourcedRepository.find(userId, listId)
    assertThat(toJava(optionalCompletedTodoList)).isNotEmpty
    assertThat(optionalCompletedTodoList.get.getTodos).usingElementComparatorIgnoringFields("completedAt").contains(new DeprecatedCompletedTodo(new CompletedTodoId(todoId2), "task2", Date.from(Instant.now)), new DeprecatedCompletedTodo(new CompletedTodoId(displacingTodoId), "displacingTask", Date.from(Instant.now)), new DeprecatedCompletedTodo(new CompletedTodoId(deferredTodoId), "deferredTask", Date.from(Instant.now)))
  }

  @Test def retrievesCompletedTodoListWithTodosInDescendingOrderByVersion(): Unit = {
    val todoId1 = "earlierId"
    val todoId2 = "laterId"
    todoListEventRepository.saveAll(userId, listId, asScala(util.Arrays.asList[TodoListEvent](DeprecatedTodoAddedEvent(todoId1, "earlierTask"), DeprecatedTodoCompletedEvent(todoId1), DeprecatedTodoAddedEvent(todoId2, "laterTask"), DeprecatedTodoCompletedEvent(todoId2))).toList)
    val optionalCompletedTodoList = completedTodoListEventSourcedRepository.find(userId, listId)
    assertThat(toJava(optionalCompletedTodoList)).isNotEmpty
    assertThat(optionalCompletedTodoList.get.getTodos).usingElementComparatorIgnoringFields("completedAt").containsExactly(new DeprecatedCompletedTodo(new CompletedTodoId(todoId2), "laterTask", Date.from(Instant.now)), new DeprecatedCompletedTodo(new CompletedTodoId(todoId1), "earlierTask", Date.from(Instant.now)))
  }
}