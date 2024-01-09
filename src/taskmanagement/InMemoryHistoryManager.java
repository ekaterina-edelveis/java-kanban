package taskmanagement;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final ArrayList<Task> taskHistory = new ArrayList<>();


    @Override
    public void add(Task task) {
        if (taskHistory.size() == 10){
            taskHistory.remove(0);
        }
        // убрала создание новой задачи
        taskHistory.add(task);
    }

    @Override
    public List<Task> getHistory() {
        //поправила - теперь метод возвращает копию списка
        return List.copyOf(taskHistory);
    }
}
