package taskmanagement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();

    protected final Set<Task> prioritizedTasks = new TreeSet<>();

    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    protected int counter = 1;

    public boolean isOverlap(LocalDateTime newStart, LocalDateTime newEnd, Task existingTask) {
        return !(newEnd.isBefore(existingTask.getStartTime())
                || newStart.isAfter(existingTask.getEndTime()));
    }

    public boolean findSlots(LocalDateTime newStart, LocalDateTime newEnd) {
        return prioritizedTasks.stream().noneMatch(t -> isOverlap(newStart, newEnd, t));
    }


    /*
    Анна, привет! В методдах создания/обновления задач и подзадач
    у меня дублируется много кода в if-else, так же быть не должно?
    Просто я не понимаю, как можно сделать код более лаконичным,
    не обложывщись подушками доп условий, спасающих от исключений
     */
    @Override
    public void createTask(Task task) {
        try {
            if (task.getStartTime() == null) {
                task.setId(counter);
                task.setType(TaskType.TASK);
                tasks.put(task.getId(), task);
                counter++;
            } else if (prioritizedTasks.isEmpty()
                    || findSlots(task.getStartTime(), task.getEndTime())) {
                task.setId(counter);
                task.setType(TaskType.TASK);
                tasks.put(task.getId(), task);
                prioritizedTasks.add(task);
                counter++;
            } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");
        } catch (ManagerSaveException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(counter);
        epic.setType(TaskType.EPIC);
        epics.put(epic.getId(), epic);

        counter++;
    }

    @Override
    public void createSubtask(Subtask subtask) {

        try {
            if (subtask.getStartTime() == null) {
                subtask.setId(counter);
                subtask.setType(TaskType.SUBTASK);
                counter++;

                Epic epicToBeUpdated = subtask.getEpic();
                epicToBeUpdated.getSubtasks().add(subtask);

                subtasks.put(subtask.getId(), subtask);

                //вычисляем статус эпика
                calculateEpicStatus(epicToBeUpdated);
                calculateEpicStartTime(epicToBeUpdated);
                calculateEpicDuration(epicToBeUpdated);


            } else if (prioritizedTasks.isEmpty()
                    || findSlots(subtask.getStartTime(), subtask.getEndTime())) {

                subtask.setId(counter);
                subtask.setType(TaskType.SUBTASK);
                counter++;

                Epic epicToBeUpdated = subtask.getEpic();
                epicToBeUpdated.getSubtasks().add(subtask);

                subtasks.put(subtask.getId(), subtask);
                prioritizedTasks.add(subtask);

                //вычисляем статус эпика
                calculateEpicStatus(epicToBeUpdated);
                calculateEpicStartTime(epicToBeUpdated);
                calculateEpicDuration(epicToBeUpdated);


            } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");
        } catch (ManagerSaveException ex) {
            System.out.println(ex.getMessage());
        }
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
        calculateEpicStartTime(epicToBeUpdated);
        calculateEpicDuration(epicToBeUpdated);

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
        calculateEpicStatus(epicToBeUpdated);
        calculateEpicStartTime(epicToBeUpdated);
        calculateEpicDuration(epicToBeUpdated);

    }

    @Override
    public void updateTaskTime(Task task, String start, long duration) {

        LocalDateTime tryStart = LocalDateTime.parse(start, task.DATE_TIME_FORMATTER);
        LocalDateTime tryEnd = tryStart.plusMinutes(duration);

        try {
            if (prioritizedTasks.isEmpty()) {
                task.setStartTime(start);
                task.setDuration(duration);
                updateTask(task);
                prioritizedTasks.add(task);
            } else if (findSlots(tryStart, tryEnd)) {
                prioritizedTasks.remove(task);
                task.setStartTime(start);
                task.setDuration(duration);
                updateTask(task);
                prioritizedTasks.add(task);
            } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");
        } catch (ManagerSaveException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void updateSubtaskTime(Subtask subtask, String start, long duration) {

        LocalDateTime tryStart = LocalDateTime.parse(start, subtask.DATE_TIME_FORMATTER);
        LocalDateTime tryEnd = tryStart.plusMinutes(duration);

        try {
            if (prioritizedTasks.isEmpty()) {
                subtask.setStartTime(start);
                subtask.setDuration(duration);
                updateSubtask(subtask);
                prioritizedTasks.add(subtask);

            } else if (findSlots(tryStart, tryEnd)) {

                prioritizedTasks.remove(subtask);
                subtask.setStartTime(start);
                subtask.setDuration(duration);
                updateSubtask(subtask);
                prioritizedTasks.add(subtask);

            } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");
        } catch (ManagerSaveException ex) {
            System.out.println(ex.getMessage());
        }

    }

    protected void calculateEpicStartTime(Epic epic) {

        Optional<LocalDateTime> epicStart = epic.getSubtasks().stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);

        if (epicStart.isPresent()) {
            String start = epicStart.get().format(epic.DATE_TIME_FORMATTER);
            epic.setStartTime(start);
        }

    }

    protected void calculateEpicDuration(Epic epic) {
        List<Duration> subsDuration = epic.getSubtasks().stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        long minutesTotal = 0;
        if (!subsDuration.isEmpty()) {
            for (Duration duration : subsDuration) {
                minutesTotal += duration.toMinutes();
            }
            epic.setDuration(minutesTotal);
        }
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
        calculateEpicStatus(epicToBeUpdated);
        calculateEpicStartTime(epicToBeUpdated);
        calculateEpicDuration(epicToBeUpdated);


    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public TreeMap<Integer, Task> sort() {
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
