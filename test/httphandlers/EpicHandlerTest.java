package httphandlers;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.Epic;
import taskmanagement.Managers;
import taskmanagement.Subtask;
import taskmanagement.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class EpicHandlerTest {

    private HttpServer httpServer;
    private TaskManager manager;
    private EpicHandler handler;
    private HttpClient client;
    private static final int PORT = 8080;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        handler = new EpicHandler(manager);
        httpServer.createContext("/epics", handler);
        client = HttpClient.newHttpClient();
        httpServer.start();
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(2);
    }


    @Test
    public void shouldFindSubtasksForEpic() {
        Epic epic = new Epic("Sunday quehaceros", "casual stuff");
        int epicId = manager.createEpic(epic);

        Subtask sub = new Subtask("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45, manager.findEpicById(epicId));
        int subId = manager.createSubtask(sub);


        String url = "http://localhost:8080/epics/" + epicId + "/subtasks";
        URI uri = URI.create(url);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        HttpRequest request = requestBuilder
                .GET()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        try {
            HttpResponse<String> response = client.send(request, handler);
            int expectedCode = 200;
            String expectedBody = "{\n" +
                    "  \"epic\": {\n" +
                    "    \"name\": \"Sunday quehaceros\",\n" +
                    "    \"description\": \"casual stuff\",\n" +
                    "    \"id\": 1,\n" +
                    "    \"status\": \"NEW\",\n" +
                    "    \"type\": \"EPIC\"\n" +
                    "  },\n" +
                    "  \"name\": \"cook dinner\",\n" +
                    "  \"description\": \"pasta with meatballs\",\n" +
                    "  \"id\": 2,\n" +
                    "  \"status\": \"NEW\",\n" +
                    "  \"type\": \"SUBTASK\",\n" +
                    "  \"duration\": 45,\n" +
                    "  \"startTime\": \"01.03.24 19:00\"\n" +
                    "}";

            assertNotNull(response.body());
            assertEquals(expectedBody, response.body());
            assertEquals(expectedCode, response.statusCode());

        } catch (IOException | InterruptedException e) {
        }

    }

}