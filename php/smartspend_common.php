<?php
// Shared category definitions and strict-mode validation for SmartBank.
const SMARTSPEND_CATEGORIES = ['Groceries', 'Shopping', 'Food & dining', 'Transport', 'Bills', 'Other'];
function smartspend_category(string $category): string {
    return in_array($category, SMARTSPEND_CATEGORIES, true) ? $category : 'Other';
}
/** Call only after begin_transaction(); throws on an exceeded configured cap. */
function smartspend_check_budget(mysqli $conn, int $accountId, string $category, float $amount): void {
    $month = date('Y-m');
    $budgetStmt = $conn->prepare('SELECT budget_amount FROM category_budgets WHERE account_id = ? AND category = ? AND month_key = ? FOR UPDATE');
    $budgetStmt->bind_param('iss', $accountId, $category, $month);
    $budgetStmt->execute();
    $budgetStmt->bind_result($budgetAmount);
    $hasBudget = $budgetStmt->fetch();
    $budgetStmt->close();
    // No cap has been configured for this category: preserve the existing app's behavior.
    if (!$hasBudget) return;
    $spentStmt = $conn->prepare("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE account_id = ? AND category = ? AND transaction_type IN ('Withdrawal','Transfer') AND status = 'Completed' AND YEAR(transaction_date) = YEAR(CURDATE()) AND MONTH(transaction_date) = MONTH(CURDATE())");
    $spentStmt->bind_param('is', $accountId, $category);
    $spentStmt->execute();
    $spentStmt->bind_result($spent);
    $spentStmt->fetch();
    $spentStmt->close();
    $remaining = (float)$budgetAmount - (float)$spent;
    if (((float)$spent + $amount) > (float)$budgetAmount + 0.00001) {
        throw new DomainException('Strict Mode blocked this expense. ' . $category . ' budget: ₹' . number_format((float)$budgetAmount, 2) . '; already spent: ₹' . number_format((float)$spent, 2) . '; remaining: ₹' . number_format(max(0, $remaining), 2) . '; attempted: ₹' . number_format($amount, 2) . '.');
    }
}
?>
