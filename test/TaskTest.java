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
    public void treatsTasksWithSameIdAsEqual(){
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        int taskId = manager.createTask(task);
        Task savedTask = manager.findTaskById(taskId);

        task.setDescription("The dog walks at 8 a.m.");
        manager.updateTask(task);
        assertEquals(savedTask, manager.findTaskById(taskId));
    }



}