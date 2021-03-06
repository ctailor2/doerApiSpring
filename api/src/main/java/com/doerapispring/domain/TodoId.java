package com.doerapispring.domain;

public class TodoId {
    private String identifier;

    public TodoId(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    //  TODO: Added just for marshaling/unmarshaling
    public TodoId() {
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TodoId todoId = (TodoId) o;

        return identifier != null ? identifier.equals(todoId.identifier) : todoId.identifier == null;
    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TodoId{" +
            "identifier='" + identifier + '\'' +
            '}';
    }
}
