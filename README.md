# Jyazo - A Screenshotting Application
This application allows for the screen capturing and uploading of 
stationary images (in .PNG format) and a collection of images (in .GIF 
format) in real time.

Included is the server-side .php scripts in the server folder, and the
code for the client-sided program is located in the client folder.

## Getting Started
### Server Side
In the `settings.php` script, you will need to change the global
variables at the top of the script.

Change the `HOST`, `USERNAME`, `PASSWORD`, `DATABASE`, and `TABLE_NAME`
variables to match your database information.

The variable defined as `URL_PATH` is the folder that the images will
be saved in. This folder will also contain the `index.php` script. By
default, this will be `ss/`. Please note, if you change this value in
the script, you **MUST** change the folder name as well. You will also
have to change the `/ss/` in the .htaccess on this line:
`RewriteRule ^(.*)$ /ss/index.php?path=$1 [NC,L,QSA]`.

For the `UPLOAD_PASSWORD`, `SALT`, and `ACCESS_PASSWORD` it is 
recommended you [generate a random password](https://passwordsgenerator.net/).

`UPLOAD_PASSWORD` is the password required for the screenshot to be uploaded.
This should be also set in the client side to validate that the image is being
uploaded from an authorized source.

`SALT` is the salt for generating the hash for the image URL.

`ACCESS_PASSWORD` is the password for being able to access the php page `ss/all.php'.
To access this page and view all your images, you should go to the URL as shown: 
`http://website.com/ss/all.php?p=ACCESS_PASSWORD`.

### Client Side
The client itself allows for uploading to any url host that accepts an
image with the POST key `uploaded_image`.

#### Manually Compile (skip to `No Compile` if you will download the JAR files instead)

To configure the upload URL and the password, either compile the JAR
file yourself with the main class as `tray.CreateTrayIcon`.

Ensure that the names of the JAR files match the value of the variable
`PROGRAM_NAME` in the class `tools.Constants`. The JAR should be named
``{{PROGRAM_NAME}}.jar``. An example of both compiled JARs are located 
in the
[releases section](https://github.com/sabihismail/Jyazo-Screenshot/releases/latest).

#### No Compile

Download the JAR file from the 
[releases section](https://github.com/sabihismail/Jyazo-Screenshot/releases/latest).

## Configure Settings
Open the JAR. An icon should appear in the tray. Right click this 
icon and click `Settings`. Click `Advanced Settings` and then input
the URL of where your `upload_image.php` script or equivalent direct 
URL is located. The server password below should match the global 
variable `UPLOAD_PASSWORD` in the `settings.php` file.

## TODO (in no particular order):
- [x] Create a more efficient GIF capture.
- [x] Change how capture works by creating one middleman overlay class
that passes information to both the CaptureImage and the CaptureGIF
class. This would cut down on some repeated code.
- [x] Experiment with smoother canvas drawing as the current method
(JavaFX Canvas embedded in Swing component is not very smooth).
- [ ] Allow for any text to be uploaded to the website instead of having 
to replace all non ASCII characters before sending data.
- [ ] Create a fancier index.php page for screenshot viewing.
- [x] Send region capture data to CaptureGIF so it only creates the GIF and
then saves the image.
- [x] Move Enable GIF from Config to Settings.
- [ ] Give the option to GIF upload to personal server rather than Gfycat.
- [ ] If upload fails, store image and queue upload at different time.
- [ ] For the overlay, change the startX, endX, startY, and endY variables
rather than doing a check of whether startX or endX is the smaller
value.
- [ ] Align image in center and resize larger images for index.php.
- [ ] Allow for Gfycat GIF embed in index.php page.
- [ ] Look for an alternative method to store the salt.
- [ ] Add an "include title" checkbox option in the settings.
- [ ] Add more exceptions to the logging.
- [ ] Move the uploaded_image.php api to the ss/api.php page.
- [ ] Potentially rewrite an an alternative language.

## Attributions
* Tray Icon Image (Name: Share Screen) by Chinnaking from the Noun 
Project. The image was resized, edited, and the colours were changed.
Link: https://thenounproject.com/search/?q=screen%20share&i=1050685
