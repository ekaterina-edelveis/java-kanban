package taskmanagement;

import java.util.List;

public interface TaskManager {

    int createTask(Task task);

    int createEpic(Epic epic);

    int createSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasksForEpic(int epicId);

    List<Subtask> getAllSubtasks();

    Task findTaskById(int id);

    Epic findEpicById(int id);

    Subtask findSubtaskById(int id);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubtaskById(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
