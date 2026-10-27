<?php
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/db_transfer_config.php';
$accountId = filter_input(INPUT_POST, 'account_id', FILTER_VALIDATE_INT);
$transactionIdText = trim($_POST['transaction_id'] ?? '');
$transactionId = $transactionIdText === '' ? null : filter_var($transactionIdText, FILTER_VALIDATE_INT);
$eventType = trim($_POST['event_type'] ?? '');
$riskLevel = trim($_POST['risk_level'] ?? '');
$description = trim($_POST['description'] ?? '');
$action = trim($_POST['recommended_action'] ?? '');
$status = trim($_POST['event_status'] ?? 'Needs review');
if (!$accountId || $eventType === '' || $riskLevel === '' || $description === '') { http_response_code(400); echo json_encode(['success'=>false,'message'=>'Missing required fields']); exit; }
try {
 $stmt=$pdo->prepare('INSERT INTO security_events (account_id, transaction_id, event_type, risk_level, description, recommended_action, event_status) VALUES (?, ?, ?, ?, ?, ?, ?)');
 $stmt->execute([$accountId,$transactionId,$eventType,$riskLevel,$description,$action,$status]);
 echo json_encode(['success'=>true,'event_id'=>(int)$pdo->lastInsertId()]);
} catch(Throwable $e) { http_response_code(500); echo json_encode(['success'=>false,'message'=>$e->getMessage()]); }
