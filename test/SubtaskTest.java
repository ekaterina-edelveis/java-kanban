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
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Add new functionality",
                "Create interfaces", "27.02.24 19:30", 90, epic);
        manager.createSubtask(subtask);

        Subtask savedSubtask = manager.findSubtaskById(subtask.getId());
        subtask.setDescription("Create interfaces and split classes");
        manager.updateSubtask(subtask);

        assertEquals(savedSubtask, manager.findSubtaskById(subtask.getId()));

    }
}