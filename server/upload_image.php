<?php
if (count(get_included_files()) == 1) {
    define("DIRECT_ACCESS", __FILE__);
}

require("./settings.php");

function checkTableExists()
{
    $connection = mysqli_connect(HOST, USERNAME, PASSWORD);
    if ($connection->connect_error) {
        sendOutput(false, "Could not connect. " . $connection->connect_error);
    }

    $query = "CREATE DATABASE IF NOT EXISTS " . DATABASE . ";";

    if ($connection->query($query) === TRUE) {
        mysqli_select_db($connection, DATABASE);
    } else {
        sendOutput(false, "Could not execute \"" . $query . "\"" . $connection->error);
    }

    $query = "CREATE TABLE IF NOT EXISTS " . TABLE_NAME . " (
		    `ID` INT(11) PRIMARY KEY,
		    `HASH` VARCHAR(255) NOT NULL,
		    `FILE_NAME` VARCHAR(255) NOT NULL,
		    `FILE_TYPE` VARCHAR(255) NOT NULL,
		    `DATE_AND_TIME` DATETIME NOT NULL,
		    `WIDTH` INT(11) NOT NULL,
		    `HEIGHT` INT(11) NOT NULL,
		    `SIZE` VARCHAR(255) NOT NULL,
		    `TITLE` VARCHAR(65536) NOT NULL
		);";

    if ($connection->query($query) !== TRUE) {
        sendOutput(false, "Could not execute \"" . $query . "\"" . $connection->error);
    }

    $connection->close();
}

function addToDatabase($timestamp, $hashed, $fileName, $fileType, $width, $height, $size, $title)
{
    $connection = mysqli_connect(HOST, USERNAME, PASSWORD, DATABASE);
    if ($connection->connect_error) {
        sendOutput(false, "Could not connect. " . $connection->connect_error);
    }

    $query = "INSERT INTO `" . TABLE_NAME . "` VALUES (?, ?, ?, ?, FROM_UNIXTIME(?), ?, ?, ?, ?);";

    $statement = $connection->prepare($query);
    $statement->bind_param("isssiiiss", $timestamp, $hashed, $fileName, $fileType, $timestamp, $width, $height, $size, $title);
    $statement->execute();

    $statement->close();
    $connection->close();
}

function formatSizeUnits($bytes)
{
    if ($bytes >= 1073741824) {
        $bytes = number_format($bytes / 1073741824, 2) . ' GB';
    } elseif ($bytes >= 1048576) {
        $bytes = number_format($bytes / 1048576, 2) . ' MB';
    } elseif ($bytes >= 1024) {
        $bytes = number_format($bytes / 1024, 2) . ' kB';
    } elseif ($bytes > 1) {
        $bytes = $bytes . ' bytes';
    } elseif ($bytes == 1) {
        $bytes = $bytes . ' byte';
    } else {
        $bytes = '0 bytes';
    }

    return $bytes;
}

function checkAcceptedFileTypes($imageType)
{
    $acceptedFileTypes = array("png", "jpeg", "jpg", "gif");

    $split = explode('/', $imageType);
    $extension = end($split);

    if (!in_array($extension, $acceptedFileTypes)) {
        sendOutput(false, "File format not accepted. Must be one of: \"" . implode("/", $acceptedFileTypes) . "\".");
    }
}

function sendOutput($success, $output)
{
    $jsonObj = new stdClass();

    $jsonObj->success = $success;

    if ($success === true) {
        $jsonObj->output = $output;
    } else {
        $jsonObj->error = $output;
    }

    echo json_encode($jsonObj);

    die();
}

if (defined('DIRECT_ACCESS') && DIRECT_ACCESS == __FILE__) {
    if (!isset($_SERVER['HTTP_UPLOADPASSWORD']) || $_SERVER['HTTP_UPLOADPASSWORD'] !== UPLOAD_PASSWORD) {
        sendOutput(false, "Invalid password.");
    }

    checkTableExists();

    $title = isset($_SERVER['HTTP_TITLE']) ? $_SERVER['HTTP_TITLE'] : "";

    $imageName = isset($_FILES['uploaded_image']['name']) ? $_FILES['uploaded_image']['name'] : "";
    $imageType = isset($_FILES['uploaded_image']['type']) ? $_FILES['uploaded_image']['type'] : "";

    checkAcceptedFileTypes($imageType);

    $timestamp = round(microtime(true));

    $split = explode('/', $imageType);
    $extension = end($split);
    $size = formatSizeUnits($_FILES['uploaded_image']['size']);
    $hashed = sha1(SALT . $size . $timestamp);
    $imagePath = URL_PATH . $hashed . '.' . $extension;
    $fileName = $hashed . '.' . $extension;

    if (!move_uploaded_file($_FILES['uploaded_image']['tmp_name'], $imagePath)) {
        sendOutput(false, "File not uploaded. Upload directory is not writable, or does not exist.");
    }

    list($width, $height) = getimagesize($imagePath);

    addToDatabase($timestamp, $hashed, $fileName, $imageType, $width, $height, $size, $title);

    sendOutput(true, (isset($_SERVER['HTTPS']) ? "https" : "http") . "://" . $_SERVER["HTTP_HOST"] . "/" . URL_PATH . $hashed);
}
