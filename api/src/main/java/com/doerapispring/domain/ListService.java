package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListService implements ListApplicationService {
    private final OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;
    private final OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository;

    ListService(OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository,
                OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository) {
        this.todoListRepository = todoListRepository;
        this.completedTodoRepository = completedTodoRepository;
    }

    public void unlock(User user) throws InvalidRequestException {
        TodoList todoList = todoListRepository.findOne(user.getUserId())
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.unlock();
            todoListRepository.save(todoList);
        } catch (LockTimerNotExpiredException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public ReadOnlyTodoList get(User user) throws InvalidRequestException {
        return todoListRepository.findOne(user.getUserId())
            .map(TodoList::read)
            .orElseThrow(InvalidRequestException::new);
    }

    public List<CompletedTodo> getCompleted(User user) throws InvalidRequestException {
        return completedTodoRepository.findAll(user.getUserId());
    }
}
