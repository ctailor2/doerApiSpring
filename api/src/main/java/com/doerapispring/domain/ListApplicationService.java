package com.doerapispring.domain;

import com.doerapispring.domain.events.TodoListEvent;
import scala.util.Try;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface ListApplicationService {
    Try<DeprecatedTodoListModel> performOperation(
            User user,
            ListId listId,
            Supplier<TodoListEvent> eventProducer,
            BiFunction<DeprecatedTodoListModel, TodoListEvent, Try<DeprecatedTodoListModel>> operation);

    DeprecatedTodoListModel getDefault(User user);

    CompletedTodoList getCompleted(User user, ListId listId);

    DeprecatedTodoListModel get(User user, ListId listId);

    List<TodoList> getAll(User user);

    void create(User user, String name);

    void setDefault(User user, ListId listId);
}
