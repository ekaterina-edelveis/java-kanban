package taskmanagement;


import java.util.*;

public class Epic extends Task {

    protected transient List<Subtask> subtasks;

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

    @Override
    public Epic clone() {
        return (Epic) super.clone();
    }

}

