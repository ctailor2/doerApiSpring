package integration;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static scala.jdk.javaapi.CollectionConverters.asJava;

public class EndToEndApplicationTest extends AbstractWebAppJUnit4SpringContextTests {
    @Autowired
    private UserService userService;

    @Autowired
    private ListService listService;

    private User user;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        user = userService.create("user");
    }

    @Test
    public void createsAList() {
        String listName = "someName";
        listService.create(user, listName);

        List<TodoList> todoLists = asJava(listService.getAll(user));

        List<String> listNames = todoLists.stream()
                .map(TodoList::getName)
                .collect(toList());
        assertThat(listNames).contains(listName);
    }
}
