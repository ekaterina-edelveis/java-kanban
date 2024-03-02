import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private TaskManager manager;

    final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    @BeforeEach
    public void beforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    public void treatsEpicsWithSameIdAsEqual() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);

        Epic savedEpic = manager.findEpicById(epic.getId());

        epic.setDescription("Upgrade the kanban");
        manager.updateEpic(epic);
        assertEquals(savedEpic, manager.findEpicById(epic.getId()));

    }

    @Test
    public void shouldUpdateEpicStatus() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, epic);
        manager.createSubtask(sub);

        sub.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(sub);

        assertEquals(Status.IN_PROGRESS, epic.getStatus());

    }

    @Test
    public void shouldNotCompleteEpicIfUndoneSubtasks() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, epic);
        manager.createSubtask(sub);

        Subtask sub2 = new Subtask("Run tests",
                "create necessary tests", "02.03.24 19:00", 120, epic);
        manager.createSubtask(sub2);

        sub.setStatus(Status.DONE);
        manager.updateSubtask(sub);

        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void shouldCompleteEpicIfAllSubtasksDone() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, epic);
        manager.createSubtask(sub);

        Subtask sub2 = new Subtask("Run tests",
                "create necessary tests", "02.03.24 19:00", 120, epic);
        manager.createSubtask(sub2);

        sub.setStatus(Status.DONE);
        manager.updateSubtask(sub);

        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);

        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    public void shouldSetEpicStartAtEarliestSubtask() {
        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, epic);
        manager.createSubtask(sub);

        Subtask sub2 = new Subtask("Run tests",
                "create necessary tests", "02.03.24 19:00", 120, epic);
        manager.createSubtask(sub2);

        Subtask sub3 = new Subtask("Prepare the grounds",
                "create a new branch", "29.02.24 19:00", 10, epic);
        manager.createSubtask(sub3);

        String expected = "29.02.24 19:00";
        String actual = epic.getStartTime().format(DATE_TIME_FORMATTER);
        assertEquals(expected, actual);

    }

    @Test
    public void shouldCountDurationOfEpic() {

        Epic epic = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic);

        Subtask sub = new Subtask("Add a new feature",
                "create necessary methods", "01.03.24 19:00", 120, epic);
        manager.createSubtask(sub);

        Subtask sub2 = new Subtask("Run tests",
                "create necessary tests", "02.03.24 19:00", 120, epic);
        manager.createSubtask(sub2);

        Subtask sub3 = new Subtask("Prepare the grounds",
                "create a new branch", "29.02.24 19:00", 10, epic);
        manager.createSubtask(sub3);

        long expected = sub.getDuration().toMinutes()
                + sub2.getDuration().toMinutes()
                + sub3.getDuration().toMinutes();

        long actual = epic.getDuration().toMinutes();

        assertEquals(expected, actual);

    }


}