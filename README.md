# Jyazo - A Screenshotting Application
This application allows for the screen capturing and uploading of 
stationary images (in .PNG format) and a collection of images (in .GIF 
format) in real time.

Included is the server-side .php scripts in the server folder, and the
code for the client-sided program is located in the client folder.

## Getting Started
### Server Side
In the `upload_image.php` script, you will need to change the global
variables at the top of the script.

Change the `HOST`, `USERNAME`, `PASSWORD`, `DATABASE`, and `TABLE_NAME`
variables to match your database information.

The variable defined as `URL_PATH` is the folder that the images will
be saved in. This folder will also contain the `index.php` script. By
default, this will be `ss/`. Please note, if you change this value in
the script, you **MUST** change the folder name as well.

For the `UPLOAD_PASSWORD` and `SALT`, it is recommended you
[generate a random password](https://passwordsgenerator.net/).

### Client Side
The client itself allows for uploading to any url host that accepts an
image with the POST name `uploaded_image`.

#### Manually Compile (skip to `No Compile` if you will download the JAR files instead)

To configure the upload URL and the password, either compile two JAR
files yourself: one with the main class as `tray.CreateTrayIcon`, and
one with the main class as `captureGIF.CaptureGIF`.

Ensure that the names of the JAR files match the value of the variable
`PROGRAM_NAME` in the class `tools.Constants`. The
`tray.CreateTrayIcon` JAR should be named ``{{PROGRAM_NAME}}.jar`` and
the compiled GIF jar should be named `{{PROGRAM_NAME}}GIF.jar`. An
example of both compiled JARs are located in the `compiled` folder.

#### No Compile

Download both JAR files from the `compiled` folder.

## Configure Settings
Open the JAR with no GIF in the name. An icon should appear in the
tray. Right click this icon and click `Settings`. Click
`Advanced Settings` and then input the URL of where your
`upload_image.php` script or equivalent direct URL is
located. The server password below should match the global variable
`UPLOAD_PASSWORD` in the `upload_image.php` file.

## TODO (in no particular order):
* Create a more efficient GIF capture. Possibly experiment with JNA to 
do this.
* Change how capture works by creating one middleman overlay class
that passes information to both the CaptureImage and the CaptureGIF
class. This would cut down on some repeated code.
* Experiment with smoother canvas drawing as the current method
(JavaFX Canvas embedded in Swing component is not very smooth).
* Allow for any text to be uploaded to the website instead of having 
to replace all non ASCII characters before sending data.
* Create a fancier index.php page for screenshot viewing.
* Send region capture data to CaptureGIF so it only creates the GIF and
then saves the image. Will possibly use Remote Method Invocation for
this.
* Move Enable GIF from Config to Settings.
* Give the option to GIF upload to personal server rather than Gfycat.
* If upload fails, store image and queue upload at different time.
* For the overlay, change the startX, endX, startY, and endY variables
rather than doing a check of whether startX or endX is the smaller
value.
* Align image in center and resize larger images for index.php.
* Allow for Gfycat GIF embed in index.php page.
* Look for an alternative method to store the salt.
* Add an "include title" checkbox option in the settings.
* Add more exceptions to the logging.

## Attributions
* Tray Icon Image (Name: TV Screen Share Icon) by Bastien Delmare from
the Noun Project. The image was resized.
Link: https://thenounproject.com/search/?q=screen%20share&i=986547