
<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db.php';
require_once 'manager_auth.php';

requireManager($conn);

$sql = "
SELECT
    (SELECT COUNT(*) FROM customers) AS total_customers,
    (SELECT COUNT(*) FROM accounts) AS total_accounts,
    (SELECT COALESCE(SUM(balance), 0) FROM accounts) AS total_balance,
    (SELECT COALESCE(SUM(amount), 0) FROM transactions
       WHERE LOWER(transaction_type) = 'deposit'
       AND status = 'Completed') AS total_deposits,
    (SELECT COALESCE(SUM(amount), 0) FROM transactions
       WHERE LOWER(transaction_type) = 'withdrawal'
       AND status = 'Completed') AS total_withdrawals,
    (SELECT COALESCE(SUM(amount), 0) FROM transactions
       WHERE LOWER(transaction_type) = 'transfer'
       AND status = 'Completed') AS total_transfers,
    (SELECT COUNT(*) FROM transactions
       WHERE status LIKE '%Flagged%') AS flagged_transactions
";

$result = $conn->query($sql);

if (!$result) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Query failed']);
    exit;
}

echo json_encode([
    'success' => true,
    'overview' => $result->fetch_assoc()
]);
?>
