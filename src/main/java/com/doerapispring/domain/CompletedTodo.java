package com.doerapispring.domain;

import java.util.Date;

public class CompletedTodo {
    private final String task;
    private final Date completedAt;

    public CompletedTodo(String task, Date completedAt) {
        this.task = task;
        this.completedAt = completedAt;
    }

    public String getTask() {
        return task;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompletedTodo that = (CompletedTodo) o;

        if (task != null ? !task.equals(that.task) : that.task != null) return false;
        return completedAt != null ? completedAt.equals(that.completedAt) : that.completedAt == null;

    }

    @Override
    public int hashCode() {
        int result = task != null ? task.hashCode() : 0;
        result = 31 * result + (completedAt != null ? completedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompletedTodo{" +
                "task='" + task + '\'' +
                ", completedAt=" + completedAt +
                '}';
    }
}
