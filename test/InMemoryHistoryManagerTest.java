import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private static HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();

    }

    @Test
    public void shouldAddTasksToHistory() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");

        historyManager.add(task);
        final ArrayList<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }


    @Test
    public void shouldSavePreviousStateOfTasks() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        Task task2 = new Task("Walk the dog", "The dog walks at 9 a.m.");

        ArrayList<Task> expected = new ArrayList<>();
        expected.add(task);
        expected.add(task2);

        historyManager.add(task);
        task.setDescription("The dog walks at 9 a.m.");
        historyManager.add(task);

        ArrayList<Task> actual = historyManager.getHistory();

        assertEquals(expected, actual, "Списки не равны");

    }

    @Test
    public void shouldNotSurpassTenItemsInList() {
        for (int i = 0; i < 12; i++) {
            Task task = new Task("Task", "Description");
            historyManager.add(task);
        }
        assertEquals(10, historyManager.getHistory().size(), "Размер списка превышает 10 объектов.");

    }

    @Test
    public void shouldDeleteOldAndAddNewTaskWhenListHasTen() {

        for (int i = 1; i <= 11; i++) {
            Task task = new Task("Task_" + i, "Description_" + i);
            historyManager.add(task);
        }

        ArrayList<Task> expected = new ArrayList<>();
        for (int i = 2; i <= 11; i++) {
            Task task = new Task("Task_" + i, "Description_" + i);
            expected.add(task);
        }
        assertEquals(expected, historyManager.getHistory(), "Списки не равны");

    }


}