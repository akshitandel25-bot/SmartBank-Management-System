<?php
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/db_transfer_config.php';
require_once __DIR__ . '/smartspend_common_pdo.php';

$fromId = filter_input(INPUT_POST, 'from_account_id', FILTER_VALIDATE_INT);
$destination = trim($_POST['destination_account_number'] ?? '');
$amount = filter_input(INPUT_POST, 'amount', FILTER_VALIDATE_FLOAT);
$fraudReason = trim($_POST['fraud_reason'] ?? '');

if (!$fromId || $destination === '' || !$amount || $amount <= 0) {
    http_response_code(400); echo json_encode(['success'=>false,'message'=>'Invalid transfer details']); exit;
}
if (strlen($destination) > 20) {
    http_response_code(400); echo json_encode(['success'=>false,'message'=>'Account number is too long']); exit;
}

try {
    $pdo->beginTransaction();
    // Lock both accounts in a consistent order to prevent concurrent balance races.
    $find = $pdo->prepare('SELECT account_id, account_number, balance FROM accounts WHERE account_id = ? OR account_number = ? ORDER BY account_id FOR UPDATE');
    $find->execute([$fromId, $destination]);
    $accounts = $find->fetchAll(PDO::FETCH_ASSOC);
    $from = null; $to = null;
    foreach ($accounts as $a) {
        if ((int)$a['account_id'] === (int)$fromId) $from = $a;
        if (strcasecmp($a['account_number'], $destination) === 0) $to = $a;
    }
    if (!$from) throw new RuntimeException('Your source account could not be found. Please sign in again or verify the source account ID.');
    if (!$to) throw new RuntimeException('Destination account ' . $destination . ' does not exist in the SmartBank accounts table. Add/register that account before transferring.');
    if ((int)$from['account_id'] === (int)$to['account_id']) throw new RuntimeException('You cannot transfer to the same account.');
    if ((float)$from['balance'] < (float)$amount) throw new RuntimeException('Insufficient balance.');

    $debit = $pdo->prepare('UPDATE accounts SET balance = balance - ? WHERE account_id = ? AND balance >= ?');
    $debit->execute([$amount, $fromId, $amount]);
    if ($debit->rowCount() !== 1) throw new RuntimeException('Insufficient balance or account changed.');
    $credit = $pdo->prepare('UPDATE accounts SET balance = balance + ? WHERE account_id = ?');
    $credit->execute([$amount, $to['account_id']]);

    $senderLabel = 'Transfer to ' . $to['account_number'];
    $receiverLabel = 'Transfer from ' . $from['account_number'];
    $status = $fraudReason === '' ? 'Completed' : 'Flagged for review';
    $insert = $pdo->prepare('INSERT INTO transactions (account_id, recipient, transaction_type, amount, status, fraud_reason, category) VALUES (?, ?, ?, ?, ?, ?, ?)');
    $insert->execute([$fromId, $senderLabel, 'Transfer', $amount, $status, $fraudReason, 'Other']);
    $senderTransactionId = (int)$pdo->lastInsertId();
    $insert->execute([$to['account_id'], $receiverLabel, 'Deposit', $amount, 'Completed', '', 'Other']);
    $receiverTransactionId = (int)$pdo->lastInsertId();
    $pdo->commit();
    echo json_encode(['success'=>true,'message'=>'Transfer completed','sender_transaction_id'=>$senderTransactionId,'receiver_transaction_id'=>$receiverTransactionId]);
} catch (Throwable $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    http_response_code(400); echo json_encode(['success'=>false,'message'=>$e->getMessage()]);
}
