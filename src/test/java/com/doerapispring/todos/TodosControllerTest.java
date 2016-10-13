package com.doerapispring.todos;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by chiragtailor on 9/27/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TodosControllerTest {
    private TodosController todosController;

    @Mock
    private TodoService todoService;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        todosController = new TodosController(todoService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(todosController)
                .build();
    }

    @Test
    public void index_mapping() throws Exception {
        mockMvc.perform(get("/v1/todos")
                .header("Session-Token", "tokenz"))
                .andExpect(status().isOk());
    }

    @Test
    public void index_withNoTypeSpecified_callsTodoService_withNullType() throws Exception {
        todosController.index("test@email.com", null);
        verify(todoService).get("test@email.com", null);
    }

    @Test
    public void index_withValidTypeSpecified_callsTodoService_withSpecifiedType() throws Exception {
        todosController.index("test@email.com", TodoTypeParamEnum.active);
        verify(todoService).get("test@email.com", TodoTypeParamEnum.active);
    }

    @Test
    public void index_withInvalidTypeSpecified_returns400() throws Exception {
        mockMvc.perform(get("/v1/todos")
                .param("type", "notARealType")
                .header("Session-Token", "tokenz"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_mapping() throws Exception {
        mockMvc.perform(post("/v1/todos")
                .header("Session-Token", "tokenz")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"task\": \"return redbox movie\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void create_callsTokenService() throws Exception {
        Todo todo = Todo.builder()
                .task("browse the web")
                .build();
        todosController.create("test@email.com", todo);
        verify(todoService).create("test@email.com", todo);
    }
}