package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

class TodosResponse extends RepresentationModel<TodosResponse> {
    @JsonProperty("todos")
    private final List<TodoDTO> todoDTOs;

    TodosResponse(List<TodoDTO> todoDTOs) {
        this.todoDTOs = todoDTOs;
    }

    List<TodoDTO> getTodoDTOs() {
        return todoDTOs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TodosResponse that = (TodosResponse) o;

        return todoDTOs != null ? todoDTOs.equals(that.todoDTOs) : that.todoDTOs == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (todoDTOs != null ? todoDTOs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodosResponse{" +
                "todoDTOs=" + todoDTOs +
                '}';
    }
}
