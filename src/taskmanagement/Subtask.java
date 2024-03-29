package taskmanagement;

public class Subtask extends Task {

    protected Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    public Subtask(String name, String description, String start, long minutes, Epic epic) {
        super(name, description, start, minutes);
        this.epic = epic;
    }


    public Epic getEpic() {
        return epic;
    }

    public String toCvs() {
        if (startTime == null && duration == null) {
            return id + "," + type + "," + name + "," + status + ","
                    + description + "," + getStartTime()
                    + "," + "0" + "," + epic.getId();
        }
        return id + "," + type + "," + name + "," + status + ","
                + description + "," + startTime.format(dateTimeFormatter)
                + "," + duration.toMinutes() + "," + epic.getId();
    }

    @Override
    public Subtask clone() {
        return (Subtask) super.clone();
    }

}
