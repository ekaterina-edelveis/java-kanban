package httphandlers;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanagement.Managers;
import taskmanagement.Status;
import taskmanagement.Task;
import taskmanagement.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskHandlerTest {

    private HttpServer httpServer;
    private TaskManager manager;
    private TaskHandler handler;
    private HttpClient client;
    private static final int PORT = 8080;

    @BeforeEach
    void setUp() throws IOException {
        manager = Managers.getDefault();
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        handler = new TaskHandler(manager);
        httpServer.createContext("/tasks", handler);
        client = HttpClient.newHttpClient();
        httpServer.start();
    }

    @AfterEach
    void tearDown() {
        httpServer.stop(2);
    }

    @Test
    public void shouldCreateTask() {
        String url = "http://localhost:8080/tasks";
        URI uri = URI.create(url);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

        String newTask = "{\n" +
                "  \"name\": \"cook dinner\",\n" +
                "  \"description\": \"pasta with meatballs\",\n" +
                "  \"status\": \"NEW\",\n" +
                "  \"duration\": 45,\n" +
                "  \"startTime\": \"01.03.24 19:00\"\n" +
                "}";

        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(newTask))
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        try {
            HttpResponse<String> response = client.send(request, handler);
            int expectedCode = 201;

            List<Task> tasks = manager.getAllTasks();
            int expectedSize = 1;

            assertEquals(expectedCode, response.statusCode());
            assertEquals(expectedSize, tasks.size());


        } catch (IOException | InterruptedException e) {
        }

    }

    @Test
    public void shouldUpdateTask() {
        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        int t1Id = manager.createTask(t1);

        String url = "http://localhost:8080/tasks";
        URI uri = URI.create(url);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

        String updTask = "{\n" +
                "  \"name\": \"cook dinner\",\n" +
                "  \"description\": \"pasta with meatballs\",\n" +
                "  \"id\": 1,\n" +
                "  \"status\": \"DONE\",\n" +
                "  \"type\": \"TASK\",\n" +
                "  \"duration\": 45,\n" +
                "  \"startTime\": \"01.03.24 19:00\"\n" +
                "}";

        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(updTask))
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        try {
            HttpResponse<String> response = client.send(request, handler);
            int expectedCode = 201;

            Task task = manager.findTaskById(t1Id);

            assertEquals(expectedCode, response.statusCode());
            assertEquals(Status.DONE, task.getStatus());


        } catch (IOException | InterruptedException e) {
        }

    }

    @Test
    public void shouldGetTaskById() {
        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        int t1Id = manager.createTask(t1);

        String url = "http://localhost:8080/tasks/" + t1Id;
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

    @Test
    public void shouldGetTasks() {
        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        int t1Id = manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);
        int t2Id = manager.createTask(t2);

        String url = "http://localhost:8080/tasks";
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
                    "}\n" +
                    "{\n" +
                    "  \"name\": \"write an article\",\n" +
                    "  \"description\": \"risc-v java port\",\n" +
                    "  \"id\": 2,\n" +
                    "  \"status\": \"NEW\",\n" +
                    "  \"type\": \"TASK\",\n" +
                    "  \"duration\": 200,\n" +
                    "  \"startTime\": \"01.03.24 10:00\"\n" +
                    "}";

            assertNotNull(response.body());
            assertEquals(expectedBody, response.body());
            assertEquals(expectedCode, response.statusCode());

        } catch (IOException | InterruptedException e) {
        }
    }

    @Test
    public void shouldDeleteTaskById() {
        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        int t1Id = manager.createTask(t1);

        String url = "http://localhost:8080/tasks/" + t1Id;
        URI uri = URI.create(url);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        HttpRequest request = requestBuilder
                .DELETE()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        try {
            HttpResponse<String> response = client.send(request, handler);
            int expectedCode = 200;
            Task task = manager.findTaskById(t1Id);

            assertEquals(expectedCode, response.statusCode());
            assertNull(task);

        } catch (IOException | InterruptedException e) {
        }
    }

    @Test
    public void shouldDeleteTasks() {
        Task t1 = new Task("cook dinner", "pasta with meatballs", "01.03.24 19:00", 45);
        int t1Id = manager.createTask(t1);

        Task t2 = new Task("write an article", "risc-v java port", "01.03.24 10:00", 200);
        int t2Id = manager.createTask(t2);

        String url = "http://localhost:8080/tasks";
        URI uri = URI.create(url);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        HttpRequest request = requestBuilder
                .DELETE()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        try {
            HttpResponse<String> response = client.send(request, handler);
            int expectedCode = 200;

            List<Task> tasks = manager.getAllTasks();
            int expectedSize = 0;

            assertEquals(expectedCode, response.statusCode());
            assertEquals(expectedSize, tasks.size());

        } catch (IOException | InterruptedException e) {
        }


    }

}