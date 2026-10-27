
<?php
function requireManager($conn) {
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

    $tokenHash = hash('sha256', $match[1]);

    $stmt = $conn->prepare(
        'SELECT manager_id FROM manager_sessions
         WHERE token_hash = ? AND expires_at > NOW()'
    );
    $stmt->bind_param('s', $tokenHash);
    $stmt->execute();
    $session = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$session) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Session expired']);
        exit;
    }

    return (int)$session['manager_id'];
}
?>
