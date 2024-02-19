package taskmanagement;

import java.util.ArrayList;

public class Epic extends Task {

    public ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public String toString(){
        return id + "," + type + "," + name + "," + status + "," + description;
    }

}
