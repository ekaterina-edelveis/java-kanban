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

public class TaskHandler implements HttpHandler {

    protected final TaskManager manager;
    protected final Gson gson;
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected ApiMessage apiMessage;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
        gson = createGson();
        apiMessage = new ApiMessage();
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
                apiMessage.setMessage("Такой страницы не существует");
                String response = gson.toJson(apiMessage);
                writeResponse(exchange, response, 404);
        }


    }

    protected void handleDeleteTasks(HttpExchange exchange) throws IOException {
        manager.deleteAllTasks();
        apiMessage.setMessage("Все задачи удалены");
        String response = gson.toJson(apiMessage);
        writeResponse(exchange, response, 200);
    }

    protected void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOptional = getTaskId(exchange);

        if (taskIdOptional.isEmpty()) {
            apiMessage.setMessage("Некорректный идентификатор задачи");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 400);
            return;
        }
        int taskId = taskIdOptional.get();

        Task task = manager.findTaskById(taskId);
        if (task != null) {
            manager.deleteTaskById(taskId);

            apiMessage.setMessage("Задача удалена");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 200);
        } else {
            apiMessage.setMessage("Задача с таким ID не обнаружена");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 404);
        }
    }

    protected void handlePostTask(HttpExchange exchange) throws IOException {

        Optional<Task> optional = parseTask(exchange.getRequestBody());
        if (optional.isEmpty()) {
            apiMessage.setMessage("Невозможно создать задачу: все или некоторые поля запроса были пустыми");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 400);
            return;
        }

        Task task = optional.get();

        if (task.getId() == 0) {
            try {

                manager.createTask(task);

                apiMessage.setMessage("Задача успешно создана");
                String response = gson.toJson(apiMessage);
                writeResponse(exchange, response, 201);
            } catch (ManagerSaveException ex) {
                apiMessage.setMessage("Задача пересекается с существующими");
                String response = gson.toJson(apiMessage);
                writeResponse(exchange, response, 406);
            }
        } else {
            try {
                manager.updateTask(task);

                apiMessage.setMessage("Задача успешно обновлена");
                String response = gson.toJson(apiMessage);
                writeResponse(exchange, response, 201);
            } catch (ManagerSaveException ex) {
                apiMessage.setMessage("Задача пересекается с существующими");
                String response = gson.toJson(apiMessage);
                writeResponse(exchange, response, 406);
            }
        }
    }

    protected void handleGetTask(HttpExchange exchange) throws IOException {

        Optional<Integer> taskIdOptional = getTaskId(exchange);

        if (taskIdOptional.isEmpty()) {
            apiMessage.setMessage("Некорректный идентификатор задачи");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 400);
            return;
        }
        int taskId = taskIdOptional.get();

        Task task = manager.findTaskById(taskId);
        String taskResponse;
        if (task != null) {
            taskResponse = gson.toJson(task);

            writeResponse(exchange, taskResponse, 200);
        } else {
            apiMessage.setMessage("Задача с таким ID не обнаружена");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 404);
        }

    }

    protected void handleGetTasks(HttpExchange exchange) throws IOException {

        List<Task> tasks = manager.getAllTasks();
        String response = gson.toJson(tasks);

        writeResponse(exchange, response, 200);

    }

    protected Gson createGson() {

        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter().nullSafe())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter().nullSafe())
                .create();
    }


    protected Endpoint getEndpoint(String requestPath, String requestMethod) {

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

    protected Optional<Integer> getTaskId(HttpExchange exchange) {
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


    protected void writeResponse(HttpExchange exchange,
                                 String responseString,
                                 int responseCode) throws IOException {

        exchange.sendResponseHeaders(responseCode, 0);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseString.getBytes(DEFAULT_CHARSET));
        }

        exchange.close();
    }

}
