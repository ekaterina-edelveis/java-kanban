import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    public void beforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    public void shouldCreateAndFindTasks() {
        Task task1 = new Task("Walk the dog", "The dog walks at 8 a.m.");
        Task task2 = new Task("Go shopping", "Buy milk, bread, meat, veggies");
        int task1Id = manager.createTask(task1);
        int task2d = manager.createTask(task2);

        String nameExpected = "Walk the dog";
        List<Task> actual = manager.getAllTasks();
        String nameActual = actual.get(0).getName();

        assertNotNull(actual);
        assertEquals(2, actual.size());
        assertEquals(nameExpected, nameActual);

    }

    @Test
    public void shouldCreateSubtaskAndAddToEpic() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Add new functionality",
                "Create interfaces", "27.02.24 19:30", 90, manager.findEpicById(epicId));
        Subtask subtask2 = new Subtask("Create tests",
                "Write tests for essential functionality", "28.02.24 19:30", 90, manager.findEpicById(epicId));

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertNotNull(manager.getAllSubtasksForEpic(epicId));
        assertEquals(2, manager.getAllSubtasksForEpic(epicId).size());

    }

    @Test
    public void shouldNotAcceptEpicStatusFromUser() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Epic found = manager.findEpicById(epicId);
        found.setStatus(Status.DONE);
        manager.updateEpic(found);

        Status expected = Status.DONE;
        assertNotEquals(expected, manager.findEpicById(epicId));

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
                "Create interfaces", "27.02.24 19:30", 90, manager.findEpicById(epicId));

        int subId = manager.createSubtask(subtask1);
        manager.deleteSubtaskById(subId);

        assertEquals(0, manager.getAllSubtasksForEpic(epicId).size());

    }

    @Test
    public void shouldUpdateTaskWithoutChangingId() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        int taskId = manager.createTask(task);

        Task found = manager.findTaskById(taskId);
        found.setDescription("The dog walks at 9 a.m.");
        manager.updateTask(found);

        assertEquals(found, manager.findTaskById(taskId));

    }

    @Test
    public void shouldDeleteSubtasksWhenEpicIsDeleted() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Add new functionality",
                "Create interfaces", "27.02.24 19:30", 90, manager.findEpicById(epicId));
        manager.createSubtask(subtask1);

        manager.deleteEpicById(epicId);
        assertEquals(0, manager.getAllSubtasks().size());

    }

    @Test
    public void shouldNotFindDeletedTaskById() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        int taskId = manager.createTask(task);
        manager.deleteTaskById(taskId);
        Task actual = manager.findTaskById(taskId);

        assertNull(actual);
    }

    @Test
    public void shouldNotKeepDeletedSubTaskInEpic() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Add new functionality",
                "Create interfaces", "27.02.24 19:30", 90, manager.findEpicById(epicId));
        int subId = manager.createSubtask(subtask);
        manager.deleteSubtaskById(subId);

        assertEquals(0, manager.getAllSubtasksForEpic(epicId).size());

    }

    @Test
    public void shouldPrioritizeTasks() {
        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        int t1Id = manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);
        int t2Id = manager.createTask(t2);

        Task t3 = new Task("watch a movie", "choose from the list", "29.02.24 20:00", 90);
        int t3Id = manager.createTask(t3);

        List<Task> expected = new ArrayList<>();
        expected.add(manager.findTaskById(t3Id));
        expected.add(manager.findTaskById(t2Id));
        expected.add(manager.findTaskById(t1Id));

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(expected, prioritized);

    }

    @Test
    public void shouldNotCreateTaskIfTimeOverlap() {

        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);

        Task t3 = new Task("watch a movie", "choose from the list", "29.02.24 20:00", 90);

        Task t4 = new Task("chat with a friend", "call Alex", "01.03.24 19:30", 25);

        int t1Id = 0;
        int t2Id = 0;
        int t3Id = 0;
        int t4Id = 0;

        try {
            t1Id = manager.createTask(t1);
            t2Id = manager.createTask(t2);
            t3Id = manager.createTask(t3);
            t4Id = manager.createTask(t4);
        } catch (ManagerSaveException ex) {

        }

        List<Task> expected = new ArrayList<>();
        expected.add(manager.findTaskById(t3Id));
        expected.add(manager.findTaskById(t2Id));
        expected.add(manager.findTaskById(t1Id));

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(expected, prioritized);

    }


    @Test
    public void shouldNotUpdateTaskIfTimeOverlap() {

        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        int t1Id = manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);
        int t2Id = manager.createTask(t2);

        try {
            Task found = manager.findTaskById(t2Id);
            found.setStartTime("01.03.24 18:00");
            manager.updateTask(found);
        } catch (ManagerSaveException ex) {

        }

        LocalDateTime expected = LocalDateTime.of(2024,
                Month.MARCH, 1, 10, 0);
        LocalDateTime actual = manager.findTaskById(t2Id).getStartTime();

        assertEquals(expected, actual);

    }



    @Test
    public void shouldUpdateTaskIfNoTimeOverlap() {

        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        int t1Id = manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);
        int t2Id = manager.createTask(t2);

        Task found = manager.findTaskById(t2Id);
        found.setStartTime("02.03.24 18:00");
        manager.updateTask(found);


        LocalDateTime expected = LocalDateTime.of(2024,
                Month.MARCH, 2, 18, 0);
        LocalDateTime actual = manager.findTaskById(t2Id).getStartTime();

        assertEquals(expected, actual);

    }


}