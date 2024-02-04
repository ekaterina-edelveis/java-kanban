import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
        taskManager = Managers.getDefault();
    }

    @Test
    public void shouldAddTasksToHistory() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");

        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();

        assertNotEquals(0, history.size(), "История пустая.");
        assertEquals(1, history.size(), "История пустая.");
    }

    @Test
    public void shouldDeletePreviousDuplicatedSearches(){
        Task task1 = new Task("Walk the dog", "The dog walks at 8 a.m.");
        Task task2 = new Task("Go shopping", "Buy milk, bread, meat, veggies");

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        final List<Task> history = historyManager.getHistory();
        List<Task> expected = new ArrayList<>();
        expected.add(task2);
        expected.add(task1);

        assertEquals(expected, history, "Списки не равны");
    }

    @Test
    public void shouldDeleteTaskFromHistory(){
        Task task1 = new Task("Walk the dog", "The dog walks at 8 a.m.");

        historyManager.add(task1);
        historyManager.remove(task1.getId());

        final List<Task> history = historyManager.getHistory();

        assertNotEquals(1, history.size(), "История пустая.");
        assertEquals(0, history.size(), "История пустая.");


    }


}