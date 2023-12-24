package taskmanagement;

import java.util.ArrayList;

public class Epic extends Task {

    protected ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new ArrayList<>();
    }

    protected ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    /*
    сделала setStatus родителя public, чтобы пользователь мог менять статус задачи и подзадачи,
    но "опустошила" тело этого метода в эпике - вместо этого taskmanager рассчитывает статус эпика,
    обращаясь напряму к его переменной. Надеюсь, правильно мыслю :)
     */

    @Override
    public void setStatus(Status status) {}

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
