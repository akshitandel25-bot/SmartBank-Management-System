
<?php
header("Content-Type: application/json");

function respond($status, $data) {
    http_response_code($status);
    echo json_encode($data);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    respond(405, [
        "success" => false,
        "message" => "Use POST to save a transaction"
    ]);
}

require "db.php";

$account_id = filter_input(INPUT_POST, "account_id", FILTER_VALIDATE_INT);
$recipient = trim($_POST["recipient"] ?? "");
$type = trim($_POST["transaction_type"] ?? "");
$amount = filter_input(INPUT_POST, "amount", FILTER_VALIDATE_FLOAT);
$status = "Completed";
$fraud_reason = trim($_POST["fraud_reason"] ?? "");
$category = trim($_POST["category"] ?? "Other");
// Ordinary dashboard transactions are separate from SmartSpend budgets.
 // Monthly category budgets are enforced only by save_monthly_expense.php.

$allowed_types = ["Deposit", "Withdrawal", "Transfer"];

if (!$account_id || $amount === false || $amount === null ||
    !is_finite((float)$amount) || $amount <= 0 ||
    !in_array($type, $allowed_types, true)) {
    respond(400, [
        "success" => false,
        "message" => "Invalid transaction details"
    ]);
}

if (strlen($recipient) > 100 || strlen($fraud_reason) > 255) {
    respond(400, [
        "success" => false,
        "message" => "Recipient or fraud reason is too long"
    ]);
}

/*
 * Fraud review is retained for transfers.
 * This endpoint records review status; it does not implement
 * a real bank approval or payment process.
 */
if ($type === "Transfer" && $fraud_reason !== "") {
    $status = "Flagged for review";
}

try {
    $conn->begin_transaction();

    // Lock the account row so the balance cannot be changed
    // by another transaction while this operation is running.
    $stmt = $conn->prepare(
        "SELECT balance FROM accounts
         WHERE account_id = ? FOR UPDATE"
    );
    $stmt->bind_param("i", $account_id);
    $stmt->execute();
    $stmt->bind_result($currentBalance);

    if (!$stmt->fetch()) {
        $stmt->close();
        $conn->rollback();
        respond(404, [
            "success" => false,
            "message" => "Account not found"
        ]);
    }
    $stmt->close();

    $currentBalance = (float)$currentBalance;

    if (($type === "Withdrawal" || $type === "Transfer")
            && $amount > $currentBalance) {
        $conn->rollback();
        respond(400, [
            "success" => false,
            "message" => "Insufficient account balance"
        ]);
    }

    // Dashboard deposits, withdrawals, and transfers are independent of SmartSpend.
    // Monthly expense budget enforcement remains in save_monthly_expense.php only.
    // A name-based demo transfer records the recipient and debits the sender; it does
    // not credit an unregistered recipient account.

    if ($type === "Deposit") {
        $update = $conn->prepare(
            "UPDATE accounts
             SET balance = balance + ?
             WHERE account_id = ?"
        );
    } else {
        $update = $conn->prepare(
            "UPDATE accounts
             SET balance = balance - ?
             WHERE account_id = ?"
        );
    }

    $update->bind_param("di", $amount, $account_id);
    $update->execute();
    $update->close();

    $insert = $conn->prepare(
        "INSERT INTO transactions
         (account_id, recipient, transaction_type, amount, status, fraud_reason, category)
         VALUES (?, ?, ?, ?, ?, ?, ?)"
    );

    $insert->bind_param(
        "issdsss",
        $account_id,
        $recipient,
        $type,
        $amount,
        $status,
        $fraud_reason,
        $category
    );

    $insert->execute();
    $transactionId = $conn->insert_id;
    $insert->close();

    $conn->commit();

    respond(200, [
        "success" => true,
        "message" => "Transaction and balance updated successfully",
        "transaction_id" => $transactionId,
        "status" => $status
    ]);

} catch (DomainException $e) {
    $conn->rollback();
    respond(409, ["success" => false, "budget_blocked" => true, "message" => $e->getMessage()]);
} catch (Throwable $e) {
    $conn->rollback();

    error_log("SmartBank transaction error: " . $e->getMessage());

    respond(500, [
        "success" => false,
        "message" => "Could not complete the database transaction"
    ]);
} finally {
    $conn->close();
}
?>
