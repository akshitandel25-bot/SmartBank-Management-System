<?php
header('Content-Type: application/json; charset=utf-8');
if ($_SERVER['REQUEST_METHOD'] !== 'POST') { http_response_code(405); echo json_encode(['success'=>false,'message'=>'Use POST']); exit; }
require_once __DIR__ . '/db.php';
$accountId=filter_input(INPUT_POST,'account_id',FILTER_VALIDATE_INT);$category=trim($_POST['category']??'');$month=trim($_POST['month']??date('Y-m'));$budget=filter_input(INPUT_POST,'budget_amount',FILTER_VALIDATE_FLOAT);
$allowed=['Groceries','Shopping','Food & dining','Transport','Bills','Other'];
if(!$accountId||!in_array($category,$allowed,true)||!preg_match('/^\d{4}-\d{2}$/',$month)||$budget===false||$budget===null||!is_finite((float)$budget)||$budget<0){http_response_code(400);echo json_encode(['success'=>false,'message'=>'Invalid budget details']);exit;}
try{$s=$conn->prepare('INSERT INTO category_budgets(account_id,category,month_key,budget_amount) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE budget_amount=VALUES(budget_amount)');$s->bind_param('issd',$accountId,$category,$month,$budget);$s->execute();echo json_encode(['success'=>true,'message'=>'Monthly budget saved']);$s->close();}catch(Throwable $e){http_response_code(500);echo json_encode(['success'=>false,'message'=>'Could not save budget']);}$conn->close();
?>
