package common

import actions.WelcomeToTheInternet
import spock.lang.Shared
import spock.lang.Stepwise
import util.Text

@Stepwise
class TestThePage extends SpecBase
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

    def "Test 1"()
    {
        when:
        welcomeToTheInternet
            .open()
            .challengingDOM()

        then:
        /**
         * Images are difficult to test in Selenium because it doesn't have
         * comparators to work with images. Tools such as Sikuli can help
         * with these types of tests.
         */
        welcomeToTheInternet.isCanvasDisplayed()
    }

    def "Test 2"()
    {
        when:
        welcomeToTheInternet
            .open()
            .forgottenPassword("random@someplace.ext")

        then:
        true
    }

    def "Test 3"()
    {
        when:
        String text = welcomeToTheInternet
            .open()
            .abTesting()

        then:
        Text.findSubstring(text, "Also known as split testing")
    }

    def "Test 4"()
    {
        when:
        String text = welcomeToTheInternet
            .open()
            .largeDeepDOM()

        then:
        Text.findSubstring(text, "40.4")
    }
}