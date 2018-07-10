package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    private ListService listService;

    @Mock
    private ObjectRepository<CompletedList, String> mockCompletedListRepository;

    @Mock
    private ObjectRepository<MasterList, String> mockMasterListRepository;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private UniqueIdentifier<String> uniqueIdentifier;
    private MasterList masterList;

    private final ArgumentCaptor<Todo> todoArgumentCaptor = ArgumentCaptor.forClass(Todo.class);

    private CompletedList completedList;

    @Before
    public void setUp() throws Exception {
        listService = mock(ListService.class);
        todoService = new TodoService(
            listService,
            mockCompletedListRepository,
            mockMasterListRepository
        );
        uniqueIdentifier = new UniqueIdentifier<>("userId");
        masterList = mock(MasterList.class);
        when(listService.get(any())).thenReturn(masterList);
        completedList = mock(CompletedList.class);
        when(mockCompletedListRepository.find(uniqueIdentifier)).thenReturn(Optional.of(completedList));
    }

    @Test
    public void get_whenMasterListFound_returnsMasterListFromRepository() throws Exception {
        when(listService.get(any())).thenReturn(masterList);
        User user = new User(uniqueIdentifier);

        MasterList masterList = todoService.get(user);

        verify(listService).get(user);
        assertThat(masterList).isEqualTo(masterList);
    }

    @Test
    public void get_whenMasterListNotFound_refusesGet() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.get(new User(new UniqueIdentifier<>("one@two.com")));
    }

    @Test
    public void getDeferredTodos_whenMasterListFound_returnsDeferredTodos() throws Exception {
        List<Todo> expectedTodos = singletonList(new Todo("someIdentifier", "someTask", 1));
        when(masterList.getDeferredTodos()).thenReturn(expectedTodos);

        User user = new User(uniqueIdentifier);
        List<Todo> actualTodos = todoService.getDeferredTodos(user);

        verify(listService).get(user);
        assertThat(actualTodos).isEqualTo(expectedTodos);
    }

    @Test
    public void getDeferredTodos_whenMasterListFound_whenLockTimerNotExpired_refusesOperation() throws Exception {
        when(masterList.getDeferredTodos()).thenThrow(new LockTimerNotExpiredException());

        exception.expect(OperationRefusedException.class);
        User user = new User(uniqueIdentifier);
        todoService.getDeferredTodos(user);
    }

    @Test
    public void getDeferredTodos_whenMasterListNotFound_refusesGet() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.getDeferredTodos(new User(uniqueIdentifier));
    }

    @Test
    public void create_whenMasterListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.create(new User(uniqueIdentifier), task);

        verify(masterList).add(task);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void create_whenMasterListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        when(masterList.add(any())).thenThrow(new DuplicateTodoException());

        assertThatThrownBy(() ->
            todoService.create(new User(uniqueIdentifier), "some things"))
            .isInstanceOf(OperationRefusedException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void create_whenMasterListFound_whenListFull_refusesCreate() throws Exception {
        when(masterList.add(any())).thenThrow(new ListSizeExceededException());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void create_whenMasterListNotFound_refusesCreate() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void create_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void createDeferred_whenMasterListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.createDeferred(new User(uniqueIdentifier), task);

        verify(masterList).addDeferred(task);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void createDeferred_whenMasterListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        when(masterList.addDeferred(any())).thenThrow(new DuplicateTodoException());

        assertThatThrownBy(() ->
            todoService.createDeferred(new User(uniqueIdentifier), "some things"))
            .isInstanceOf(OperationRefusedException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void createDeferred_whenMasterListNotFound_refusesCreate() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.createDeferred(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void createDeferred_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(OperationRefusedException.class);
        todoService.createDeferred(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void delete_whenMasterListFound_whenTodoFound_deletesTodoUsingRepository() throws Exception {
        String localIdentifier = "someIdentifier";
        todoService.delete(new User(uniqueIdentifier), localIdentifier);

        verify(masterList).delete(localIdentifier);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void delete_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModels_refusesDelete() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(OperationRefusedException.class);
        todoService.delete(new User(uniqueIdentifier), "someTodoId");
    }

    @Test
    public void delete_whenMasterListFound_whenTodoNotFound_refusesDelete() throws Exception {
        when(masterList.delete(any())).thenThrow(new TodoNotFoundException());

        exception.expect(OperationRefusedException.class);
        todoService.delete(new User(uniqueIdentifier), "someTodoId");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_savesUsingRepository() throws Exception {
        todoService.displace(new User(uniqueIdentifier), "someTask");

        verify(masterList).displace("someTask");
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void displace_whenMasterListNotFound_refusesDisplace() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(uniqueIdentifier), "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenTodoWithTaskExists_refusesDisplace() throws Exception {
        when(masterList.displace(any())).thenThrow(new DuplicateTodoException());

        assertThatThrownBy(() ->
            todoService.displace(new User(uniqueIdentifier), "someTask"))
            .isInstanceOf(OperationRefusedException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesDisplace() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(uniqueIdentifier), "someTask");
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_updatesUsingRepository() throws Exception {
        String updatedTask = "someOtherTask";
        String localIdentifier = "someIdentifier";
        todoService.update(new User(uniqueIdentifier), localIdentifier, updatedTask);

        verify(masterList).update(localIdentifier, updatedTask);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(OperationRefusedException.class);
        todoService.update(new User(uniqueIdentifier), "someIdentifier", "someOtherTask");
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_whenTodoWithTaskExists_refusesUpdate() throws Exception {
        when(masterList.update(any(), any())).thenThrow(new DuplicateTodoException());

        assertThatThrownBy(() ->
            todoService.update(new User(uniqueIdentifier), "someIdentifier", "someTask"))
            .isInstanceOf(OperationRefusedException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void update_whenMasterListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        when(masterList.update(any(), any())).thenThrow(new TodoNotFoundException());

        exception.expect(OperationRefusedException.class);
        todoService.update(new User(uniqueIdentifier), "someId", "someTask");
    }

    @Test
    public void update_whenMasterListNotFound_refusesUpdate() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.update(new User(uniqueIdentifier), "someId", "someTask");
    }

    @Test
    public void complete_whenMasterListFound_whenTodoFound_completesUsingRepository() throws Exception {
        String localIdentifier = "someIdentifier";
        todoService.complete(new User(uniqueIdentifier), localIdentifier);

        verify(masterList).complete(localIdentifier);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void complete_addsCompletedTaskToCompletedList_savesUsingRepository() throws Exception {
        String completedTask = "completedTask";
        when(masterList.complete(anyString())).thenReturn(completedTask);

        String localIdentifier = "someIdentifier";
        todoService.complete(new User(uniqueIdentifier), localIdentifier);

        verify(mockCompletedListRepository).find(uniqueIdentifier);
        verify(completedList).add(completedTask);
        verify(mockCompletedListRepository).save(completedList);
    }

    @Test
    public void complete_whenCompletedListRepositoryRejectsModel_refusesOperation() throws Exception {
        String completedTask = "completedTask";
        when(masterList.complete(anyString())).thenReturn(completedTask);
        doThrow(new AbnormalModelException()).when(mockCompletedListRepository).save(any(CompletedList.class));

        String localIdentifier = "someIdentifier";
        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(uniqueIdentifier), localIdentifier);
    }

    @Test
    public void complete_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(uniqueIdentifier), "someIdentifier");
    }

    @Test
    public void complete_whenMasterListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        doThrow(new TodoNotFoundException()).when(masterList).complete(any());

        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(uniqueIdentifier), "someId");
    }

    @Test
    public void complete_whenMasterListNotFound_refusesUpdate() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(uniqueIdentifier), "someId");
    }

    @Test
    public void getCompleted_whenCompletedListFound_returnsCompletedListFromRepository() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        CompletedList completedListFromRepository = new CompletedList(mock(Clock.class), uniqueIdentifier, emptyList());
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.of(completedListFromRepository));
        User user = new User(uniqueIdentifier);

        CompletedList completedList = todoService.getCompleted(user);

        verify(mockCompletedListRepository).find(uniqueIdentifier);
        assertThat(completedList).isEqualTo(completedListFromRepository);
    }

    @Test
    public void getCompleted_whenCompletedListNotFound_refusesGet() throws Exception {
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        todoService.getCompleted(new User(new UniqueIdentifier<>("one@two.com")));
    }

    @Test
    public void move_whenMasterListFound_whenTodosFound_updatesMovedTodosUsingRepository() throws Exception {
        masterList.add("task1");
        masterList.add("task2");

        String sourceIdentifier = "sourceIdentifier";
        String destinationIdentifier = "destinationIdentifier";
        todoService.move(new User(uniqueIdentifier), sourceIdentifier, destinationIdentifier);

        verify(masterList).move(sourceIdentifier, destinationIdentifier);
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void move_whenMasterListFound_whenTodosFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(OperationRefusedException.class);
        todoService.move(new User(uniqueIdentifier), "sourceIdentifier", "destinationIdentifier");
    }

    @Test
    public void move_whenMasterListFound_whenTodosNotFound_refusesOperation() throws Exception {
        when(masterList.move(any(), any())).thenThrow(new TodoNotFoundException());

        exception.expect(OperationRefusedException.class);
        todoService.move(new User(uniqueIdentifier), "idOne", "idTwo");
    }

    @Test
    public void move_whenMasterListNotFound_refsusesOperation() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.move(new User(uniqueIdentifier), "idOne", "idTwo");
    }

    @Test
    public void pull_whenMasterListFound_whenTodosPulled_updatesPulledTodosUsingRepository() throws Exception {
        todoService.pull(new User(uniqueIdentifier));

        verify(masterList).pull();
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void pull_whenMasterListFound_whenTodosPulled_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(OperationRefusedException.class);
        todoService.pull(new User(uniqueIdentifier));
    }
}