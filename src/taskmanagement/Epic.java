package taskmanagement;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    protected List<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }



}
