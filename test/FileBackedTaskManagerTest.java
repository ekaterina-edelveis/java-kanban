import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.Managers;
import taskmanagement.Task;
import taskmanagement.TaskManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    public void beforeEach() throws IOException {
        manager = Managers.getFileBacked(new File("temp/backup-copy.csv"),
                new File("temp/backup-history-copy.csv"));
    }
    
    @Test
    public void shouldLoadFromEmptyFile(){
        File file = Paths.get("temp/backup-copy.csv").toFile();
        long backupLength = file.length();
        assertEquals(0, backupLength);
    }

    @Test
    public void shouldSaveTasksToFile(){
        Task t1 = new Task("Walk the dog", "The dog walks at 8");
        Task t2 = new Task("Go shopping", "Buy veggies");
        manager.createTask(t1);
        manager.createTask(t2);
        int counter = 0;

        try (BufferedReader reader =
                     new BufferedReader(new FileReader("temp/backup-copy.csv", StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                counter++;
                reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("Произошла ошибка во время чтения файла.");
        }

        assertEquals(3, counter);

    }

    @Test
    public void shouldSaveHistoryToFile(){
        Task t1 = new Task("Walk the dog", "The dog walks at 8");
        manager.createTask(t1);
        manager.findTaskById(1);

        int counter = 0;
        try (BufferedReader reader =
                     new BufferedReader(new FileReader("temp/backup-history-copy.csv", StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                counter++;
                reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("Произошла ошибка во время чтения файла.");
        }

        assertEquals(2, counter);


    }

    @Test
    public void shouldRestoreTasksFromFile(){
        Task task = manager.findTaskById(1);
        String name = task.getName();
        String expected = "Walk the dog";

        assertEquals(expected, name);
    }

    @Test
    public void shouldRestoreHistoryFromFile(){
        List<Task> history = manager.getHistory();
        String name = "";
        for(Task task : history){
           name = task.getName();
        }
        String expected = "Walk the dog";
        assertEquals(expected, name);
    }



}