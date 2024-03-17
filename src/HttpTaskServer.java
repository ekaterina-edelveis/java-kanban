
import com.sun.net.httpserver.HttpServer;
import httphandlers.*;
import taskmanagement.*;

import java.io.IOException;
import java.net.InetSocketAddress;


public class HttpTaskServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {

        //TaskManager manager = Managers.getFileBacked(new File("backup.csv"), new File("backupHistory.csv"));
        TaskManager manager = Managers.getDefault();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks", new TaskHandler(manager));
        httpServer.createContext("/epics", new EpicHandler(manager));
        httpServer.createContext("/subtasks", new SubtaskHandler(manager));
        httpServer.createContext("/history", new HistoryHandler(manager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(manager));

        httpServer.start();


    }
}
