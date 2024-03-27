package httphandlers;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.*;

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
        manager.createEpic(epic);

        String newSubtask = getReferenceDataFromFile("test/testresources/subtask.json");

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