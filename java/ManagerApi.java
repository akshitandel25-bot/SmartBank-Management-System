
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ManagerApi {

    private static final String BASE_URL =
            "http://localhost/smartbank-api/";

    private static final HttpClient CLIENT =
            HttpClient.newHttpClient();

    private static String encode(String value) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }

    private static String post(String endpoint, String form)
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                )
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "Manager API HTTP " + response.statusCode()
                    + ": " + response.body()
            );
        }

        return response.body();
    }

    private static String get(String endpoint, String token)
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "Manager API HTTP " + response.statusCode()
                    + ": " + response.body()
            );
        }

        return response.body();
    }

    public static String login(String username, String password)
            throws Exception {

        return post(
                "manager_login.php",
                "username=" + encode(username)
                + "&password=" + encode(password)
        );
    }

    public static String getOverview(String token)
            throws Exception {

        return get("manager_overview.php", token);
    }

    public static String getTransactions(String token)
            throws Exception {

        return get("manager_transactions.php", token);
    }

    public static String logout(String token)
            throws Exception {

        return post(
                "manager_logout.php",
                "token=" + encode(token)
        );
    }
}
