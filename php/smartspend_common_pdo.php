<?php
function smartspend_check_budget_pdo(PDO $pdo, int $accountId, string $category, float $amount): void {
    $month = date('Y-m');
    $b = $pdo->prepare('SELECT budget_amount FROM category_budgets WHERE account_id = ? AND category = ? AND month_key = ? FOR UPDATE');
    $b->execute([$accountId, $category, $month]); $budget = $b->fetchColumn();
    if ($budget === false) return;
    $s = $pdo->prepare("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE account_id = ? AND category = ? AND transaction_type IN ('Withdrawal','Transfer') AND status = 'Completed' AND YEAR(transaction_date)=YEAR(CURDATE()) AND MONTH(transaction_date)=MONTH(CURDATE())");
    $s->execute([$accountId, $category]); $spent = (float)$s->fetchColumn();
    if ($spent + $amount > (float)$budget + 0.00001) {
        throw new RuntimeException('Strict Mode blocked this transfer. Other budget: ₹'.number_format((float)$budget,2).'; already spent: ₹'.number_format($spent,2).'; remaining: ₹'.number_format(max(0,(float)$budget-$spent),2).'.');
    }
}
?>
