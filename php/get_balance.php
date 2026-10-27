
<?php
header("Content-Type: application/json");

require "db.php";

$account_id = filter_input(INPUT_GET, "account_id", FILTER_VALIDATE_INT);

if (!$account_id || $account_id <= 0) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Invalid account ID"
    ]);
    $conn->close();
    exit;
}

$stmt = $conn->prepare(
    "SELECT account_number, balance, account_type
     FROM accounts
     WHERE account_id = ?"
);

$stmt->bind_param("i", $account_id);
$stmt->execute();
$stmt->bind_result($accountNumber, $balance, $accountType);

if ($stmt->fetch()) {
    echo json_encode([
        "success" => true,
        "account_id" => $account_id,
        "account_number" => $accountNumber,
        "balance" => (float)$balance,
        "account_type" => $accountType
    ]);
} else {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "Account not found"
    ]);
}

$stmt->close();
$conn->close();
?>
