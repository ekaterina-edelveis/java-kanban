package taskmanagement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {

    protected List<Subtask> subtasks;


    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public String toCvs() {
        return id + "," + type + "," + name + "," + status + "," + description;
    }


}

