
<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db.php';

$headers = function_exists('getallheaders') ? getallheaders() : [];
$authorization = '';

foreach ($headers as $key => $value) {
    if (strtolower($key) === 'authorization') {
        $authorization = $value;
        break;
    }
}

if (!preg_match('/^Bearer\s+([a-f0-9]{64})$/i', $authorization, $match)) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Unauthorized']);
    exit;
}

$hash = hash('sha256', $match[1]);

$stmt = $conn->prepare(
    'DELETE FROM manager_sessions WHERE token_hash = ?'
);
$stmt->bind_param('s', $hash);
$stmt->execute();
$stmt->close();

echo json_encode(['success' => true, 'message' => 'Logged out']);
?>
