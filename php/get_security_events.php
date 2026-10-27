<?php
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/db_transfer_config.php';
$accountId = filter_input(INPUT_GET, 'account_id', FILTER_VALIDATE_INT);
if (!$accountId) { http_response_code(400); echo json_encode(['success'=>false,'message'=>'Invalid account_id']); exit; }
try {
 $stmt=$pdo->prepare('SELECT event_id, account_id, transaction_id, event_type, risk_level, description, recommended_action, event_status, created_at FROM security_events WHERE account_id = ? ORDER BY created_at DESC, event_id DESC');
 $stmt->execute([$accountId]); echo json_encode(['success'=>true,'events'=>$stmt->fetchAll(PDO::FETCH_ASSOC)]);
} catch(Throwable $e) { http_response_code(500); echo json_encode(['success'=>false,'message'=>$e->getMessage()]); }
