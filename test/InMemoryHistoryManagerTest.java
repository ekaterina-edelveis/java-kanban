import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();

    }

    @Test
    public void shouldAddTasksToHistory() {
        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");

        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();

        //поправила проверку списка
        assertNotEquals(0, history.size(), "История пустая.");
        assertEquals(1, history.size(), "История пустая.");
    }


    @Test
    public void shouldSaveAllSearchedForTasks() {

        //поправила тест добавления всех задач, которые искал польователь

        Task task = new Task("Walk the dog", "The dog walks at 8 a.m.");
        historyManager.add(task);
        historyManager.add(task);

        List<Task> expected = new ArrayList<>();
        expected.add(task);
        expected.add(task);

        List<Task> actual = historyManager.getHistory();

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