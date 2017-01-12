package com.doerapispring.domain;

import java.util.List;

public class PostponedList {
    private final List<Todo> todos;

    public PostponedList(List<Todo> todos) {
        this.todos = todos;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public Todo add(String task) {
        String localIdentifier = String.valueOf(getTodos().size() + 1);
        Todo todo = new Todo(localIdentifier, task, ScheduledFor.later);
        todos.add(todo);
        return todo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PostponedList that = (PostponedList) o;

        return todos != null ? todos.equals(that.todos) : that.todos == null;

    }

    @Override
    public int hashCode() {
        return todos != null ? todos.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PostponedList{" +
                "todos=" + todos +
                '}';
    }
}
