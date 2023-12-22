package taskmanagement;

import taskmanagement.Status;

public class Subtask extends Task {

    protected Epic epic;

    public Subtask(String name, String description) {
        super(name, description);
    }

    protected Epic getEpic() {
        return epic;
    }

    protected void setEpic(Epic epic) {
        this.epic = epic;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
