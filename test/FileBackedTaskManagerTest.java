import org.junit.jupiter.api.Test;
import taskmanagement.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    @Test
    public void shouldSaveTasksToFile() throws IOException {

        File tasks = File.createTempFile("backup-copy", ".csv");
        File history = File.createTempFile("backup-copy", ".csv");

        TaskManager manager = Managers.getFileBacked(tasks, history);


        Task t1 = new Task("Walk the dog", "The dog walks at 8");
        Task t2 = new Task("Go shopping", "Buy veggies");
        manager.createTask(t1);
        manager.createTask(t2);
        int counter;
        String task1;

        try {
            final List<String> lines = Files.readAllLines(tasks.toPath(), StandardCharsets.UTF_8);
            counter = lines.size();
            task1 = lines.get(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String expected = "1,TASK,Walk the dog,NEW,The dog walks at 8,null,0";

        tasks.deleteOnExit();
        history.deleteOnExit();

        assertEquals(3, counter);
        assertEquals(expected, task1);


    }

    @Test
    public void shouldSaveHistoryToFile() throws IOException {

        File tasks = File.createTempFile("backup-copy", ".csv");
        File history = File.createTempFile("backup-copy", ".csv");

        TaskManager manager = Managers.getFileBacked(tasks, history);

        Task t1 = new Task("Walk the dog", "The dog walks at 8");

        manager.createTask(t1);
        manager.findTaskById(1);

        int counter = 0;

        try {
            final List<String> lines = Files.readAllLines(history.toPath(), StandardCharsets.UTF_8);
            counter = lines.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tasks.deleteOnExit();
        history.deleteOnExit();

        assertEquals(2, counter);


    }

    @Test
    public void shouldRestoreTasksFromFile() throws IOException {

        File tasks = File.createTempFile("backup-copy", ".csv");
        File history = File.createTempFile("backup-copy", ".csv");

        TaskManager manager = Managers.getFileBacked(tasks, history);


        Task t1 = new Task("Walk the dog", "The dog walks at 8");
        manager.createTask(t1);

        TaskManager manager2 = Managers.getFileBacked(tasks, history);


        Task task = manager2.findTaskById(1);
        String name = task.getName();
        String expected = "Walk the dog";

        tasks.deleteOnExit();
        history.deleteOnExit();

        assertEquals(expected, name);
    }

    @Test
    public void shouldRestoreTasksWithTimeFromFile() throws IOException {

        File tasks = File.createTempFile("backup-copy", ".csv");
        File history = File.createTempFile("backup-copy", ".csv");

        TaskManager manager = Managers.getFileBacked(tasks, history);

        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);
        manager.createTask(t2);

        TaskManager manager2 = Managers.getFileBacked(tasks, history);

        Task task = manager2.findTaskById(1);
        String expected = "01.03.24 19:00";
        String actual = task.getStartTime().format(dateTimeFormatter);

        tasks.deleteOnExit();
        history.deleteOnExit();

        assertEquals(expected, actual);


    }

    @Test
    public void shouldRestoreHistoryFromFile() throws IOException {

        File tasks = File.createTempFile("backup-copy", ".csv");
        File history = File.createTempFile("backup-copy", ".csv");

        TaskManager manager = Managers.getFileBacked(tasks, history);

        Task t1 = new Task("Walk the dog", "The dog walks at 8");
        manager.createTask(t1);
        manager.findTaskById(1);


        TaskManager manager2 = Managers.getFileBacked(tasks, history);

        List<Task> recoveredHistory = manager2.getHistory();
        String name = "";
        for (Task task : recoveredHistory) {
            name = task.getName();
        }
        String expected = "Walk the dog";

        tasks.deleteOnExit();
        history.deleteOnExit();

        assertEquals(expected, name);
    }


}