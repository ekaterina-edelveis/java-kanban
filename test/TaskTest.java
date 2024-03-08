import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.Managers;
import taskmanagement.Task;
import taskmanagement.TaskManager;

import java.time.LocalDateTime;
import java.time.Month;

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
        int id = manager.createTask(task);
        Task savedTask = manager.findTaskById(id);

        savedTask.setDescription("The dog walks at 8 a.m.");
        manager.updateTask(savedTask);
        assertEquals(savedTask, manager.findTaskById(id));
    }


    @Test
    public void shouldCalculateEndTime() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.", "04.03.24 20:30", 30);
        int id = manager.createTask(task);

        //"2024-03-04T21:00"
        LocalDateTime expected = LocalDateTime.of(2024, Month.MARCH, 4, 21, 0);
        LocalDateTime actual = manager.findTaskById(id).getEndTime();

        assertEquals(expected, actual);
    }


}