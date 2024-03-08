import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    private TaskManager manager;

    @BeforeEach
    public void beforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    public void treatsSubtasksWithSameIdAsEqual() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Add new functionality",
                "Create interfaces", "27.02.24 19:30", 90, manager.findEpicById(epicId));
        int subId = manager.createSubtask(subtask);

        Subtask savedSubtask = manager.findSubtaskById(subId);
        savedSubtask.setDescription("Create interfaces and split classes");
        manager.updateSubtask(savedSubtask);

        assertEquals(savedSubtask, manager.findSubtaskById(subId));

    }
}