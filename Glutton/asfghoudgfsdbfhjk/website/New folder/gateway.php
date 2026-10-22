<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");

// 1. Connection Configurations mapped directly to your phpMyAdmin Setup
$db_host = "localhost";
$db_name = "user_data"; // Matches database label
$db_user = "root";      
$db_pass = "";          // Default XAMPP structure relies on blank password

try {
    $pdo = new PDO("mysql:host=$db_host;dbname=$db_name;charset=utf8", $db_user, $db_pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $ex) {
    echo json_encode(["status" => "error", "message" => "DATABASE CONNECTION OFFLINE"]);
    exit;
}

// 2. Interpret Payload
$raw_input = file_get_contents("php://input");
$payload = json_decode($raw_input, true);

if (!$payload || !isset($payload['operation'])) {
    echo json_encode(["status" => "error", "message" => "EMPTY OR MALFORMED OPERATION"]);
    exit;
}

$operation = $payload['operation'];

// 3. Operation Processing
if ($operation === 'register') {
    $user = trim($payload['username']);
    $pass = $payload['password'];

    if (empty($user) || empty($pass)) {
        echo json_encode(["status" => "error", "message" => "FIELDS CANNOT BE BLANK"]);
        exit;
    }

    // Encrypt password natively to securely handle text validations later
    $encrypted_pass = password_hash($pass, PASSWORD_DEFAULT);

    try {
        // Matches structure columns: username, password (date_registered defaults automatically or can be inserted)
        $query = $pdo->prepare("INSERT INTO users (username, password, date_registered) VALUES (:username, :password, NOW())");
        $query->execute([
            ':username' => $user,
            ':password' => $encrypted_pass
        ]);
        echo json_encode(["status" => "success", "message" => "REGISTRATION COMPLETE! SUCCESS"]);
    } catch (PDOException $ex) {
        // Triggers error response if duplicate unique index username constraint is broken
        echo json_encode(["status" => "error", "message" => "USERNAME NOT AVAILABLE"]);
    }
}

if ($operation === 'login') {
    $user = trim($payload['username']);
    $pass = $payload['password'];

    try {
        $query = $pdo->prepare("SELECT * FROM users WHERE username = :username");
        $query->execute([':username' => $user]);
        $fetched_account = $query->fetch(PDO::FETCH_ASSOC);

        if ($fetched_account && password_verify($pass, $fetched_account['password'])) {
            echo json_encode(["status" => "success", "message" => "ACCESS GRANTED"]);
        } else {
            echo json_encode(["status" => "error", "message" => "INVALID ACCOUNT CREDENTIALS"]);
        }
    } catch (PDOException $ex) {
        echo json_encode(["status" => "error", "message" => "CRITICAL VERIFICATION EXCEPTION"]);
    }
}
?>
