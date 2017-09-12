<?php
	function getScreenshot($slug) {
		$connection = mysqli_connect(HOST, USERNAME, PASSWORD, DATABASE);
		if ($connection->connect_error) {
			sendOutput (false, "Could not connect. " . $connection->connect_error);
		}

		$query = "SELECT * FROM " . TABLE_NAME . " WHERE `HASH` = \"" . $slug . "\";";
		$result = $connection->query($query);

		$connection->close();
		
		if ($result->num_rows == 1) {
			$screenshot = (object) $result->fetch_assoc();

			return $screenshot;
		} else {
			return sendErrorPage();
		}
	}
	
	function sendErrorPage() {
		$screenshot = (object) ["TITLE" => "This page doesn't exist!",
								"FILE_NAME" => "fail.png",
								"DATE_AND_TIME" => 0,
								"WIDTH" => 0,
								"HEIGHT" => 0,
								"SIZE" => "ERROR!",
								"FILE_TYPE" => "ERROR!"];
								
		return $screenshot;
	}

	$split = explode("/", $_SERVER["REQUEST_URI"]);
	$dir = (stripos($_SERVER['SERVER_PROTOCOL'],'https') === true ? 'https://' : 'http://') . $_SERVER['SERVER_NAME'] . dirname($_SERVER['REQUEST_URI']) . "/";
	$slug = end($split);

	if (strlen($slug) === 40) {
		require("../upload_image.php");

		$screenshot = getScreenshot($slug);
	} else {
		$screenshot = sendErrorPage();
	}
?>


<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
 <?php 
if ($screenshot->TITLE !== "") {
?>
	<title><?php echo $screenshot->TITLE ?></title>
	<meta property="og:title" content="<?php echo $screenshot->TITLE ?>"/>
	<meta name="twitter:title" content="<?php echo $screenshot->TITLE ?>"/>
<?php
}
?>
	<meta property="og:image" content="<?php echo $dir . $screenshot->FILE_NAME ?>"/>
	<meta name="twitter:image" content="<?php echo $dir . $screenshot->FILE_NAME ?>"/>
	<meta name="twitter:card" content="summary_large_image"/>
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
	<style>
		.jumbotron{
		    text-align:center;
		}
		.jumbotron h1{
			margin-top:20px;
			margin-bottom:20px;
		}
		.jumbotron p{
			margin-top:20px;
			margin-bottom:20px;
		}
	</style>
</head>

<body>
	<div class="jumbotron">
	  <div class="container">
	  	<?php 
		  	if ($screenshot->TITLE !== "") {
		  		echo "<h1>" . $screenshot->TITLE . "</h1>";
		  	}
	  	?>

	  	<p><img src=<?php echo $screenshot->FILE_NAME ?>></img></p>
	  	<h4>Date: 
	  	<?php 
	  		$sqlDate = strtotime($screenshot->DATE_AND_TIME);
	  		echo date('F jS, Y', $sqlDate) . " at " . date('h:i:s a', $sqlDate);
	  	?></h4>
	  	<h4>Dimensions: <?php echo $screenshot->WIDTH . " x " . $screenshot->HEIGHT . "px" ?></h4>
	  	<h4>File Type: <?php echo $screenshot->FILE_TYPE ?></h4>
	  	<h4>File Size: <?php echo $screenshot->SIZE ?></h4>
	  </div>
	</div>
</body>