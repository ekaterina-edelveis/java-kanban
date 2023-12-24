package taskmanagement;

import taskmanagement.Status;

public class Subtask extends Task {

    protected Epic epic;

    //добавмла привязку к эпику в конструктор

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    protected Epic getEpic() {
        return epic;
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
