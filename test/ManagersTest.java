import org.junit.jupiter.api.Test;
import taskmanagement.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    public void createFunctionalTaskManager(){
        TaskManager manager = Managers.getDefault();

        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        int taskId = manager.createTask(task);
        ArrayList<Task> expectedTasks = manager.getAllTasks();

        assertEquals(task, manager.findTaskById(taskId), "Задачи не совпадают");
        assertNotNull(expectedTasks, "Задачи не возвращаются");

    }

    @Test
    public void createFunctionalHistoryManager(){
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        historyManager.add(task);

        ArrayList<Task> expectedTasks = new ArrayList<>();
        expectedTasks.add(task);
        ArrayList<Task> tasks = historyManager.getHistory();

        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(expectedTasks, tasks, "Задачи не совпадают");

    }

}