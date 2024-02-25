import taskmanagement.*;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        //здесь основные файлы для работы с менеджером
        TaskManager manager = Managers.getFileBacked(new File("backup.csv"), new File("backupHistory.csv"));


    }
}
