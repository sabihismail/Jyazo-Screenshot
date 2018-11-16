<?php
require("../settings.php");
define("MAXIMUM_COLUMNS", 4);

function format($rows)
{
    $dir = (stripos($_SERVER['SERVER_PROTOCOL'], 'https') === true ? 'https://' : 'http://') . $_SERVER['SERVER_NAME'] . "/ss/";

    $formatted = "<div class=\"parent\">";

    foreach ($rows as $row) {
        $url = $dir . $row["HASH"];

        $title = $row["TITLE"];
        $escapedTitle = addslashes($title);
        $filename = $row["FILE_NAME"];
        $hash = $row["HASH"];

        $formatted .= "<div class=\"column\"><div><p class=\"text\">" . $title . "</p><div class=\"image-buttons\"><p class=\"edit\" onclick=\"editImage('" . $hash . "', '" . $filename . "', '" . $escapedTitle . "')\">E</p><p class=\"delete\" onclick=\"xhrDeleteImage('" . $hash . "')\">X</p></div></div><img class=\"image\" onclick=\"copyToClipboard('" . $url . "')\" src=\"" . $filename . "\" /></div>";
    }

    $formatted .= "</div>\n";

    return $formatted;
}

function getScreenshots()
{
    $connection = mysqli_connect(HOST, USERNAME, PASSWORD, DATABASE);
    if ($connection->connect_error) {
        sendOutput(false, "Could not connect. " . $connection->connect_error);
    }

    $query = "SELECT HASH, FILE_NAME, DATE_AND_TIME, TITLE FROM " . TABLE_NAME . " ORDER BY DATE_AND_TIME DESC;";
    $result = $connection->query($query);

    $connection->close();

    if ($result->num_rows > 0) {
        $array = array();
        $tempArray = array();

        while ($row = $result->fetch_assoc()) {
            array_push($tempArray, $row);

            if (count($tempArray) === MAXIMUM_COLUMNS) {
                array_push($array, format($tempArray));

                $tempArray = array();
            }
        }

        return $array;
    } else {

    }

    return array();
}

$password = $_GET['p'];

if ($password === ACCESS_PASSWORD) {
    require("../upload_image.php");

    $screenshots = getScreenshots();
} else {

}
?>

<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="twitter:card" content="summary_large_image"/>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
          integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
    <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js"
            integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN"
            crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"
            integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q"
            crossorigin="anonymous"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"
            integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl"
            crossorigin="anonymous"></script>
    <script>
        function copyToClipboard(text) {
            var $temp = $("<input>");
            $("body").append($temp);
            $temp.val(text).select();
            document.execCommand("copy");
            $temp.remove();
        }

        function editImage(hash, src, oldTitle) {
            document.getElementById("editModalImage").src = src;
            document.getElementById("editModalHash").value = hash;
            document.getElementById("editModalNewTitle").value = oldTitle;

            $('#editModal').modal('show');
        }

        function updateImageTitle() {
            var hash = document.getElementById("editModalHash").value;
            var title = document.getElementById("editModalNewTitle").value;

            xhrUpdateImageTitle(hash, title);
        }

        function xhrUpdateImageTitle(hash, title) {
            var xhr = new XMLHttpRequest();

            var data = JSON.stringify({
                "p": new URL(window.location.href).searchParams.get("p"),
                "hash": hash,
                "title": title
            });

            xhr.open("POST", "./api.php", true);
            xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            xhr.onload = function() {
                window.location.reload();
            };

            xhr.send(data);
        }

        function xhrDeleteImage(hash) {
            var xhr = new XMLHttpRequest();

            var data = JSON.stringify({
                "p": new URL(window.location.href).searchParams.get("p"),
                "hash": hash
            });

            xhr.open("DELETE", "./api.php", true);
            xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
            xhr.onload = function() {
                window.location.reload();
            };

            xhr.send(data);
        }
    </script>
    <style>
        .parent {
            width: 100%;
        }

        .column {
            background: grey;
            float: left;
            width: 23%;
            padding: 1%;
            margin: 1%;
        }

        .image {
            width: 100%;
            cursor: pointer;
        }

        .text {
            display: inline-block;
            width: 80%;
            word-break: break-all;
            -webkit-hyphens: auto;
            -moz-hyphens: auto;
            hyphens: auto;
        }

        .image-buttons {
            float: right;
        }

        .input-fill {
            width: 100%;
        }
    </style>
</head>

<body>
<div class="modal fade" id="editModal" tabindex="-1" role="dialog" aria-labelledby="editModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Edit Image</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div><img id="editModalImage" src="data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs="/></div>
                <div><label for="editModalHash">Hash: </label><input class="input-fill" type="text" id="editModalHash" readonly></div>
                <div><label for="editModalNewTitle">New Title: </label><input class="input-fill" type="text" id="editModalNewTitle"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" onclick="updateImageTitle();">Save changes</button>
            </div>
        </div>
    </div>
</div>
<?php
foreach ($screenshots as $screenshot) {
    echo $screenshot;
}
?>
</body>
</html>
