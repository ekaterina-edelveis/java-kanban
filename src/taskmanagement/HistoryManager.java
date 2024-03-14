package taskmanagement;


import java.util.Collection;
import java.util.List;

public interface HistoryManager {

    void add(Task task);

    List<Task> getHistory();

    void remove(int id);

    void removeAll(Collection<Task> tasks);


}
