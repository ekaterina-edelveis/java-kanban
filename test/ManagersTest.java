import org.junit.jupiter.api.Test;
import taskmanagement.*;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    public void createFunctionalTaskManager(){
        TaskManager manager = Managers.getDefault();

        //видимо, здесь как раз стоит проверить на NPE? если менеджер не создался, оно и выбросится
        assertNotNull(manager.getAllTasks());

    }

    @Test
    public void createFunctionalHistoryManager(){
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager.getHistory());

    }

}