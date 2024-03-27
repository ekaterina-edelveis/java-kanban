package httphandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanagement.Task;
import taskmanagement.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends TaskHandler implements HttpHandler {

    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        if (endpoint.equals(Endpoint.GET_TASKS)) {
            handleGetHistory(exchange);
        } else {
            apiMessage.setMessage("Такой страницы не существует");
            String response = gson.toJson(apiMessage);
            writeResponse(exchange, response, 404);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {

        List<Task> history = manager.getHistory();
        String response = gson.toJson(history);
        writeResponse(exchange, response, 200);

    }

    protected Endpoint getEndpoint(String requestPath, String requestMethod) {

        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("history") && requestMethod.equals("GET")) {
            return Endpoint.GET_TASKS;
        }
        return Endpoint.UNKNOWN;
    }

}
