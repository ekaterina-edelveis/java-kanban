package httphandlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanagement.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gsonForDateTime;
    private final Gson basicGson;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
        gsonForDateTime = createGson();
        basicGson = createSimpleGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                handleGetTasks(exchange);
                break;
            }
            case GET_TASK: {
                handleGetTask(exchange);
                break;
            }
            case POST_TASK: {
                handlePostTask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleDeleteTask(exchange);
                break;
            }
            case DELETE_TASKS: {
                handleDeleteTasks(exchange);
                break;
            }
            default:
                writeResponse(exchange, "Такой страницы не существует", 404);
        }


    }

    private void handleDeleteTasks(HttpExchange exchange) throws IOException {
        manager.deleteAllTasks();
        writeResponse(exchange, "Все задачи удалены", 200);
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOptional = getTaskId(exchange);

        if (taskIdOptional.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 400);
            return;
        }
        int taskId = taskIdOptional.get();

        Task task = manager.findTaskById(taskId);
        if (task != null) {
            manager.deleteTaskById(taskId);
            writeResponse(exchange, "Задача удалена", 200);
        } else {
            writeResponse(exchange, "Задача с таким ID не обнаружена", 404);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {

        Optional<Task> optional = parseTask(exchange.getRequestBody());
        if (optional.isEmpty()) {
            writeResponse(exchange, "Поля задачи не могут быть пустыми", 400);
            return;
        }

        Task task = optional.get();


        if (task.getId() == 0) {
            try {

                manager.createTask(task);
                writeResponse(exchange, "Задача успешно создана", 201);
            } catch (ManagerSaveException ex) {
                writeResponse(exchange, "Задача пересекается с существующими", 406);
            }
        } else {
            try {
                manager.updateTask(task);
                writeResponse(exchange, "Задача успешно обновлена", 201);
            } catch (ManagerSaveException ex) {
                writeResponse(exchange, "Задача пересекается с существующими", 406);
            }
        }
    }

    private void handleGetTask(HttpExchange exchange) throws IOException {

        Optional<Integer> taskIdOptional = getTaskId(exchange);

        if (taskIdOptional.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 400);
            return;
        }
        int taskId = taskIdOptional.get();

        Task task = manager.findTaskById(taskId);
        String taskResponse;
        if (task != null) {
            if (task.getStartTime() != null) {
                taskResponse = gsonForDateTime.toJson(task);
            } else {
                taskResponse = basicGson.toJson(task);
            }
            writeResponse(exchange, taskResponse, 200);
        } else {
            writeResponse(exchange, "Задача с таким ID не обнаружена", 404);
        }

    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {

        List<Task> tasks = manager.getAllTasks();

        String response = tasks.stream()
                .map(task -> {
                    if (task.getStartTime() != null) {
                        return gsonForDateTime.toJson(task);
                    } else {
                        return basicGson.toJson(task);
                    }
                })
                .collect(Collectors.joining("\n"));

        writeResponse(exchange, response, 200);

    }

    private Gson createGson() {

        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter().nullSafe())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter().nullSafe())
                .create();
    }

    private Gson createSimpleGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }


    private Endpoint getEndpoint(String requestPath, String requestMethod) {

        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_TASK;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASKS;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASK;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASK;
            }
        }
        return Endpoint.UNKNOWN;

    }

    private Optional<Integer> getTaskId(HttpExchange exchange) {
        String[] path = exchange.getRequestURI().getPath().split("/");
        try {
            int id = Integer.parseInt(path[2]);
            return Optional.of(id);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }

    }

    private Optional<Task> parseTask(InputStream inputStream) throws IOException {

        String requestBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

        JsonElement element = JsonParser.parseString(requestBody);
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            String name = object.get("name").getAsString();
            String description = object.get("description").getAsString();
            Status status = Status.valueOf(object.get("status").getAsString());
            Task task = new Task(name, description);
            task.setStatus(status);
            if (object.has("id")) {
                int id = object.get("id").getAsInt();
                task.setId(id);
            }
            if (object.has("type")) {
                TaskType type = TaskType.valueOf(object.get("type").getAsString());
                task.setType(type);
            }

            if (object.has("startTime")) {
                String start = object.get("startTime").getAsString();
                task.setStartTime(start);
            }
            if (object.has("duration")) {
                long duration = object.get("duration").getAsLong();
                task.setDuration(duration);
            }

            return Optional.of(task);
        }
        return Optional.empty();

    }


    private void writeResponse(HttpExchange exchange,
                               String responseString,
                               int responseCode) throws IOException {

        exchange.sendResponseHeaders(responseCode, 0);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseString.getBytes(DEFAULT_CHARSET));
        }

        exchange.close();
    }

}
