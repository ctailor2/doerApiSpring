package com.doerapispring.todos;

import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by chiragtailor on 9/28/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    private ArgumentCaptor<TodoEntity> todoArgumentCaptor = ArgumentCaptor.forClass(TodoEntity.class);
    private Todo todo;

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(todoRepository, userRepository);
        todo = Todo.builder()
                .task("reconfigure things")
                .active(true)
                .build();
    }

    @Test
    public void get_withNoType_callsTodoRepository_returnsTodos() throws Exception {
        List<TodoEntity> todos = Arrays.asList(TodoEntity.builder()
                .task("clean the fridge")
                .active(true)
                .build());
        doReturn(todos).when(todoRepository).findByUserEmail("one@two.com");

        List<Todo> todoEntities = todoService.get("one@two.com", null);

        verify(todoRepository).findByUserEmail("one@two.com");
        Todo todo = todoEntities.get(0);
        assertThat(todo).isNotNull();
        assertThat(todo.getTask()).isEqualTo("clean the fridge");
        assertThat(todo.isActive()).isEqualTo(true);
    }

    @Test
    public void get_withTypeActive_callsTodoRepository_forActiveTodos_returnsTodos() throws Exception {
        List<TodoEntity> todos = Arrays.asList(TodoEntity.builder()
                .task("clean the fridge")
                .active(true)
                .build());

        doReturn(todos).when(todoRepository).findByUserEmailAndType("one@two.com", true);

        List<Todo> todoEntities = todoService.get("one@two.com", TodoTypeParamEnum.active);

        verify(todoRepository).findByUserEmailAndType("one@two.com", true);
        verifyNoMoreInteractions(todoRepository);
        Todo todo = todoEntities.get(0);
        assertThat(todo).isNotNull();
        assertThat(todo.getTask()).isEqualTo("clean the fridge");
        assertThat(todo.isActive()).isEqualTo(true);
    }

    @Test
    public void get_withTypeInactive_callsTodoRepository_forInactiveTodos_returnsTodos() throws Exception {
        List<TodoEntity> todos = Arrays.asList(TodoEntity.builder()
                .task("clean the fridge")
                .active(true)
                .build());

        doReturn(todos).when(todoRepository).findByUserEmailAndType("one@two.com", false);

        List<Todo> todoEntities = todoService.get("one@two.com", TodoTypeParamEnum.inactive);

        verify(todoRepository).findByUserEmailAndType("one@two.com", false);
        verifyNoMoreInteractions(todoRepository);
        Todo todo = todoEntities.get(0);
        assertThat(todo).isNotNull();
        assertThat(todo.getTask()).isEqualTo("clean the fridge");
        assertThat(todo.isActive()).isEqualTo(true);
    }

    @Test
    public void get_withNullType_freaksOutMaybe() throws Exception {
        todoService.get("one@two.com", null);
    }

    @Test
    public void create_callsUserRepository_whenUserExists_callsTodoRepository_savesTodoForUser_returnsTodoEntity() throws Exception {
        UserEntity userEntity = UserEntity.builder()
                .id(123L)
                .email("one@two.com")
                .build();
        doReturn(userEntity).when(userRepository).findByEmail(userEntity.email);

        Todo returnedTodo = todoService.create(userEntity.email, todo);

        verify(userRepository).findByEmail(userEntity.email);
        verify(todoRepository).save(todoArgumentCaptor.capture());
        TodoEntity savedTodoEntity = todoArgumentCaptor.getValue();
        assertThat(savedTodoEntity.userEntity).isEqualTo(userEntity);
        assertThat(savedTodoEntity.task).isEqualTo("reconfigure things");
        assertThat(savedTodoEntity.active).isEqualTo(true);
        assertThat(savedTodoEntity.createdAt).isToday();
        assertThat(savedTodoEntity.updatedAt).isToday();
        assertThat(returnedTodo).isEqualTo(todo);
    }

    @Test
    public void create_callsUserRepository_whenUserDoesNotExist_returnsNull() throws Exception {
        doReturn(null).when(userRepository).findByEmail("one@two.com");

        Todo returnedTodo = todoService.create("one@two.com", todo);

        verify(userRepository).findByEmail("one@two.com");
        verifyZeroInteractions(todoRepository);
        assertThat(returnedTodo).isNull();
    }
}