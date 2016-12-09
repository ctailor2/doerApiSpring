package com.doerapispring.todos;

import com.doerapispring.apiTokens.AuthenticatedUser;
import com.doerapispring.userSessions.OperationRefusedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by chiragtailor on 9/27/16.
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
public class TodosController {
    private TodoService todoService;

    @Autowired
    public TodosController(TodoService todoService) {
        this.todoService = todoService;
    }

    @RequestMapping(value = "/todos", method = RequestMethod.GET)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    ResponseEntity<List<Todo>> index(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                     @RequestParam(name = "scheduling", required = false, defaultValue = "anytime") String scheduling) {
        ScheduledFor scheduledFor;
        try {
            scheduledFor = Enum.valueOf(ScheduledFor.class, scheduling);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(todoService.getByScheduling(authenticatedUser.getUserIdentifier(), scheduledFor));
    }

    @RequestMapping(value = "/todos", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity<Todo> create(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                @RequestBody TodoForm todoForm) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(todoService.create(authenticatedUser.getUserIdentifier(), todoForm.getTask(), todoForm.getScheduling()));
        } catch (OperationRefusedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
