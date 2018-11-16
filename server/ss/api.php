<?php
require("../settings.php");

function isNullOrEmpty($str){
    return !isset($str) || trim($str) === "";
}

function sendOutput($success, $output)
{
    $jsonObj = new stdClass();

    $jsonObj->success = $success;

    if (!$success) {
        $jsonObj->error = $output;
    }

    echo json_encode($jsonObj);

    die();
}

$data = json_decode(file_get_contents("php://input"), true);
if (!isNullOrEmpty($data["p"]) && !isNullOrEmpty($data["hash"]) && $data["p"] == ACCESS_PASSWORD) {
    $hash = $data["hash"];

    $connection = mysqli_connect(HOST, USERNAME, PASSWORD, DATABASE);
    if ($connection->connect_error) {
        sendOutput(false, "Could not connect. " . $connection->connect_error);
    }

    $query = "SELECT FILE_NAME FROM " . TABLE_NAME . " WHERE HASH = '" . $hash . "';";
    $result = $connection->query($query);

    if ($result->num_rows > 0) {
        $screenshot = (object)$result->fetch_assoc();

        if ($_SERVER["REQUEST_METHOD"] === "POST" && !isNullOrEmpty($data["title"])) {
            $title = $data["title"];

            $query = "UPDATE " . TABLE_NAME . " SET TITLE = ? WHERE HASH = ?;";

            $statement = $connection->prepare($query);
            $statement->bind_param("ss", $title, $hash);
            $updateResult = $statement->execute();

            $statement->close();
            $connection->close();

            if (!$updateResult) {
                sendOutput(false, "Could not execute \"" . $query . "\"" . $connection->error);
            } else {
                sendOutput(true, "true");
            }
        } else if ($_SERVER["REQUEST_METHOD"] === "DELETE") {
            $query = "DELETE FROM " . TABLE_NAME . " WHERE HASH = ?;";

            $statement = $connection->prepare($query);
            $statement->bind_param("s", $hash);
            $deleteResult = $statement->execute();

            $statement->close();
            $connection->close();

            if (!$deleteResult) {
                sendOutput(false, "Could not execute \"" . $query . "\"" . $connection->error);
            } else {
                $pathOfFile = "../" . URL_PATH . $screenshot->FILE_NAME;

                unlink($pathOfFile);

                sendOutput(true, "true");
            }
        } else {
            $connection->close();
        }
    } else {
        $connection->close();
    }
} else {
    sendOutput(false, "Invalid password or payload.");
}
