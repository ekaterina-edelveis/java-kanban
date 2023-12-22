package taskmanagement;

import java.util.Objects;

public class Task {

    /*
    я выбрала protected для большинства переменных и методов, чтобы
    у наследников и taskmanager был к ним доступ, но не в main из другого пакета, поэтому
    вся работа по созданию, изменению и удалению задач ведется только через taskmanager.
    Это оберегает от самовольного присвоения эпику статуса, например. И прочее.
     */

    protected String name;
    protected String description;
    protected int id;
    protected Status status;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }

    protected String getName() {
        return name;
    }

    protected String getDescription() {
        return description;
    }

    protected int getId() {
        return id;
    }

    protected Status getStatus() {
        return status;
    }

    protected void setId(int id) {
        this.id = id;
    }

    protected void setStatus(Status status) {
        this.status = status;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
