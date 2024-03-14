package taskmanagement;

import java.util.*;

public class Epic extends Task {

    protected List<Subtask> subtasks;

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

    /*
    переопределила методы колинрования, теперь клонирется именно эпик или поздадача в классе Subtask
    Проверила - объекты клонируются правильно, со всеми полями
    Почитала про Cloneable: действительно, больше рисков, чем пользы.. В следующий раз поищу более
    enterprise-grade решение :)
     */

    @Override
    public Epic clone() {
        return (Epic) super.clone();
    }

}

