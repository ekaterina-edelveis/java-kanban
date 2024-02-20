package taskmanagement;

public class Subtask extends Task {

    protected Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }


    public String toCvs(){
        return id + "," + type + "," + name + "," + status + "," + description + "," + epic.getId();
    }

}
