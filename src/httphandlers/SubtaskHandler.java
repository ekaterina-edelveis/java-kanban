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

public class SubtaskHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gsonForDateTime;
    private final Gson basicGson;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public SubtaskHandler(TaskManager manager) {
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
            default:
                writeResponse(exchange, "Такой страницы не существует", 404);
        }


    }


    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOptional = getTaskId(exchange);

        if (taskIdOptional.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи", 400);
            return;
        }
        int taskId = taskIdOptional.get();

        Subtask subtask = manager.findSubtaskById(taskId);
        if (subtask != null) {
            manager.deleteSubtaskById(taskId);
            writeResponse(exchange, "Задача удалена", 200);
        } else {
            writeResponse(exchange, "Задача с таким ID не обнаружена", 404);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {

        Optional<Subtask> optional = parseTask(exchange.getRequestBody());
        if (optional.isEmpty()) {
            writeResponse(exchange, "Поля задачи не могут быть пустыми", 400);
            return;
        }

        Subtask subtask = optional.get();

        if (subtask.getId() == 0) {
            try {
                manager.createSubtask(subtask);
                writeResponse(exchange, "Задача успешно создана", 201);
            } catch (ManagerSaveException ex) {
                writeResponse(exchange, "Задача пересекается с существующими", 406);
            }
        } else {
            try {
                manager.updateSubtask(subtask);
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

        Subtask subtask = manager.findSubtaskById(taskId);
        String taskResponse;
        if (subtask != null) {
            if (subtask.getStartTime() != null) {
                taskResponse = gsonForDateTime.toJson(subtask);
            } else {
                taskResponse = basicGson.toJson(subtask);
            }
            writeResponse(exchange, taskResponse, 200);
        } else {
            writeResponse(exchange, "Задача с таким ID не обнаружена", 404);
        }

    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {

        List<Subtask> subtasks = manager.getAllSubtasks();

        String response = subtasks.stream()
                .map(subtask -> {
                    if (subtask.getStartTime() != null) {
                        return gsonForDateTime.toJson(subtask);
                    } else {
                        return basicGson.toJson(subtask);
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

        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_TASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
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

    private Optional<Subtask> parseTask(InputStream inputStream) throws IOException {

        String requestBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

        JsonElement element = JsonParser.parseString(requestBody);

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            String name = object.get("name").getAsString();
            String description = object.get("description").getAsString();
            Status status = Status.valueOf(object.get("status").getAsString());

            JsonObject epicObject = object.get("epic").getAsJsonObject();
            Epic epic;
            if (!epicObject.has("startTime")) {
                epic = basicGson.fromJson(epicObject.toString(), Epic.class);
            } else {
                epic = gsonForDateTime.fromJson(epicObject.toString(), Epic.class);
            }

            Subtask subtask = new Subtask(name, description, epic);

            subtask.setStatus(status);
            if (object.has("id")) {
                int id = object.get("id").getAsInt();
                subtask.setId(id);
            }
            if (object.has("type")) {
                TaskType type = TaskType.valueOf(object.get("type").getAsString());
                subtask.setType(type);
            }

            if (object.has("startTime")) {
                String start = object.get("startTime").getAsString();
                subtask.setStartTime(start);
            }
            if (object.has("duration")) {
                long duration = object.get("duration").getAsLong();
                subtask.setDuration(duration);
            }

            return Optional.of(subtask);
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
