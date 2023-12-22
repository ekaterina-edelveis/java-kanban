package taskmanagement;

import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {

    /*я подумала, что лучший способ привязать подзадачи к эпику,
    это создать ArrayList подзадач для каждого эпика.
    Но при работе с подзадачами и эпиком это, конечно, пляски с бубном :)

     */
    protected ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }

    protected ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
