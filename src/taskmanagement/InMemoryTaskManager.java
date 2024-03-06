package taskmanagement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();

    protected final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    protected int counter = 1;

    protected boolean isOverlap(LocalDateTime newStart, LocalDateTime newEnd, Task existingTask) {
        return !(newEnd.isBefore(existingTask.getStartTime())
                || newStart.isAfter(existingTask.getEndTime()));
    }

    protected boolean checkSlots(LocalDateTime newStart, LocalDateTime newEnd) {
        return prioritizedTasks.stream().noneMatch(t -> isOverlap(newStart, newEnd, t));
    }


    @Override
    public int createTask(Task task) {

        task.setId(counter);
        task.setType(TaskType.TASK);

        if (task.getStartTime() == null) {
            tasks.put(task.getId(), task);

        } else if (prioritizedTasks.isEmpty()
                || checkSlots(task.getStartTime(), task.getEndTime())) {

            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);

        } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");

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
        Epic epicToBeUpdated = subtask.getEpic();

        if (subtask.getStartTime() == null) {

            subtasks.put(subtask.getId(), subtask);


        } else if (prioritizedTasks.isEmpty()
                || checkSlots(subtask.getStartTime(), subtask.getEndTime())) {

            subtasks.put(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);

        } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");

        epicToBeUpdated.getSubtasks().add(subtask);
        calculateEpicState(epicToBeUpdated);
        counter++;
        return subtask.getId();
    }


    @Override
    public void updateTask(Task task) {

       /*
       Анна, привет! В общем, почему-то TreeSet не видит эту задачу.
       Если вызвать тут же в методе .contains() перед .remove(), выдает false
       Поэтому он ее не удаляет и поэтому ее невозможно обновить
       Но это происходит, только если новое время старта меньше времени старта любой
       из имеющихся задач
       Сюдя по тому, что я поняла из дебаггера, задача не меняется, как объект
       Айдишник тот же, хеш-код тот же...
       Особенности TreeSet? Какой-то хитрый баг? Как это отлаживать?
       Или все-таки в таком случае доп метод UpdateTaskTime() - валидное решение?

        */

        prioritizedTasks.remove(task);

        if (task.getStartTime() == null) {
            tasks.put(task.getId(), task);

        } else if (checkSlots(task.getStartTime(), task.getEndTime())) {

            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);

        } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");

    }


    @Override
    public void updateEpic(Epic epic) {

        String newName = epic.getName();
        String newDescription = epic.getDescription();
        Epic epicToBeUpdated = epics.get(epic.getId());
        epicToBeUpdated.setName(newName);
        epicToBeUpdated.setDescription(newDescription);

        calculateEpicState(epicToBeUpdated);

    }

    @Override
    public void updateSubtask(Subtask subtask) {

        //сначала обновляем задачу в hashmap
        subtasks.put(subtask.getId(), subtask);

        //теперь нам нужно обновить задачу в arraylist соответствующего эпика
        Epic epicToBeUpdated = subtask.getEpic();
        epicToBeUpdated.getSubtasks().remove(subtask);
        epicToBeUpdated.getSubtasks().add(subtask);

        //проверяем статусы подзадач и меняем статус-старт-продолжительность эпика
        calculateEpicState(epicToBeUpdated);

    }


    @Override
    public void updateTaskTime(Task task, String start, long duration) {

        LocalDateTime tryStart = LocalDateTime.parse(start, task.dateTimeFormatter);
        LocalDateTime tryEnd = tryStart.plusMinutes(duration);

        if (checkSlots(tryStart, tryEnd)) {
            prioritizedTasks.remove(task);
            task.setStartTime(start);
            task.setDuration(duration);
            updateTask(task);
            prioritizedTasks.add(task);
        } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");
    }


    @Override
    public void updateSubtaskTime(Subtask subtask, String start, long duration) {

        LocalDateTime tryStart = LocalDateTime.parse(start, subtask.dateTimeFormatter);
        LocalDateTime tryEnd = tryStart.plusMinutes(duration);

        if (checkSlots(tryStart, tryEnd)) {
            prioritizedTasks.remove(subtask);
            subtask.setStartTime(start);
            subtask.setDuration(duration);
            updateSubtask(subtask);
            prioritizedTasks.add(subtask);

        } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");
    }

    protected void calculateEpicState(Epic epic) {
        calculateEpicStartTime(epic);
        calculateEpicDuration(epic);
        calculateEpicStatus(epic);
    }

    protected void calculateEpicStartTime(Epic epic) {

        Optional<LocalDateTime> epicStart = epic.getSubtasks().stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);

        if (epicStart.isPresent()) {
            LocalDateTime start = epicStart.get();
            epic.setStartTime(start);
        }

    }

    protected void calculateEpicDuration(Epic epic) {

        long minutes = epic.getSubtasks().stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .map(Duration::toMinutes)
                .reduce(0L, Long::sum);

        epic.setDuration(minutes);

    }

    protected void calculateEpicStatus(Epic epic) {

        List<Status> subtasksStatuses = epic.getSubtasks().stream()
                .map(subtask -> subtask.getStatus())
                .collect(Collectors.toList());


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
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasksForEpic(int epicId) {
        return new ArrayList<>(epics.get(epicId).getSubtasks());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task findTaskById(int id) {

        if (!tasks.containsKey(id)) {
            System.out.println("Задача с таким ID не обнаружена.");
            return null;
        } else {
            Task foundTask = tasks.get(id);
            historyManager.add(foundTask);
            return foundTask;
        }
    }

    @Override
    public Epic findEpicById(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Эпик с таким ID не обнаружен.");
            return null;
        } else {
            Epic foundEpic = epics.get(id);
            historyManager.add(foundEpic);
            return foundEpic;
        }
    }

    @Override
    public Subtask findSubtaskById(int id) {
        if (!subtasks.containsKey(id)) {
            System.out.println("Подзадача с таким ID не обнаружена.");
            return null;
        } else {
            Subtask foundSubtask = subtasks.get(id);
            historyManager.add(foundSubtask);
            return foundSubtask;
        }
    }

    @Override
    public void deleteAllTasks() {
        historyManager.removeAll(tasks.values());

        for (Task task : tasks.values()) {
            prioritizedTasks.remove(task);
        }

        tasks.clear();

    }

    @Override
    public void deleteAllEpics() {

        Collection<Task> epicsAndSubtasks = new ArrayList<>();
        epicsAndSubtasks.addAll(epics.values());
        epicsAndSubtasks.addAll(subtasks.values());
        historyManager.removeAll(epicsAndSubtasks);

        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
        }

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteTaskById(int id) {
        prioritizedTasks.remove(tasks.get(id));
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
        prioritizedTasks.remove(subtask);

        //проверяем статус эпика
        calculateEpicState(epicToBeUpdated);

    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected TreeMap<Integer, Task> sort() {
        TreeMap<Integer, Task> sortedTasks = new TreeMap<>();
        sortedTasks.putAll(tasks);
        sortedTasks.putAll(epics);
        sortedTasks.putAll(subtasks);

        return sortedTasks;
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }


}
