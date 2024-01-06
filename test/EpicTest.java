import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.Epic;
import taskmanagement.Managers;
import taskmanagement.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private static TaskManager manager;

    @BeforeEach
    public void beforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    public void treatsEpicsWithSameIdAsEqual() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Epic savedEpic = manager.findEpicById(epicId);

        epic.setDescription("Upgrade the kanban");
        manager.updateEpic(epic);
        assertEquals(savedEpic, manager.findEpicById(epicId));

    }

        /*
    В т/з написано, нужно убедиться, что объект Epic нельзя добавить к себе к подзадачи.
    Но как это тестировать: метода setSubtask нет,
    а при создании подзадачи manager принимает только объект класса Subtask
     */


}