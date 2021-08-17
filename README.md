# Usana
Usana assessment

I have attempted to make a rather extensible framework from my previous
experiences.

In the properties.ini file you can set the browser, i.e. googlechrome or firefox
at this time but others could be added by also modifying the SeleniumManager
class code to add the appropriate browser initialization code.

You can also specify the chromedriver version. At this time I have only 
included version 90, 01, and 92. 92 being the latest.

The test can be run using the gradlew command line.

gradlew test --tests TestThePage
