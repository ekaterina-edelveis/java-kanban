package httphandlers;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskHandlerTest {
    private HttpServer httpServer;
    private TaskManager manager;
    private SubtaskHandler handler;
    private HttpClient client;
    private static final int PORT = 8080;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        handler = new SubtaskHandler(manager);
        httpServer.createContext("/subtasks", handler);
        client = HttpClient.newHttpClient();
        httpServer.start();
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(2);
    }


    @Test
    public void shouldCreateSubtask() {
        Epic epic = new Epic("Sunday quehaceros", "casual stuff");
        int epicId = manager.createEpic(epic);

        String newSubtask = "{\n" +
                "  \"epic\": {\n" +
                "    \"name\": \"Sunday quehaceros\",\n" +
                "    \"description\": \"casual stuff\",\n" +
                "    \"id\": 1,\n" +
                "    \"status\": \"NEW\",\n" +
                "    \"type\": \"EPIC\"\n" +
                "  },\n" +
                "  \"name\": \"cook dinner\",\n" +
                "  \"description\": \"pasta with meatballs\",\n" +
                "  \"status\": \"NEW\",\n" +
                "  \"duration\": 45,\n" +
                "  \"startTime\": \"01.03.24 19:00\"\n" +
                "}";

        String url = "http://localhost:8080/subtasks";
        URI uri = URI.create(url);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(newSubtask))
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        try {
            HttpResponse<String> response = client.send(request, handler);
            int expectedCode = 201;

            List<Subtask> tasks = manager.getAllSubtasks();
            int expectedSize = 1;

            assertEquals(expectedCode, response.statusCode());
            assertEquals(expectedSize, tasks.size());


        } catch (IOException | InterruptedException e) {
        }


    }
}