import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;


import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private TaskManager manager;

    @BeforeEach
    public void beforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    public void treatsEpicsWithSameIdAsEqual() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int id = manager.createEpic(epic);

        Epic savedEpic = manager.findEpicById(id);

        savedEpic.setDescription("Upgrade the kanban");
        manager.updateEpic(savedEpic);

        assertEquals(savedEpic, manager.findEpicById(savedEpic.getId()));

    }

    @Test
    public void shouldUpdateEpicStatus() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, manager.findEpicById(epicId));
        int subId = manager.createSubtask(sub);

        Subtask found = manager.findSubtaskById(subId);
        found.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(found);

        assertEquals(Status.IN_PROGRESS, manager.findEpicById(epicId).getStatus());

    }

    @Test
    public void shouldNotCompleteEpicIfUndoneSubtasks() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, manager.findEpicById(epicId));
        int sub1Id = manager.createSubtask(sub);

        Subtask sub2 = new Subtask("Run tests",
                "create necessary tests", "02.03.24 19:00", 120, manager.findEpicById(epicId));
        manager.createSubtask(sub2);

        Subtask found = manager.findSubtaskById(sub1Id);
        found.setStatus(Status.DONE);
        manager.updateSubtask(found);

        assertEquals(Status.IN_PROGRESS, manager.findEpicById(epicId).getStatus());
    }

    @Test
    public void shouldCompleteEpicIfAllSubtasksDone() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, manager.findEpicById(epicId));
        int sub1Id = manager.createSubtask(sub);

        Subtask sub2 = new Subtask("Run tests",
                "create necessary tests", "02.03.24 19:00", 120, manager.findEpicById(epicId));
        int sub2Id = manager.createSubtask(sub2);

        Subtask foundSub1 = manager.findSubtaskById(sub1Id);
        foundSub1.setStatus(Status.DONE);
        manager.updateSubtask(foundSub1);

        Subtask foundSub2 = manager.findSubtaskById(sub2Id);
        foundSub2.setStatus(Status.DONE);
        manager.updateSubtask(foundSub2);

        assertEquals(Status.DONE, manager.findEpicById(epicId).getStatus());
    }

    @Test
    public void shouldSetEpicStartAtEarliestSubtask() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, manager.findEpicById(epicId));
        manager.createSubtask(sub);

        Subtask sub2 = new Subtask("Run tests",
                "create necessary tests", "02.03.24 19:00", 120, manager.findEpicById(epicId));
        manager.createSubtask(sub2);

        Subtask sub3 = new Subtask("Prepare the grounds",
                "create a new branch", "29.02.24 19:00", 10, manager.findEpicById(epicId));
        manager.createSubtask(sub3);

        LocalDateTime expectedDate = LocalDateTime.of(2024,
                Month.FEBRUARY, 29, 19, 0);
        LocalDateTime actualDate = manager.findEpicById(epicId).getStartTime();
        assertEquals(expectedDate, actualDate);

    }

    @Test
    public void shouldCountDurationOfEpic() {

        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epicId = manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, manager.findEpicById(epicId));
        manager.createSubtask(sub);

        Subtask sub2 = new Subtask("Run tests",
                "create necessary tests", "02.03.24 19:00", 120, manager.findEpicById(epicId));
        manager.createSubtask(sub2);

        Subtask sub3 = new Subtask("Prepare the grounds",
                "create a new branch", "29.02.24 19:00", 10, manager.findEpicById(epicId));
        manager.createSubtask(sub3);

        long expected = sub.getDuration().toMinutes()
                + sub2.getDuration().toMinutes()
                + sub3.getDuration().toMinutes();

        long actual = manager.findEpicById(epicId).getDuration().toMinutes();

        assertEquals(expected, actual);

    }


}