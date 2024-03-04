import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.Managers;
import taskmanagement.Task;
import taskmanagement.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private static TaskManager manager;

    @BeforeEach
    public void beforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    public void treatsTasksWithSameIdAsEqual() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        manager.createTask(task);
        Task savedTask = manager.findTaskById(task.getId());

        task.setDescription("The dog walks at 8 a.m.");
        manager.updateTask(task);
        assertEquals(savedTask, manager.findTaskById(task.getId()));
    }


    @Test
    public void shouldCalculateEndTime() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.", "04.03.24 20:30", 30);
        manager.createTask(task);

        String expected = "2024-03-04T21:00";
        String actual = task.getEndTime().toString();

        assertEquals(expected, actual);
    }


}