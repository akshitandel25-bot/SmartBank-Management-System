import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SmartBank PHP API client.
 * Uses Java's built-in HttpClient; no external JAR is required.
 */
public class BankApi {

    private static final String BASE_URL = "http://localhost/smartbank-api/";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private BankApi() {
        // Utility class
    }

    /** Save a deposit, withdrawal, or transfer through the PHP API. */
    public static String saveTransaction(
            int accountId,
            String recipient,
            String type,
            double amount,
            String status,
            String fraudReason) throws Exception {

        String form = "account_id=" + encode(String.valueOf(accountId))
                + "&recipient=" + encode(recipient == null ? "" : recipient)
                + "&transaction_type=" + encode(type == null ? "" : type)
                + "&amount=" + encode(String.valueOf(amount))
                + "&status=" + encode(status == null ? "Completed" : status)
                + "&fraud_reason=" + encode(fraudReason == null ? "" : fraudReason);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "save_transaction.php"))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = CLIENT.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Save transaction API returned HTTP "
                    + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    /** Read the current balance for an account from MySQL via PHP. */
    public static double getBalance(int accountId) throws Exception {
        String url = BASE_URL + "get_balance.php?account_id=" + accountId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Balance API returned HTTP "
                    + response.statusCode() + ": " + response.body());
        }

        String json = response.body();
        String balanceText = getJsonValue(json, "balance");
        if (balanceText.isBlank()) {
            throw new RuntimeException("Balance was not found in API response: " + json);
        }
        try {
            return Double.parseDouble(balanceText);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid balance in API response: " + balanceText, ex);
        }
    }

    /** Read income and expenses totals from the dashboard statistics API. */
    public static double[] getStats(int accountId) throws Exception {
        String url = BASE_URL + "get_stats.php?account_id=" + accountId;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Statistics API returned HTTP "
                    + response.statusCode() + ": " + response.body());
        }
        String json = response.body();
        if (!json.replace(" ", "").contains("\"success\":true")) {
            throw new RuntimeException("Could not load dashboard statistics: " + json);
        }
        String income = getJsonValue(json, "total_income");
        String expenses = getJsonValue(json, "total_expenses");
        if (income.isBlank() || expenses.isBlank()) {
            throw new RuntimeException("Statistics missing from API response: " + json);
        }
        return new double[]{Double.parseDouble(income), Double.parseDouble(expenses)};
    }

    /**
     * Load transaction history. Each returned row has:
     * [transaction ID, account ID, date, recipient, type, amount, status, fraud reason].
     */
    public static List<String[]> getTransactions(int accountId) throws Exception {
        String url = BASE_URL + "get_transactions.php?account_id=" + accountId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Transaction API returned HTTP "
                    + response.statusCode() + ": " + response.body());
        }

        String json = response.body();
        if (!json.replace(" ", "").contains("\"success\":true")) {
            throw new RuntimeException("API could not load transactions: " + json);
        }

        List<String[]> transactions = new ArrayList<>();
        // This API returns a flat array of transaction objects, so each object
        // can be parsed independently without an external JSON library.
        Pattern objectPattern = Pattern.compile("\\{([^{}]*)\\}");
        Matcher objectMatcher = objectPattern.matcher(json);

        while (objectMatcher.find()) {
            String object = objectMatcher.group(1);
            if (!object.contains("\"transaction_id\"")) {
                continue;
            }

            String transactionId = getJsonValue(object, "transaction_id");
            String accountIdValue = getJsonValue(object, "account_id");
            String date = getJsonValue(object, "created_at");
            String recipient = getJsonValue(object, "recipient");
            String type = getJsonValue(object, "type");
            String amount = getJsonValue(object, "amount");
            String status = getJsonValue(object, "status");
            String reason = getJsonValue(object, "fraud_reason");

            transactions.add(new String[]{transactionId, accountIdValue, date,
                    recipient, type, amount, status, reason});
        }
        return transactions;
    }


    public static String transferBetweenAccounts(int fromAccountId, String destinationAccountNumber, double amount, String fraudReason) throws Exception {
        String form = "from_account_id=" + encode(String.valueOf(fromAccountId))
                + "&destination_account_number=" + encode(destinationAccountNumber)
                + "&amount=" + encode(String.valueOf(amount))
                + "&fraud_reason=" + encode(fraudReason == null ? "" : fraudReason);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "transfer_between_accounts.php"))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new RuntimeException(response.body());
        return response.body();
    }

    public static String saveSecurityEvent(int accountId, Integer transactionId, String eventType, String riskLevel,
            String description, String recommendedAction) throws Exception {
        return saveSecurityEvent(accountId, transactionId, eventType, riskLevel,
                description, recommendedAction, "Needs review");
    }

    /** Append a security event with its supplied review status. */
    public static String saveSecurityEvent(int accountId, Integer transactionId, String eventType, String riskLevel,
            String description, String recommendedAction, String eventStatus) throws Exception {
        String form = "account_id=" + encode(String.valueOf(accountId))
                + "&transaction_id=" + encode(transactionId == null ? "" : String.valueOf(transactionId))
                + "&event_type=" + encode(eventType) + "&risk_level=" + encode(riskLevel)
                + "&description=" + encode(description) + "&recommended_action=" + encode(recommendedAction)
                + "&event_status=" + encode(eventStatus == null ? "Needs review" : eventStatus);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "save_security_event.php"))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new RuntimeException(response.body());
        return response.body();
    }

    public static List<String[]> getSecurityEvents(int accountId) throws Exception {
        String url = BASE_URL + "get_security_events.php?account_id=" + accountId;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw new RuntimeException(response.body());
        String json = response.body();
        if (!json.replace(" ", "").contains("\"success\":true")) throw new RuntimeException(json);
        List<String[]> events = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{([^{}]*)\\}").matcher(json);
        while (matcher.find()) {
            String obj = matcher.group(1);
            if (!obj.contains("\"event_id\"")) continue;
            events.add(new String[]{getJsonValue(obj,"event_id"),getJsonValue(obj,"created_at"),
                getJsonValue(obj,"event_type"),getJsonValue(obj,"risk_level"),getJsonValue(obj,"description"),
                getJsonValue(obj,"recommended_action"),getJsonValue(obj,"event_status")});
        }
        return events;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** Read a simple JSON scalar value from an object or API response. */
    private static String getJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                + "\"\\s*:\\s*(null|\"(?:\\\\.|[^\"\\\\])*\"|[^,}\\s]+)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return "";
        }

        String value = matcher.group(1).trim();
        if ("null".equals(value)) {
            return "";
        }
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\\\"", "\"")
                         .replace("\\n", "\n")
                         .replace("\\r", "\r")
                         .replace("\\t", "\t")
                         .replace("\\\\", "\\");
        }
        return value;
    }
}
