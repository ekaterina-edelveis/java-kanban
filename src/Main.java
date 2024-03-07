import taskmanagement.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        //TaskManager manager = Managers.getFileBacked(new File("backup.csv"), new File("backupHistory.csv"));

        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic("do the project", "add new feature");
        manager.createEpic(epic);

        Subtask t1 = new Subtask("bla", "blabla", "28.02.24 19:30", 100, epic);
        Subtask t2 = new Subtask("blu", "blublu", "25.02.24 18:00", 60, epic);
        Subtask t3 = new Subtask("bli", "blibli", "20.02.24 10:00", 90, epic);

        manager.createSubtask(t1);
        manager.createSubtask(t2);
        manager.createSubtask(t3);


        try {
            t2.setStartTime("20.02.24 11:00");
            t2.setStatus(Status.IN_PROGRESS);
            manager.updateSubtask(t2);
        }
        catch (ManagerSaveException ex){}

        List<Task> priorities = manager.getPrioritizedTasks();
        priorities.forEach(System.out::println);
        System.out.println(epic.getStatus());

        List<Subtask> subs = manager.getAllSubtasks();
        subs.forEach(System.out::println);

    }
}
