import taskmanagement.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        //TaskManager manager = Managers.getFileBacked(new File("backup.csv"), new File("backupHistory.csv"));

        TaskManager manager = Managers.getDefault();

        Task t1 = new Task("bla", "blabla", "28.02.24 19:30", 100);
        Task t2 = new Task("blu", "blublu", "25.02.24 18:00", 60);
        Task t3 = new Task("bli", "blibli", "20.02.24 10:00", 90);

        manager.createTask(t1);
        manager.createTask(t2);
        manager.createTask(t3);

        Task task = manager.findTaskById(1);
        //LocalDateTime time = LocalDateTime.of(2024, Month.FEBRUARY, 24, 15, 0);
        //task.setStartTime(time);
        task.setStartTime("01.01.24 15:00");
        manager.updateTask(task);

        List<Task> priorities = manager.getPrioritizedTasks();
        priorities.forEach(System.out::println);


    }
}
