package com.doerapispring.domain;

import com.doerapispring.domain.events.TodoListEvent;
import scala.util.Try;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TodoApplicationService {
    Try<DeprecatedTodoListModel> performOperation(
            User user,
            ListId listId,
            Supplier<TodoListEvent> eventProducer
    );

    Try<DeprecatedTodoListModel> performOperation(
            User user,
            ListId listId,
            Function<TodoId, TodoListEvent> eventProducer
    );
}
