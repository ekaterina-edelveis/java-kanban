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


    /*
    Анна, привет! А как насчет такого варианта?
    Мы оставляем сеттеры в задачах, но при этом пользователь может работать только с клонами объектов.
    Т.е. мы сохраняем в мапе клон переданного объекта, только нормальный, с аданным ID, типом. статусом и пр.
    Пользователь получает по методу find только клоны, и обновляет их, а менеджер, получая клон,
    уже проверяет, можно ли обновить задачу. Даже если пользователь попытается передать какой-то левый инпут,
    менеджер проверит, существует ли вообще такая задача.
    Таким образом мы:
    1. Сохраняем привычный интерфейс с сеттерами
    2. Не позволяем пользователю взаимодействовать напрямую с объектами в мапах
    3. Не сильно загружаем пламять. Клоны мы нигде не храним, они нужны нам в моменте, соотв.,
    сборщик мусора их приберет
    3. Не создаем доп неудобств:
    хотя мы учти возможность передачи таска без айдишника,
    такого произойти, по идее, не должно. Пользователь не будет работать с программой
    через Main: будет либо фронт, либо консолька. Соотв., не будет возможности продолжать работу с ранее созданными клонами.

    В иммутабельности в данной ситуации я виду след проблему:
    Нам вместо сеттеров придется вводить какие-то другие методы, потому что в конструкторе
    не все можно передать: айдишник, например, или статус NEW у новой задачи, или статус эпика
     */

    @Override
    public int createTask(Task newTask) {
        Task task = newTask.clone();

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
    public int createEpic(Epic newEpic) {
        Epic epic = (Epic) newEpic.clone();

        epic.setId(counter);
        epic.setType(TaskType.EPIC);
        epics.put(epic.getId(), epic);

        counter++;

        return epic.getId();
    }

    @Override
    public int createSubtask(Subtask newSubtask) {
        Subtask subtask = (Subtask) newSubtask.clone();

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
        epics.put(epicToBeUpdated.getId(), epicToBeUpdated);
        counter++;
        return subtask.getId();
    }


    @Override
    public void updateTask(Task task) {

        if (task.getId() == 0) {
            throw new ManagerSaveException("Передана задача с нулевым ID");
        }


        Task taskToUpdate = tasks.get(task.getId());

        if (task.getStartTime() == null) {
            tasks.put(task.getId(), task);
        } else if ((taskToUpdate.getStartTime() == task.getStartTime()
                && taskToUpdate.getDuration() == task.getDuration())
                || checkSlots(task.getStartTime(), task.getEndTime())) {
            prioritizedTasks.removeIf(t -> t.id == task.getId());
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);

        } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");

    }


    @Override
    public void updateEpic(Epic epic) {

        if (epic.getId() == 0) {
            throw new ManagerSaveException("Передана задача с нулевым ID");
        }

        String newName = epic.getName();
        String newDescription = epic.getDescription();
        Epic epicToBeUpdated = epics.get(epic.getId());
        epicToBeUpdated.setName(newName);
        epicToBeUpdated.setDescription(newDescription);

        calculateEpicState(epicToBeUpdated);

    }

    @Override
    public void updateSubtask(Subtask subtask) {

        if (subtask.getId() == 0) {
            throw new ManagerSaveException("Передана задача с нулевым ID");
        }

        Subtask subToUpdate = subtasks.get(subtask.getId());

        if (subtask.getStartTime() == null) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicUponSubtaskUpdate(subtask);

        } else if ((subToUpdate.getStartTime() == subtask.getStartTime()
                && subToUpdate.getDuration() == subtask.getDuration())
                || checkSlots(subtask.getStartTime(), subtask.getEndTime())) {

            prioritizedTasks.removeIf(s -> s.id == subtask.getId());
            subtasks.put(subtask.getId(), subtask);
            updateEpicUponSubtaskUpdate(subtask);
            prioritizedTasks.add(subtask);

        } else throw new ManagerSaveException("Время выполнения задачи пересекается с другими задачами");


    }

    protected void updateEpicUponSubtaskUpdate(Subtask subtask) {

        Epic epicToBeUpdated = subtask.getEpic();
        epicToBeUpdated.getSubtasks().remove(subtask);
        epicToBeUpdated.getSubtasks().add(subtask);

        calculateEpicState(epicToBeUpdated);
        epics.put(epicToBeUpdated.getId(), epicToBeUpdated);
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
                .map(Task::getStatus)
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
            return foundTask.clone();
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
            return (Epic) foundEpic.clone();
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
            return (Subtask) foundSubtask.clone();
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
