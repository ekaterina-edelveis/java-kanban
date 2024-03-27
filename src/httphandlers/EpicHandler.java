package httphandlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanagement.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class EpicHandler extends TaskHandler implements HttpHandler {

    public EpicHandler(TaskManager manager) {
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
            case DELETE_TASKS: {
                handleDeleteTasks(exchange);
                break;
            }
            case GET_SUBTASKS_FOR_EPIC: {
                handleGetSubtasksForEpic(exchange);
                break;
            }
            default:
                apiMessage.setMessage("Такой страницы не существует");
                String response = gson.toJson(apiMessage);
                writeResponse(exchange, response, 404);
        }
    }


    protected void handleDeleteTasks(HttpExchange exchange) throws IOException {
        manager.deleteAllEpics();

        apiMessage.setMessage("Все задачи удалены");
        String response = gson.toJson(apiMessage);
        writeResponse(exchange, response, 200);
    }

    protected void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> epicIdOptional = getTaskId(exchange);

        if (epicIdOptional.isEmpty()) {
            apiMessage.setMessage("Некорректный идентификатор задачи");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 400);
            return;
        }
        int epicId = epicIdOptional.get();

        Epic epic = manager.findEpicById(epicId);
        if (epic != null) {
            manager.deleteEpicById(epicId);

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

        Optional<Epic> optional = parseTask(exchange.getRequestBody());
        if (optional.isEmpty()) {

            apiMessage.setMessage("Невозможно создать задачу: все или некоторые поля запроса были пустыми");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 400);
            return;
        }

        Epic epic = optional.get();

        if (epic.getId() == 0) {
            try {
                manager.createEpic(epic);

                apiMessage.setMessage("Задача успешно создана");
                String response = gson.toJson(apiMessage);
                writeResponse(exchange, response, 201);
            } catch (Exception ex) {
                String error = ex.getMessage();
                apiMessage.setMessage(error);
                String response = gson.toJson(apiMessage);
                writeResponse(exchange, response, 500);
            }
        } else {
            manager.updateEpic(epic);

            apiMessage.setMessage("Задача успешно обновлена");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 201);
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

        Epic epic = manager.findEpicById(taskId);
        String taskResponse;
        if (epic != null) {
            taskResponse = gson.toJson(epic);
            writeResponse(exchange, taskResponse, 200);
        } else {
            apiMessage.setMessage("Задача с таким ID не обнаружена");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 404);
        }

    }

    protected void handleGetTasks(HttpExchange exchange) throws IOException {

        List<Epic> epics = manager.getAllEpics();
        String response = gson.toJson(epics);

        writeResponse(exchange, response, 200);

    }

    private void handleGetSubtasksForEpic(HttpExchange exchange) throws IOException {

        Optional<Integer> taskIdOptional = getTaskId(exchange);

        if (taskIdOptional.isEmpty()) {
            apiMessage.setMessage("Некорректный идентификатор задачи");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 400);
            return;
        }
        int taskId = taskIdOptional.get();

        Epic epic = manager.findEpicById(taskId);
        String taskResponse;

        if (epic != null) {

            List<Subtask> subs = manager.getAllSubtasksForEpic(taskId);

            taskResponse = gson.toJson(subs);

            writeResponse(exchange, taskResponse, 200);
        } else {
            apiMessage.setMessage("Задача с таким ID не обнаружена");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 404);
        }

    }


    protected Endpoint getEndpoint(String requestPath, String requestMethod) {

        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("epics")) {
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
        if (pathParts.length == 3 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASK;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASK;
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals("epics")
                && pathParts[3].equals("subtasks") && requestMethod.equals("GET")) {
            return Endpoint.GET_SUBTASKS_FOR_EPIC;
        }
        return Endpoint.UNKNOWN;

    }

    private Optional<Epic> parseTask(InputStream inputStream) throws IOException {

        String requestBody = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

        JsonElement element = JsonParser.parseString(requestBody);
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            String name = object.get("name").getAsString();
            String description = object.get("description").getAsString();
            Status status = Status.valueOf(object.get("status").getAsString());

            Epic epic = new Epic(name, description);
            epic.setStatus(status);

            if (object.has("id")) {
                int id = object.get("id").getAsInt();
                epic.setId(id);
            }
            if (object.has("type")) {
                TaskType type = TaskType.valueOf(object.get("type").getAsString());
                epic.setType(type);
            }

            if (object.has("startTime")) {
                String start = object.get("startTime").getAsString();
                epic.setStartTime(start);
            }
            if (object.has("duration")) {
                long duration = object.get("duration").getAsLong();
                epic.setDuration(duration);
            }

            return Optional.of(epic);
        }
        return Optional.empty();

    }


}
