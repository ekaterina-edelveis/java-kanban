package httphandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanagement.Task;
import taskmanagement.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gsonForDateTime;
    private final Gson basicGson;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
        gsonForDateTime = createGson();
        basicGson = createSimpleGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        if (endpoint.equals(Endpoint.GET_TASKS)) {
            handleGetHistory(exchange);
        } else {
            writeResponse(exchange, "Такой страницы не существует", 404);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {

        List<Task> history = manager.getHistory();

        String response = history.stream()
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


    private Endpoint getEndpoint(String requestPath, String requestMethod) {

        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("history") && requestMethod.equals("GET")) {
            return Endpoint.GET_TASKS;
        }
        return Endpoint.UNKNOWN;
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
