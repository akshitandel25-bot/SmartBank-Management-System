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
                COALESCE(SUM(
                    CASE
                        WHEN transaction_type = 'Deposit'
                        THEN amount
                        ELSE 0
                    END
                ), 0) AS total_income,
                COALESCE(SUM(
                    CASE
                        WHEN transaction_type IN ('Withdrawal', 'Transfer')
                        THEN amount
                        ELSE 0
                    END
                ), 0) AS total_expenses
            FROM transactions
            WHERE account_id = ?";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $accountId);
    $stmt->execute();
    $stmt->bind_result($totalIncome, $totalExpenses);
    $stmt->fetch();

    echo json_encode([
        "success" => true,
        "total_income" => (float) $totalIncome,
        "total_expenses" => (float) $totalExpenses
    ]);

    $stmt->close();
    $conn->close();

} catch (Throwable $e) {
    error_log("SmartBank statistics error: " . $e->getMessage());

    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Unable to load dashboard statistics"
    ]);
}
?>
```