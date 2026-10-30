<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");

$db_host = "localhost";
$db_name = "user_data"; 
$db_user = "root";      
$db_pass = "";          

try {
    $pdo = new PDO("mysql:host=$db_host;dbname=$db_name;charset=utf8", $db_user, $db_pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $ex) {
    echo json_encode(["status" => "error", "message" => "DATABASE CONNECTION OFFLINE"]);
    exit;
}

$raw_input = file_get_contents("php://input");
$payload = json_decode($raw_input, true);

if (!$payload || !isset($payload['operation'])) {
    echo json_encode(["status" => "error", "message" => "EMPTY OR MALFORMED OPERATION"]);
    exit;
}

$operation = $payload['operation'];

if ($operation === 'register') {
    $user = trim($payload['username']);
    $pass = $payload['password'];

    if (empty($user) || empty($pass)) {
        echo json_encode(["status" => "error", "message" => "FIELDS CANNOT BE BLANK"]);
        exit;
    }

    $encrypted_pass = password_hash($pass, PASSWORD_DEFAULT);

    try {
        $pdo->beginTransaction();

        $queryUser = $pdo->prepare("INSERT INTO users (username, date_registered) VALUES (:username, NOW())");
        $queryUser->execute([':username' => $user]);
        $userId = $pdo->lastInsertId();

        $queryCred = $pdo->prepare("INSERT INTO credentials (user_id, password) VALUES (:user_id, :password)");
        $queryCred->execute([
            ':user_id' => $userId,
            ':password' => $encrypted_pass
        ]);

        $pdo->commit();
        echo json_encode(["status" => "success", "message" => "REGISTRATION COMPLETE! SUCCESS"]);
    } catch (PDOException $ex) {
        if ($pdo->inTransaction()) {
            $pdo->rollBack();
        }
        echo json_encode(["status" => "error", "message" => "USERNAME NOT AVAILABLE"]);
    }
}

if ($operation === 'login') {
    $user = trim($payload['username']);
    $pass = $payload['password'];

    try {
        $query = $pdo->prepare("SELECT u.id, u.username, c.password FROM users u JOIN credentials c ON u.id = c.user_id WHERE u.username = :username");
        $query->execute([':username' => $user]);
        $fetched_account = $query->fetch(PDO::FETCH_ASSOC);

        if ($fetched_account && password_verify($pass, $fetched_account['password'])) {
            // Write session bridge for the LibGDX game to poll
            file_put_contents("session.json", json_encode(["user_id" => $fetched_account['id'], "username" => $fetched_account['username']]));
            
            echo json_encode(["status" => "success", "message" => "ACCESS GRANTED", "user_id" => $fetched_account['id']]);
        } else {
            echo json_encode(["status" => "error", "message" => "INVALID ACCOUNT CREDENTIALS"]);
        }
    } catch (PDOException $ex) {
        echo json_encode(["status" => "error", "message" => "CRITICAL VERIFICATION EXCEPTION"]);
    }
}

if ($operation === 'check_session') {
    if (file_exists("session.json")) {
        $data = json_decode(file_get_contents("session.json"), true);
        unlink("session.json"); // Clear so it only triggers once
        echo json_encode(["status" => "success", "user_id" => $data['user_id'], "username" => $data['username']]);
    } else {
        echo json_encode(["status" => "not_found"]);
    }
    exit;
}

if ($operation === 'get_stats') {
    $userId = $payload['user_id'] ?? null;
    if (!$userId) {
        echo json_encode(["status" => "error", "message" => "MISSING USER ID"]);
        exit;
    }

    try {
        $recentStmt = $pdo->prepare("SELECT completion, cash_earned, waves_survived, monsters_killed FROM stats_history WHERE user_id = :uid ORDER BY played_at DESC LIMIT 1");
        $recentStmt->execute([':uid' => $userId]);
        $recent = $recentStmt->fetch(PDO::FETCH_ASSOC) ?: ["completion" => 0, "cash_earned" => 0, "waves_survived" => 0, "monsters_killed" => 0];

        $highestStmt = $pdo->prepare("SELECT MAX(completion) as completion, MAX(cash_earned) as cash_earned, MAX(waves_survived) as waves_survived, MAX(monsters_killed) as monsters_killed FROM stats_history WHERE user_id = :uid");
        $highestStmt->execute([':uid' => $userId]);
        $highest = $highestStmt->fetch(PDO::FETCH_ASSOC);
        if (!$highest || $highest['completion'] === null) {
            $highest = ["completion" => 0, "cash_earned" => 0, "waves_survived" => 0, "monsters_killed" => 0];
        }

        echo json_encode(["status" => "success", "recent" => $recent, "highest" => $highest]);
    } catch (PDOException $ex) {
        echo json_encode(["status" => "error", "message" => "FAILED TO FETCH STATS"]);
    }
}

if ($operation === 'save_stats') {
    $userId = $payload['user_id'] ?? null;
    if (!$userId) {
        echo json_encode(["status" => "error", "message" => "MISSING USER ID"]);
        exit;
    }

    try {
        $stmt = $pdo->prepare("INSERT INTO stats_history (user_id, completion, cash_earned, waves_survived, monsters_killed, played_at) VALUES (:uid, :comp, :cash, :waves, :monsters, NOW())");
        $stmt->execute([
            ':uid' => $userId,
            ':comp' => $payload['completion'] ?? 0,
            ':cash' => $payload['cash_earned'] ?? 0,
            ':waves' => $payload['waves_survived'] ?? 0,
            ':monsters' => $payload['monsters_killed'] ?? 0
        ]);
        echo json_encode(["status" => "success", "message" => "STATS SAVED"]);
    } catch (PDOException $ex) {
        echo json_encode(["status" => "error", "message" => "FAILED TO SAVE STATS"]);
    }
}
?>