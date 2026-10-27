
<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db.php';
require_once 'manager_auth.php';

requireManager($conn);

$sql = "
SELECT
    t.transaction_id,
    t.account_id,
    a.account_number,
    c.full_name,
    t.recipient,
    t.transaction_type,
    t.amount,
    t.status,
    t.fraud_reason,
    t.transaction_date
FROM transactions t
LEFT JOIN accounts a ON t.account_id = a.account_id
LEFT JOIN customers c ON a.customer_id = c.customer_id
ORDER BY t.transaction_date DESC
LIMIT 500
";

$result = $conn->query($sql);

if (!$result) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Query failed']);
    exit;
}

$transactions = [];
while ($row = $result->fetch_assoc()) {
    $transactions[] = $row;
}

echo json_encode([
    'success' => true,
    'transactions' => $transactions
]);
?>
