package taskmanagement;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private final File backupFile;
    private final File backupHistory;

    public FileBackedTaskManager(File file, File history) {
        this.backupFile = file;
        this.backupHistory = history;
        // loadFromFile(file, history);

    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;

    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();

        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {

        super.updateSubtask(subtask);
        save();

    }

    @Override
    protected void calculateEpicStatus(Epic epic) {
        super.calculateEpicStatus(epic);

    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return super.getAllTasks();
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return super.getAllEpics();
    }

    @Override
    public ArrayList<Subtask> getAllSubtasksForEpic(int epicId) {
        return super.getAllSubtasksForEpic(epicId);
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return super.getAllSubtasks();
    }

    @Override
    public Task findTaskById(int id) {
        Task task = super.findTaskById(id);
        saveHistory();
        return task;
    }

    @Override
    public Epic findEpicById(int id) {
        Epic epic = super.findEpicById(id);
        saveHistory();
        return epic;
    }

    @Override
    public Subtask findSubtaskById(int id) {
        Subtask subtask = super.findSubtaskById(id);
        saveHistory();
        return subtask;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    private void save() {
        TreeMap<Integer, Task> sortedTasks = super.sort();

        try (FileWriter writer = new FileWriter(backupFile, StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : sortedTasks.values()) {
                writer.write(task.toCvs() + "\n");
            }

        } catch (IOException ex) {
            throw new ManagerSaveException("Произошла ошибка во время чтения файла менеджера.");
        }

    }

    private void saveHistory() {
        String history = historyToString();

        try (FileWriter writer = new FileWriter(backupHistory, StandardCharsets.UTF_8)) {
            writer.write(history);
        } catch (IOException ex) {
            throw new ManagerSaveException("Произошла ошибка во время чтения файла менеджера.");
        }

    }

    private void fromString(String value) {
        String[] taskData = value.split(",");
        int id = Integer.parseInt(taskData[0]);
        TaskType type = TaskType.valueOf(taskData[1]);
        String name = taskData[2];
        Status status = Status.valueOf(taskData[3]);
        String description = taskData[4];

        if (type == TaskType.TASK) {
            Task task = new Task(name, description);
            task.setId(id);
            task.setType(type);
            task.setStatus(status);

            tasks.put(task.getId(), task);

        } else if (type == TaskType.EPIC) {
            Epic task = new Epic(name, description);

            task.setId(id);
            task.setType(type);
            task.setStatus(status);

            epics.put(task.getId(), task);

        } else if (type == TaskType.SUBTASK) {
            int epicId = Integer.parseInt(taskData[5]);

            Epic epic = super.epics.get(epicId);
            Subtask task = new Subtask(name, description, epic);
            task.setId(id);
            task.setType(type);
            task.setStatus(status);

            subtasks.put(task.getId(), task);
            epic.getSubtasks().add(task);
        }


    }

    public void setIdCounter(String line) {

        String[] data = line.split(",");
        super.counter = Integer.parseInt(data[0]) + 1;

    }


    private String historyToString() {
        List<Task> history = super.getHistory();
        StringBuilder builder = new StringBuilder();
        builder.append("id\n");
        for (Task task : history) {
            builder.append(task.getId() + ",");
        }

        builder.setLength(builder.length() - 1);


        return builder.toString();
    }


    private void historyFromString(String value) {

        String[] recoveredTasks = value.split(",");

        for (String idValue : recoveredTasks) {
            int id = Integer.parseInt(idValue);
            if (super.tasks.containsKey(id)) {
                Task task = super.tasks.get(id);
                super.historyManager.add(task);
            } else if (super.epics.containsKey(id)) {
                Epic epic = super.epics.get(id);
                super.historyManager.add(epic);
            } else {
                Subtask subtask = super.subtasks.get(id);
                super.historyManager.add(subtask);
            }
        }

    }


    public static FileBackedTaskManager loadFromFile(File file, File history) {

        FileBackedTaskManager manager = new FileBackedTaskManager(file, history);

        if (file.length() != 0) {
            try (BufferedReader reader =
                         new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String lastLine = "";

                reader.readLine();
                while (reader.ready()) {
                    String line = reader.readLine();
                    manager.fromString(line);
                    lastLine = line;
                }
                manager.setIdCounter(lastLine);

            } catch (IOException e) {
                throw new ManagerSaveException("Произошла ошибка во время чтения файла менеджера.");

            }
        }

        if (history.length() != 0) {
            try (BufferedReader historyReader = new BufferedReader(new FileReader(history, StandardCharsets.UTF_8))) {
                historyReader.readLine();
                String ids = historyReader.readLine();
                manager.historyFromString(ids);


            } catch (IOException e) {
                throw new ManagerSaveException("Произошла ошибка во время чтения файла менеджера.");
            }
        }
        return manager;
    }
}

