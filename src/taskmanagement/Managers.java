package taskmanagement;

import java.io.File;

public class Managers {

    public static TaskManager getDefault(){
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileBacked(File file, File history){
        return new FileBackedTaskManager(file, history);
    }

    public static HistoryManager getDefaultHistory(){
        return new InMemoryHistoryManager();
    }



}
