package httphandlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.Managers;
import taskmanagement.Task;
import taskmanagement.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HistoryHandlerTest {
    private HttpServer httpServer;
    private TaskManager manager;
    private HistoryHandler handler;
    private HttpClient client;
    private static final int PORT = 8080;


    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        handler = new HistoryHandler(manager);
        httpServer.createContext("/history", handler);
        client = HttpClient.newHttpClient();
        httpServer.start();
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(2);
    }


    @Test
    public void shouldGetHistory() {
        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        int t1Id = manager.createTask(t1);

        manager.findTaskById(t1Id);

        String url = "http://localhost:8080/history";
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
                    "  \"name\": \"cook dinner\",\n" +
                    "  \"description\": \"pasta with meatballs\",\n" +
                    "  \"id\": 1,\n" +
                    "  \"status\": \"NEW\",\n" +
                    "  \"type\": \"TASK\",\n" +
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