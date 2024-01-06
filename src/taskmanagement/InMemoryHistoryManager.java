package taskmanagement;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private final ArrayList<Task> taskHistory = new ArrayList<>();


    @Override
    public void add(Task task) {
        if (taskHistory.size() == 10){
            taskHistory.remove(0);
        }
        Task newTask = new Task(task.getName(), task.getDescription());
        newTask.setId(task.getId());
        newTask.setStatus(task.getStatus());
        taskHistory.add(taskHistory.size(), newTask);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return taskHistory;
    }
}
