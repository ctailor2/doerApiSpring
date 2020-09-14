package integration;

import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DefaultListIntegrationTest extends AbstractWebAppJUnit4SpringContextTests {
    private final HttpHeaders httpHeaders = new HttpHeaders();

    @Autowired
    private UserSessionsApiService userSessionsApiService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String identifier = "test@email.com";
        SessionTokenDTO signupSessionToken = userSessionsApiService.signup(identifier, "password");
        httpHeaders.add("Session-Token", signupSessionToken.getToken());
    }

    @Test
    public void list() throws Exception {
        mockMvc.perform(get("/v1/lists/default")
            .headers(httpHeaders))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.list.profileName", equalTo("default")));

        String otherListName = "someListName";
        String nextActionHref = JsonPath.parse(mockMvc.perform(post("/v1/lists")
                .headers(httpHeaders)
                .content("{\n  \"name\": \"" + otherListName + "\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString()).read("$._links.lists.href", String.class);

        nextActionHref = JsonPath.parse(mockMvc.perform(get(nextActionHref)
                .headers(httpHeaders))
                .andReturn().getResponse().getContentAsString()).read("$.lists[1]._links.setDefault.href", String.class);

        mockMvc.perform(post(nextActionHref)
            .headers(httpHeaders))
            .andExpect(status().isAccepted());

        mockMvc.perform(get("/v1/lists/default")
            .headers(httpHeaders))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.list.profileName", equalTo(otherListName)));
    }
}
