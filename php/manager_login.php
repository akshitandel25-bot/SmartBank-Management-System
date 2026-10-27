
<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'db.php';

function respond($code, $data) {
    http_response_code($code);
    echo json_encode($data);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    respond(405, ['success' => false, 'message' => 'POST required']);
}

$username = trim($_POST['username'] ?? '');
$password = $_POST['password'] ?? '';

if ($username === '' || $password === '') {
    respond(400, ['success' => false, 'message' => 'Missing credentials']);
}

$stmt = $conn->prepare(
    'SELECT manager_id, password_hash FROM manager_users WHERE username = ?'
);
$stmt->bind_param('s', $username);
$stmt->execute();
$user = $stmt->get_result()->fetch_assoc();
$stmt->close();

if (!$user || !password_verify($password, $user['password_hash'])) {
    respond(401, ['success' => false, 'message' => 'Invalid credentials']);
}

$token = bin2hex(random_bytes(32));
$tokenHash = hash('sha256', $token);
$managerId = (int)$user['manager_id'];

$stmt = $conn->prepare(
    'INSERT INTO manager_sessions (manager_id, token_hash, expires_at)
     VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 2 HOUR))'
);
$stmt->bind_param('is', $managerId, $tokenHash);
$stmt->execute();
$stmt->close();

respond(200, [
    'success' => true,
    'token' => $token,
    'message' => 'Login successful'
]);
?>
