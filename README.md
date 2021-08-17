# Usana
Usana assessment

- I have attempted to make a rather extensible framework from my current
experiences.

- The project assumes that Java JDK is installed. It must be at least 1.8 
or newer.

- In the properties.ini file you can set the browser, i.e. googlechrome or firefox
at this time but others could be added by also modifying the SeleniumManager
class code to add the appropriate browser initialization code. I have done most
of my work on the chrome browser and did not test Firefox at this time. So please
use the chrome browser for now.

- You can also specify the chromedriver version. At this time I have only 
included versions 90, 91, and 92. 92 being the latest.

- To run on Linux requires the xDisplay value to be set. To run on the main 
screen it should be set to :0, but can be set to other values if there is a
graphics system like xvfb or Xephyr installed and running. Then it could be
set to :1, or any other virtual value that the graphics system supports.

- The test can be run using the gradlew command line.

- gradlew test --tests TestThePage
