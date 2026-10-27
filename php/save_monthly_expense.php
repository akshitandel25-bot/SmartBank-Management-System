<?php
header('Content-Type: application/json; charset=utf-8');
if($_SERVER['REQUEST_METHOD']!=='POST'){http_response_code(405);echo json_encode(['success'=>false,'message'=>'Use POST']);exit;}
require_once __DIR__.'/db.php';require_once __DIR__.'/smartspend_common.php';
$accountId=filter_input(INPUT_POST,'account_id',FILTER_VALIDATE_INT);$category=smartspend_category(trim($_POST['category']??''));$recipient=trim($_POST['description']??$category);$amount=filter_input(INPUT_POST,'amount',FILTER_VALIDATE_FLOAT);
if(!$accountId||$amount===false||$amount===null||!is_finite((float)$amount)||$amount<=0||strlen($recipient)>100){http_response_code(400);echo json_encode(['success'=>false,'message'=>'Enter a valid description and amount']);exit;}
try{$conn->begin_transaction();$a=$conn->prepare('SELECT balance FROM accounts WHERE account_id=? FOR UPDATE');$a->bind_param('i',$accountId);$a->execute();$a->bind_result($balance);if(!$a->fetch())throw new RuntimeException('Account not found');$a->close();if((float)$balance<(float)$amount)throw new RuntimeException('Insufficient account balance.');smartspend_check_budget($conn,(int)$accountId,$category,(float)$amount);$u=$conn->prepare('UPDATE accounts SET balance=balance-? WHERE account_id=?');$u->bind_param('di',$amount,$accountId);$u->execute();$u->close();$type='Withdrawal';$status='Completed';$fraud='';$i=$conn->prepare('INSERT INTO transactions(account_id,recipient,transaction_type,amount,status,fraud_reason,category) VALUES(?,?,?,?,?,?,?)');$i->bind_param('issdsss',$accountId,$recipient,$type,$amount,$status,$fraud,$category);$i->execute();$id=$conn->insert_id;$i->close();$conn->commit();echo json_encode(['success'=>true,'message'=>'Expense saved','transaction_id'=>$id]);}
catch(DomainException $e){$conn->rollback();http_response_code(409);echo json_encode(['success'=>false,'budget_blocked'=>true,'message'=>$e->getMessage()]);}
catch(Throwable $e){$conn->rollback();http_response_code(400);echo json_encode(['success'=>false,'message'=>$e->getMessage()]);}
$conn->close();
?>
