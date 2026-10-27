
<?php
$host = "localhost";
$user = "root";
$password = "";
$database = "smartbank";

$conn = new mysqli($host, $user, $password, $database);

if ($conn->connect_error) {
    http_response_code(500);
    header("Content-Type: application/json");
    echo json_encode([
        "success" => false,
        "message" => "Database connection failed"
    ]);
    exit;
}

$conn->set_charset("utf8mb4");
?>
