<?php
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/db.php';
$accountId = filter_input(INPUT_GET, 'account_id', FILTER_VALIDATE_INT);
$month = trim($_GET['month'] ?? date('Y-m'));
if (!$accountId || !preg_match('/^\d{4}-\d{2}$/', $month)) { http_response_code(400); echo json_encode(['success'=>false,'message'=>'Invalid account_id or month']); exit; }
$categories = ['Groceries','Shopping','Food & dining','Transport','Bills','Other'];
try {
  $start = $month . '-01'; $end = date('Y-m-d', strtotime($start . ' +1 month'));
  $stmt = $conn->prepare("SELECT category, COALESCE(SUM(CASE WHEN transaction_type IN ('Withdrawal','Transfer') AND status='Completed' THEN amount ELSE 0 END),0) AS spent FROM transactions WHERE account_id=? AND transaction_date>=? AND transaction_date<? GROUP BY category");
  $stmt->bind_param('iss', $accountId, $start, $end); $stmt->execute(); $res=$stmt->get_result(); $spent=[]; while($r=$res->fetch_assoc()) $spent[$r['category']]=(float)$r['spent']; $stmt->close();
  $b=$conn->prepare('SELECT category,budget_amount FROM category_budgets WHERE account_id=? AND month_key=?'); $b->bind_param('is',$accountId,$month); $b->execute(); $br=$b->get_result(); $budgets=[]; while($r=$br->fetch_assoc()) $budgets[$r['category']]=(float)$r['budget_amount']; $b->close();
  $rows=[]; $totalSpent=0; $totalBudget=0;
  foreach($categories as $c){$sp=(float)($spent[$c]??0);$bu=(float)($budgets[$c]??0);$totalSpent+=$sp;$totalBudget+=$bu;$rows[]=['category'=>$c,'spent'=>$sp,'budget'=>$bu,'remaining'=>max(0,$bu-$sp),'has_budget'=>array_key_exists($c,$budgets)];}
  echo json_encode(['success'=>true,'month'=>$month,'categories'=>$rows,'total_spent'=>$totalSpent,'total_budget'=>$totalBudget]);
} catch(Throwable $e){http_response_code(500);echo json_encode(['success'=>false,'message'=>'Could not load monthly expenses']);}
$conn->close();
?>
