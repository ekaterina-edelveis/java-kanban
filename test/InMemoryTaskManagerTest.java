import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryTaskManagerTest {

    private static TaskManager manager;

    @BeforeEach
    public void beforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    public void shouldCreateAndFindTasks() {
        Task task1 = new Task("Walk the dog", "The dog walks at 8 a.m.");
        Task task2 = new Task("Go shopping", "Buy milk, bread, meat, veggies");
        int taskId = manager.createTask(task1);
        manager.createTask(task2);

        ArrayList<Task> expected = new ArrayList<>();
        expected.add(task1);
        expected.add(task2);
        ArrayList<Task> actual = manager.getAllTasks();

        assertEquals(task1, manager.findTaskById(taskId));
        assertNotNull(actual);
        assertEquals(expected, actual);

    }

    @Test
    public void shouldCreateSubtaskAndUpdateEpic() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Add new functionality",
                "Create interfaces", epic);
        Subtask subtask2 = new Subtask("Create tests",
                "Write tests for essential functionality", epic);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);


        ArrayList<Subtask> expected = new ArrayList<>();
        expected.add(subtask1);
        expected.add(subtask2);

        assertNotNull(manager.getAllSubtasksForEpic(epicId));
        assertEquals(expected, manager.getAllSubtasksForEpic(epicId));

    }

    @Test
    public void shouldUpdateEpicStatusWhenSubtaskStatusIsUpdated() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);
        Subtask subtask = new Subtask("Add new functionality",
                "Create interfaces", epic);
        manager.createSubtask(subtask);

        subtask.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask);

        Status expected = Status.IN_PROGRESS;

        assertEquals(expected, manager.findEpicById(epicId).getStatus());
    }

    @Test
    public void shouldNotAcceptEpicStatusFromUser() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);
        epic.setStatus(Status.DONE);
        manager.updateEpic(epic);

        Status expected = Status.DONE;
        assertNotEquals(expected, manager.findEpicById(epicId).getStatus());

    }

    @Test
    public void shouldDeleteAllTasks() {
        Task task1 = new Task("Walk the dog", "The dog walks at 8 a.m.");
        Task task2 = new Task("Go shopping", "Buy milk, bread, meat, veggies");
        manager.createTask(task1);
        manager.createTask(task2);

        manager.deleteAllTasks();
        assertEquals(0, manager.getAllTasks().size());
    }

    @Test
    public void shouldDeleteSubtaskAndUpdateEpic() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Add new functionality",
                "Create interfaces", epic);

        int subtaskId = manager.createSubtask(subtask1);
        manager.deleteSubtaskById(subtaskId);

        assertEquals(0, manager.getAllSubtasksForEpic(epicId).size());

    }

    @Test
    public void shouldUpdateTaskWithoutChangingId() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        int taskId = manager.createTask(task);
        task.setDescription("The dog walks at 9 a.m.");
        manager.updateTask(task);

        assertEquals(task, manager.findTaskById(taskId));

    }

    @Test
    public void shouldDeleteSubtasksWhenEpicIsDeleted() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Add new functionality",
                "Create interfaces", epic);
        manager.createSubtask(subtask1);

        manager.deleteEpicById(epicId);
        assertEquals(0, manager.getAllSubtasks().size());

    }


}