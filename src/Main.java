import taskmanagement.*;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = new TaskManager();

        Task task1 = new Task("Walk the dog", "The dog walks at 8 a.m.");
        Task task2 = new Task("Go shopping", "Buy milk, bread, meat, veggies");
        Task task3 = new Task("Read a book", "Read at least 30 pages");
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);

        Epic epic1 = new Epic("Attend my friend's birthday",
                "The party takes place on Saturday, 5 p.m.");
        manager.createEpic(epic1);
        Subtask subtask1ForEpic1 = new Subtask("Buy a present", "She likes video games.", epic1);
        manager.createSubtask(subtask1ForEpic1);

        Epic epic2 = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        manager.createEpic(epic2);

        Subtask subtask1ForEpic2 = new Subtask("Clone GitHub repo",
                "The link is provided in the project description", epic2);
        Subtask subtask2ForEpic2 = new Subtask("Create necessary classes",
                "Create all classes with relevant methods", epic2);
        Subtask subtask3ForEpic2 = new Subtask("Test the project",
                "Create several objects, analyze their behaviour, verify all methods.", epic2);
        manager.createSubtask(subtask1ForEpic2);
        manager.createSubtask(subtask2ForEpic2);
        manager.createSubtask(subtask3ForEpic2);

        System.out.println("Let's test it!");

        epic1.setStatus(Status.DONE);
        manager.updateEpic(epic1);
        System.out.println(manager.findEpicById(4).toString());
        subtask1ForEpic1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1ForEpic1);
        System.out.println(manager.findEpicById(4).toString());


    }
}
