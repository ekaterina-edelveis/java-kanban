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
        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> expected = new ArrayList<>();
        expected.add(task1);
        expected.add(task2);
        List<Task> actual = manager.getAllTasks();

        assertEquals(task1, manager.findTaskById(task1.getId()));
        assertNotNull(actual);
        assertEquals(expected, actual);

    }

    @Test
    public void shouldCreateSubtaskAndAddToEpic() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Add new functionality",
                "Create interfaces", "27.02.24 19:30", 90, epic);
        Subtask subtask2 = new Subtask("Create tests",
                "Write tests for essential functionality", "28.02.24 19:30", 90, epic);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);


        ArrayList<Subtask> expected = new ArrayList<>();
        expected.add(subtask1);
        expected.add(subtask2);

        assertNotNull(manager.getAllSubtasksForEpic(epic.getId()));
        assertEquals(expected, manager.getAllSubtasksForEpic(epic.getId()));

    }

    @Test
    public void shouldNotAcceptEpicStatusFromUser() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);
        epic.setStatus(Status.DONE);
        manager.updateEpic(epic);

        Status expected = Status.DONE;
        assertNotEquals(expected, manager.findEpicById(epic.getId()).getStatus());

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
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Add new functionality",
                "Create interfaces", "27.02.24 19:30", 90, epic);

        manager.createSubtask(subtask1);
        manager.deleteSubtaskById(subtask1.getId());

        assertEquals(0, manager.getAllSubtasksForEpic(epic.getId()).size());

    }

    @Test
    public void shouldUpdateTaskWithoutChangingId() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        manager.createTask(task);
        task.setDescription("The dog walks at 9 a.m.");
        manager.updateTask(task);

        assertEquals(task, manager.findTaskById(task.getId()));

    }

    @Test
    public void shouldDeleteSubtasksWhenEpicIsDeleted() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Add new functionality",
                "Create interfaces", "27.02.24 19:30", 90, epic);
        manager.createSubtask(subtask1);

        manager.deleteEpicById(epic.getId());
        assertEquals(0, manager.getAllSubtasks().size());

    }

    @Test
    public void shouldNotFindDeletedTaskById() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        manager.createTask(task);
        manager.deleteTaskById(task.getId());
        Task actual = manager.findTaskById(task.getId());

        assertNull(actual);
    }

    @Test
    public void shouldNotKeepDeletedSubTaskInEpic() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Add new functionality",
                "Create interfaces", "27.02.24 19:30", 90, epic);
        manager.createSubtask(subtask);
        manager.deleteSubtaskById(subtask.getId());

        assertEquals(0, manager.getAllSubtasksForEpic(epic.getId()).size());

    }

    @Test
    public void shouldPrioritizeTasks() {
        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);
        manager.createTask(t2);

        Task t3 = new Task("watch a movie", "choose from the list", "29.02.24 20:00", 90);
        manager.createTask(t3);

        List<Task> expected = new ArrayList<>();
        expected.add(t3);
        expected.add(t2);
        expected.add(t1);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(expected, prioritized);

    }

    @Test
    public void shouldNotCreateTaskIfTimeOverlap() {

        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);

        Task t3 = new Task("watch a movie", "choose from the list", "29.02.24 20:00", 90);

        Task t4 = new Task("chat with a friend", "call Alex", "01.03.24 19:30", 25);

        try {
            manager.createTask(t1);
            manager.createTask(t2);
            manager.createTask(t3);
            manager.createTask(t4);
        } catch (ManagerSaveException ex) {

        }

        List<Task> expected = new ArrayList<>();
        expected.add(t3);
        expected.add(t2);
        expected.add(t1);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(expected, prioritized);

    }
    /*

    @Test
    public void shouldNotUpdateTaskIfTimeOverlap() {

        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);
        manager.createTask(t2);

        try {
            t2.setStartTime("01.03.24 18:00");
            manager.updateTask(t2);
        } catch (ManagerSaveException ex) {

        }

        LocalDateTime expected = LocalDateTime.of(2024,
                Month.MARCH, 1, 10, 0);
        LocalDateTime actual = t2.getStartTime();

        assertEquals(expected, actual);

    }

     */

    @Test
    public void shouldUpdateTaskIfNoTimeOverlap() {

        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);
        manager.createTask(t2);

        t2.setStartTime("02.03.24 18:00");
        manager.updateTask(t2);


        LocalDateTime expected = LocalDateTime.of(2024,
                Month.MARCH, 2, 18, 0);
        LocalDateTime actual = t2.getStartTime();

        assertEquals(expected, actual);

    }


}