import taskmanagement.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

       //TaskManager manager = Managers.getDefault();


        //здесь основные файлы для работы с менеджером

        TaskManager manager = Managers.getFileBacked(new File("backup.csv"), new File("backupHistory.csv"));




        /*
        Epic epic = new Epic("Prepare for the conference",
                "KubeCon March 19-22");
        manager.createEpic(epic);

        Subtask s1 = new Subtask("Prepare slides",
                "The link to github repo", epic);
        manager.createSubtask(s1);
        Subtask s2 = new Subtask("The talk",
                "Prepare the speech", epic);
        manager.createSubtask(s2);

        Task t1 = new Task("Walk the dog", "The dog walks at 8");
        Task t2 = new Task("Go shopping", "Buy veggies");
        manager.createTask(t1);
        manager.createTask(t2);

        Epic epic2 = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic2);
                Subtask s3 = new Subtask("Add a new feature", "Wrte classes", manager.findEpicById(6));
        manager.createSubtask(s3);


        Subtask sub4 = new Subtask("Run tests", "check functionality", manager.findEpicById(6));
        manager.createSubtask(sub4);

        Subtask sub5 = manager.findSubtaskById(7);
        sub5.setDescription("Write classes");
        manager.updateSubtask(sub5);

        Subtask sub6 = manager.findSubtaskById(2);
        sub6.setStatus(Status.DONE);
        manager.updateSubtask(sub6);

         */











    }
}
