import taskmanagement.*;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("Walk the dog", "The dog walks at 8 a.m.");
        Task task2 = new Task("Go shopping", "Buy milk, bread, meat, veggies");
        Task task3 = new Task("Read a book", "Read at least 30 pages");
        int task1Id = manager.createTask(task1);
        int task2Id = manager.createTask(task2);
        int task3Id = manager.createTask(task3);

        Epic epic1 = new Epic("Attend my friend's birthday",
                "The party takes place on Saturday, 5 p.m.");
        int epic1Id = manager.createEpic(epic1);
        Subtask subtask1ForEpic1 = new Subtask("Buy a present", "She likes video games.", epic1);
        int sub1Id = manager.createSubtask(subtask1ForEpic1);

        Epic epic2 = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        int epic2Id = manager.createEpic(epic2);

        Subtask subtask1ForEpic2 = new Subtask("Clone GitHub repo",
                "The link is provided in the project description", epic2);
        Subtask subtask2ForEpic2 = new Subtask("Create necessary classes",
                "Create all classes with relevant methods", epic2);
        Subtask subtask3ForEpic2 = new Subtask("Test the project",
                "Create several objects, analyze their behaviour, verify all methods.", epic2);
        int sub2Id = manager.createSubtask(subtask1ForEpic2);
        int sub3Id = manager.createSubtask(subtask2ForEpic2);
        int sub4Id = manager.createSubtask(subtask3ForEpic2);

        System.out.println("Time for some tests!");


        manager.findEpicById(epic1Id);
        manager.findTaskById(task1Id);
        manager.findSubtaskById(sub4Id);
        manager.findTaskById(task2Id);
        manager.findEpicById(epic2Id);
        manager.findEpicById(epic1Id);
        manager.findSubtaskById(sub1Id);

        manager.deleteTaskById(task2Id);

        System.out.println("Getting history #1");
        List<Task> history = manager.getHistory();
        for(Task task : history){
        System.out.println(task.getName());
        }

        manager.deleteAllTasks();

        System.out.println("Getting history #2");
        List<Task> history2 = manager.getHistory();
        for(Task task : history2){
            System.out.println(task.getName());
        }






    }
}
