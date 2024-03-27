package httphandlers;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.Managers;
import taskmanagement.Task;
import taskmanagement.TaskManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHandlerTest {

    private HttpServer httpServer;
    private TaskManager manager;
    private PrioritizedHandler handler;
    private HttpClient client;
    private static final int PORT = 8080;


    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        handler = new PrioritizedHandler(manager);
        httpServer.createContext("/prioritized", handler);
        client = HttpClient.newHttpClient();
        httpServer.start();
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(2);
    }


    @Test
    public void shouldGetPrioritizedTasks() {
        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "02.03.24 10:00", 200);
        manager.createTask(t2);

        String url = "http://localhost:8080/prioritized";
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
            String expectedBody = getReferenceDataFromFile("test/testresources/priorities.json");

            assertNotNull(response.body());
            assertEquals(expectedBody, response.body());
            assertEquals(expectedCode, response.statusCode());

        } catch (IOException | InterruptedException e) {
            fail("Exception was thrown:" + e.getMessage());
        }
    }

    public String getReferenceDataFromFile(String filepath) {

        Path path = Paths.get(filepath);

        StringBuilder builder = new StringBuilder();
        try (BufferedReader rdr =
                     new BufferedReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
            while (rdr.ready()) {
                builder.append(rdr.readLine()).append("\n");
            }
            builder.deleteCharAt(builder.length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

}