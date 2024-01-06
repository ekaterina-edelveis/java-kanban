package taskmanagement;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int counter = 1;

    @Override
    public int createTask(Task task) {
        task.setId(counter);
        tasks.put(task.getId(), task);
        counter++;
        return task.getId();
    }

    @Override
    public int createEpic(Epic epic) {
        epic.setId(counter);
        epics.put(epic.getId(), epic);
        counter++;

        return epic.getId();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        subtask.setId(counter);
        counter++;

        Epic epicToBeUpdated = subtask.getEpic();
        epicToBeUpdated.getSubtasks().add(subtask);

        subtasks.put(subtask.getId(), subtask);

        //вычисляем статус эпика
        calculateEpicStatus(epicToBeUpdated);

        return subtask.getId();

    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }


    @Override
    public void updateEpic(Epic epic) {
        String newName = epic.getName();
        String newDescription = epic.getDescription();
        Epic epicToBeUpdated = epics.get(epic.getId());
        epicToBeUpdated.setName(newName);
        epicToBeUpdated.setDescription(newDescription);
        calculateEpicStatus(epicToBeUpdated);

    }

    @Override
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

            epic.setStatus(Status.NEW);

        } else if (subtasksStatuses.contains(Status.DONE)
                && !subtasksStatuses.contains(Status.IN_PROGRESS)
                && !subtasksStatuses.contains(Status.NEW)) {

            epic.setStatus(Status.DONE);

        } else {

            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasksForEpic(int epicId) {
        ArrayList<Subtask> requiredSubtasks = new ArrayList<>(epics.get(epicId).getSubtasks());
        return requiredSubtasks;
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task findTaskById(int id) {
        historyManager.add(tasks.get(id));

        return tasks.get(id);
    }

    @Override
    public Epic findEpicById(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Subtask findSubtaskById(int id) {
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }


    @Override
    public void deleteEpicById(int id) {
        Epic epicToBeDeleted = epics.get(id);

        for (Subtask subtask : epicToBeDeleted.getSubtasks()) {
            subtasks.remove(subtask.getId());
        }

        //удаляем эпик
        epics.remove(id);
    }

    @Override
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

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }
}
