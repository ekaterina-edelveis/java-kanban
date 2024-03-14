package taskmanagement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task implements Cloneable {

    protected String name;
    protected String description;
    protected int id;
    protected Status status;
    protected TaskType type;
    protected Duration duration;
    protected LocalDateTime startTime;

    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }

    public Task(String name, String description, String start, long minutes) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        this.startTime = LocalDateTime.parse(start, dateTimeFormatter);
        this.duration = Duration.ofMinutes(minutes);
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    protected void setType(TaskType type) {
        this.type = type;
    }

    protected TaskType getType() {
        return type;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime != null) {
            return startTime.plusMinutes(duration.toMinutes());
        }
        return null;
    }

    public void setDuration(long minutes) {
        this.duration = Duration.ofMinutes(minutes);
    }

    public void setStartTime(String start) {
        this.startTime = LocalDateTime.parse(start, dateTimeFormatter);
    }

    public void setStartTime(LocalDateTime time) {
        this.startTime = time;
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
                ", type=" + type +
                ", duration=" + getDuration() +
                ", startTime=" + getStartTime() +
                '}';
    }

    public String toCvs() {
        if (startTime == null && duration == null) {
            return id + "," + type + "," + name + "," + status + ","
                    + description + "," + getStartTime()
                    + "," + "0";
        }
        return id + "," + type + "," + name + "," + status + "," + description + ","
                + startTime.format(dateTimeFormatter) + "," + duration.toMinutes();
    }

    @Override
    public Task clone() {
        try {
            Task clone = (Task) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
