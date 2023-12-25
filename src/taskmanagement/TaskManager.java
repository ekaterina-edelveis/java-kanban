package taskmanagement;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private int counter = 1;

    public TaskManager() {
    }

    public void createTask(Task task) {
        task.setId(counter);
        tasks.put(task.getId(), task);
        counter++;
    }

    public void createEpic(Epic epic) {
        epic.setId(counter);
        epics.put(epic.getId(), epic);
        counter++;
    }

    public void createSubtask(Subtask subtask) {
        subtask.setId(counter);
        counter++;

        Epic epicToBeUpdated = subtask.getEpic();
        epicToBeUpdated.getSubtasks().add(subtask);

        subtasks.put(subtask.getId(), subtask);

        //вычисляем статус эпика
        calculateEpicStatus(epicToBeUpdated);

    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }


    public void updateEpic(Epic epic) {
        String newName = epic.getName();
        String newDescription = epic.getDescription();
        Epic epicToBeUpdated = epics.get(epic.getId());
        epicToBeUpdated.setName(newName);
        epicToBeUpdated.setDescription(newDescription);
        calculateEpicStatus(epicToBeUpdated);

    }

    public void updateSubtask(Subtask subtask) {

        //сначала обновляем задачу в hashmap
        subtasks.put(subtask.getId(), subtask);

        //теперь нам нужнообновить задачу в arraylist соответствующего эпика
        Epic epicToBeUpdated = subtask.getEpic();
        epicToBeUpdated.getSubtasks().remove(subtask);
        epicToBeUpdated.getSubtasks().add(subtask);

        //проверяем статусы подзадач и при необходимости меняем статус эпика
        calculateEpicStatus(epicToBeUpdated);
    }

    protected void calculateEpicStatus(Epic epic) {
        ArrayList<Status> subtasksStatuses = new ArrayList<>();
        for (Subtask sub : epic.getSubtasks()) {
            subtasksStatuses.add(sub.getStatus());
        }

        if (subtasksStatuses.isEmpty() || (subtasksStatuses.contains(Status.NEW)
                && !subtasksStatuses.contains(Status.IN_PROGRESS)
                && !subtasksStatuses.contains(Status.DONE))) {

            //убрала обращение к переменным напрямую

            epic.setStatus(Status.NEW);

        } else if (subtasksStatuses.contains(Status.DONE)
                && !subtasksStatuses.contains(Status.IN_PROGRESS)
                && !subtasksStatuses.contains(Status.NEW)) {

            epic.setStatus(Status.DONE);

        } else {

            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasksForEpic(int epicId) {
        ArrayList<Subtask> requiredSubtasks = new ArrayList<>(epics.get(epicId).getSubtasks());
        return requiredSubtasks;
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public Task findTaskById(int id) {
        return tasks.get(id);
    }

    public Epic findEpicById(int id) {
        return epics.get(id);
    }

    public Subtask findSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }


    public void deleteEpicById(int id) {
        Epic epicToBeDeleted = epics.get(id);

        for (Subtask subtask : epicToBeDeleted.getSubtasks()) {
            subtasks.remove(subtask.getId());
        }

        //удаляем эпик
        epics.remove(id);
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);

        //нужно удалить задачу из arraylist эпика
        Epic epicToBeUpdated = subtask.getEpic();
        epicToBeUpdated.getSubtasks().remove(subtask);

        //удаляем подзадачу из hashmap
        subtasks.remove(id);

        //проверяем статус эпика
        calculateEpicStatus(epicToBeUpdated);

    }


}
