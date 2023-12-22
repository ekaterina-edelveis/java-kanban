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
        Subtask subtask1ForEpic1 = new Subtask("Buy a present", "She likes video games.");
        manager.createEpic(epic1, subtask1ForEpic1);

        Epic epic2 = new Epic("Do the project", "Create a kanban for Yandex Practicum");
        Subtask subtask1ForEpic2 = new Subtask("Clone GitHub repo!",
                "The link is provided in the project description");
        Subtask subtask2ForEpic2 = new Subtask("Create necessary classes...",
                "Create all classes with relevant methods");
        Subtask subtask3ForEpic2 = new Subtask("Test the project",
                "Create several objects, analyze their behaviour, verify all methods.");
        ArrayList<Subtask> subtasksForEpic2 = new ArrayList<>();
        subtasksForEpic2.add(subtask1ForEpic2);
        subtasksForEpic2.add(subtask2ForEpic2);
        subtasksForEpic2.add(subtask3ForEpic2);
        manager.createEpicWithMultipleSubtasks(epic2, subtasksForEpic2);


        System.out.println("Let's test it!");




        Subtask updatedSubtask7 = new Subtask("Clone GitHub repo",
                "The link is provided in the project description");
        manager.updateSubtask(7, updatedSubtask7, Status.DONE);


        System.out.println("-------Getting all subtasks by epic id");
        ArrayList<Subtask> subtasks = manager.getAllSubtasksForEpic(6);
        for(Subtask subtask : subtasks){
            System.out.println(subtask.toString());
        }
        System.out.println("-------Finding epic by id");
        Epic epicCheck1 = manager.findEpicById(6);
        System.out.println(epicCheck1.toString());


        Subtask updatedSubtask8 = new Subtask("Create necessary classes",
                "Create all classes with relevant methods");
        manager.updateSubtask(8, updatedSubtask8, Status.DONE);

        Subtask updatedSubtask9 = new Subtask("Test the project",
                "Create several objects, analyze their behaviour, verify all methods");
        manager.updateSubtask(9, updatedSubtask9, Status.DONE);


        System.out.println("-------Getting all subtasks by epic id");
        ArrayList<Subtask> subtasks3 = manager.getAllSubtasksForEpic(6);
        for(Subtask subtask : subtasks3){
            System.out.println(subtask.toString());
        }
        System.out.println("-------Finding epic by id");
        Epic epicCheck3 = manager.findEpicById(6);
        System.out.println(epicCheck3.toString());


        Task taskToUpdate = new Task("Read a book", "Read at least 20 pages");
        manager.updateTask(3, taskToUpdate, Status.DONE);


        System.out.println("-------Getting all tasks");
        ArrayList<Task> tasks = manager.getAllTasks();
        for(Task task : tasks){
            System.out.println(task.toString());
        }

        System.out.println("-------Getting all epics");
        ArrayList<Epic> epics = manager.getAllEpics();
        for(Epic epic : epics){
            System.out.println(epic.toString());
        }

        System.out.println("-------Getting all subtasks by epic id");
        ArrayList<Subtask> checkSubtasks = manager.getAllSubtasksForEpic(6);
        for(Subtask subtask : checkSubtasks){
            System.out.println(subtask.toString());
        }

        System.out.println("-------Finding task by id");
        Task task = manager.findTaskById(2);
        System.out.println(task.toString());

        System.out.println("-------Finding epic by id");
        Epic epic = manager.findEpicById(4);
        System.out.println(epic.toString());

        System.out.println("-------Finding subtask by id");
        Subtask subtask = manager.findSubtaskById(8);
        System.out.println(subtask.toString());

        System.out.println("-------Deleting task");
        manager.deleteTaskById(1);

        System.out.println("-------Deleting subtask");
        manager.deleteSubtaskById(8);


        System.out.println("-------Deleting epic");
        manager.deleteEpicById(4);


        System.out.println("-------Deleting all tasks and epics");
        manager.deleteAllTasks();
        manager.deleteAllEpics();


        System.out.println("-------Getting all tasks");
        ArrayList<Task> tasks2 = manager.getAllTasks();
        for(Task newTask : tasks2){
            System.out.println(newTask.toString());
        }

        System.out.println("-------Getting all epics");
        ArrayList<Epic> epics2 = manager.getAllEpics();
        for(Epic newEpic : epics2){
            System.out.println(newEpic.toString());
        }








    }
}
