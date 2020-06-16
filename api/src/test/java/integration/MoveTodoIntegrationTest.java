package integration;

import com.doerapispring.domain.*;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class MoveTodoIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private User user;

    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Autowired
    private TodoApplicationService todoApplicationService;

    @Autowired
    private ListApplicationService listApplicationService;

    @Autowired
    private UserService userService;

    private ListId defaultListId;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
        user = userService.find(identifier).orElseThrow(RuntimeException::new);
        defaultListId = user.getDefaultListId();
    }

    @Test
    public void move() throws Exception {
        todoApplicationService.performOperation(user, defaultListId, (todoList, todoId) -> TodoListModel.add(todoList, todoId, "some task"));
        todoApplicationService.performOperation(user, defaultListId, (todoList, todoId) -> TodoListModel.add(todoList, todoId, "some other task"));

        String todosResponse = mockMvc.perform(get("/v1/lists/" + defaultListId.get())
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("some other task")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("some task")))
            .andReturn().getResponse().getContentAsString();
        String moveLink = JsonPath.parse(todosResponse).read("$.list.todos[0]._links.move[1].href", String.class);
        String movePath = URI.create(moveLink).getPath();

        MvcResult mvcResult = mockMvc.perform(post(movePath)
            .headers(httpHeaders))
            .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        assertThat(responseContent, isJson());
        assertThat(responseContent, hasJsonPath("$._links", not(isEmptyString())));
        assertThat(responseContent, hasJsonPath("$._links.self.href", containsString(movePath)));
        assertThat(responseContent, hasJsonPath("$._links.list.href", endsWith("/v1/lists/" + defaultListId.get())));

        mockMvc.perform(get("/v1/lists/" + defaultListId.get())
            .headers(httpHeaders))
            .andExpect(jsonPath("$.list.todos", hasSize(2)))
            .andExpect(jsonPath("$.list.todos[0].task", equalTo("some task")))
            .andExpect(jsonPath("$.list.todos[1].task", equalTo("some other task")));
    }
}
