import taskmanagement.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        //TaskManager manager = Managers.getFileBacked(new File("backup.csv"), new File("backupHistory.csv"));

        TaskManager manager = Managers.getDefault();

    }
}
