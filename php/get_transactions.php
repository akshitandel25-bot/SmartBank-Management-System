```php
<?php
header("Content-Type: application/json; charset=UTF-8");

require_once "db.php";

try {
    $accountId = isset($_GET["account_id"])
        ? (int) $_GET["account_id"]
        : 1;

    if ($accountId <= 0) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "message" => "Invalid account ID"
        ]);
        exit;
    }

    $sql = "SELECT
                transaction_id,
                account_id,
                recipient,
                transaction_type AS type,
                amount,
                status,
                fraud_reason,
                transaction_date AS created_at
            FROM transactions
            WHERE account_id = ?
            ORDER BY transaction_id DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $accountId);
    $stmt->execute();

    $result = $stmt->get_result();
    $transactions = [];

    while ($row = $result->fetch_assoc()) {
        $transactions[] = $row;
    }

    echo json_encode([
        "success" => true,
        "transactions" => $transactions
    ]);

    $stmt->close();
    $conn->close();

} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Unable to load transactions"
    ]);
}
?>
```