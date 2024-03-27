package httphandlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanagement.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class SubtaskHandler extends TaskHandler implements HttpHandler {

    public SubtaskHandler(TaskManager manager) {
        super(manager);
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
                apiMessage.setMessage("Такой страницы не существует");
                String response = gson.toJson(apiMessage);
                writeResponse(exchange, response, 404);
        }

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

        Subtask subtask = manager.findSubtaskById(taskId);
        if (subtask != null) {
            manager.deleteSubtaskById(taskId);

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

        Optional<Subtask> optional = parseTask(exchange.getRequestBody());
        if (optional.isEmpty()) {
            apiMessage.setMessage("Невозможно создать задачу: все или некоторые поля запроса были пустыми");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 400);
            return;
        }

        Subtask subtask = optional.get();

        if (subtask.getId() == 0) {
            try {
                manager.createSubtask(subtask);

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
                manager.updateSubtask(subtask);

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

        Subtask subtask = manager.findSubtaskById(taskId);
        String taskResponse;
        if (subtask != null) {
            taskResponse = gson.toJson(subtask);
            writeResponse(exchange, taskResponse, 200);
        } else {
            apiMessage.setMessage("Задача с таким ID не обнаружена");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 404);
        }

    }

    protected void handleGetTasks(HttpExchange exchange) throws IOException {

        List<Subtask> subtasks = manager.getAllSubtasks();
        String response = gson.toJson(subtasks);
        writeResponse(exchange, response, 200);

    }


    protected Endpoint getEndpoint(String requestPath, String requestMethod) {

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

    private Optional<Subtask> parseTask(InputStream inputStream) throws IOException {

        String requestBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

        JsonElement element = JsonParser.parseString(requestBody);

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            String name = object.get("name").getAsString();
            String description = object.get("description").getAsString();
            Status status = Status.valueOf(object.get("status").getAsString());

            JsonObject epicObject = object.get("epic").getAsJsonObject();
            Epic epic = gson.fromJson(epicObject.toString(), Epic.class);

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

}
