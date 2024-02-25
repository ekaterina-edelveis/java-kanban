package taskmanagement;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();

    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    protected int counter = 1;

    @Override
    public int createTask(Task task) {
        task.setId(counter);
        task.setType(TaskType.TASK);
        tasks.put(task.getId(), task);
        counter++;
        return task.getId();
    }

    @Override
    public int createEpic(Epic epic) {
        epic.setId(counter);
        epic.setType(TaskType.EPIC);
        epics.put(epic.getId(), epic);
        counter++;

        return epic.getId();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        subtask.setId(counter);
        subtask.setType(TaskType.SUBTASK);
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
        return new ArrayList<>(epics.get(epicId).getSubtasks());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task findTaskById(int id) {

        if(!tasks.containsKey(id)){
            System.out.println("Задача с таким ID не обнаружена.");
            return null;
        }
        else {
            Task foundTask = tasks.get(id);
            historyManager.add(foundTask);
            return foundTask;
        }
    }

    @Override
    public Epic findEpicById(int id) {
        if(!epics.containsKey(id)){
            System.out.println("Эпик с таким ID не обнаружен.");
            return null;
        }
        else {
            Epic foundEpic = epics.get(id);
            historyManager.add(foundEpic);
            return foundEpic;
        }
    }

    @Override
    public Subtask findSubtaskById(int id) {
        if(!subtasks.containsKey(id)){
            System.out.println("Подзадача с таким ID не обнаружена.");
            return null;
        }
        else {
            Subtask foundSubtask = subtasks.get(id);
            historyManager.add(foundSubtask);
            return foundSubtask;
        }
    }

    @Override
    public void deleteAllTasks() {
        historyManager.removeAll(tasks.values());

        tasks.clear();

    }

    @Override
    public void deleteAllEpics() {

        Collection<Task> epicsAndSubtasks = new ArrayList<>();
        epicsAndSubtasks.addAll(epics.values());
        epicsAndSubtasks.addAll(subtasks.values());
        historyManager.removeAll(epicsAndSubtasks);

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }


    @Override
    public void deleteEpicById(int id) {
        Epic epicToBeDeleted = epics.get(id);

        for (Subtask subtask : epicToBeDeleted.getSubtasks()) {
            subtasks.remove(subtask.getId());
        }


        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);

        //нужно удалить задачу из arraylist эпика
        Epic epicToBeUpdated = subtask.getEpic();
        epicToBeUpdated.getSubtasks().remove(subtask);

        //удаляем подзадачу из hashmap
        subtasks.remove(id);
        historyManager.remove(id);

        //проверяем статус эпика
        calculateEpicStatus(epicToBeUpdated);

    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public TreeMap<Integer, Task> sort(){
        TreeMap<Integer, Task> sortedTasks = new TreeMap<>();
        sortedTasks.putAll(tasks);
        sortedTasks.putAll(epics);
        sortedTasks.putAll(subtasks);

        return sortedTasks;
    }
}
