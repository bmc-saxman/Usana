package common

import actions.WelcomeToTheInternet
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class TestUsanaHome extends SpecBase
{
    @Shared WelcomeToTheInternet welcomeToTheInternet

    def "Setup the test: Step-0"()
    {
        when:
        // Start up the Selenium Manager code, this initialized the WebDriver
        startUI()

        welcomeToTheInternet = new WelcomeToTheInternet()

        then:
        true
    }

    def "Step-1"()
    {
        when:
        welcomeToTheInternet.open()

        then:
        /**
         * Images are difficult to test in Selenium because it doesn't have
         * comparators to work with images. Tools such as Sikuli can help
         * with these types of tests.
         */
        welcomeToTheInternet.isCanvasDisplayed()
    }
}
