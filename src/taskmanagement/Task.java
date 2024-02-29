package taskmanagement;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task implements Comparable<Task> {

    protected String name;
    protected String description;
    protected int id;
    protected Status status;
    protected TaskType type;
    protected Duration duration;
    protected LocalDateTime startTime;

    final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }

    public Task(String name, String description, String start, long minutes) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        this.startTime = LocalDateTime.parse(start, DATE_TIME_FORMATTER);
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
        return startTime.plusMinutes(duration.toMinutes());
    }

    public void setDuration(long minutes) {
        this.duration = Duration.ofMinutes(minutes);
    }

    public void setStartTime(String start) {
        this.startTime = LocalDateTime.parse(start, DATE_TIME_FORMATTER);
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
        if (startTime == null && duration == null){
            return id + "," + type + "," + name + "," + status + ","
                    + description + "," + getStartTime()
                    + "," + "0";
        }
        return id + "," + type + "," + name + "," + status + "," + description + ","
                + startTime.format(DATE_TIME_FORMATTER) + "," + duration.toMinutes();
    }


    @Override
    public int compareTo(Task task) {
        return this.getStartTime().compareTo(task.getStartTime());
    }

}
