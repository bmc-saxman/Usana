package util;

import net.sourceforge.htmlunit.corejs.javascript.JavaScriptException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the util.SeleniumManager utilities.
 * @author bchristiansen
 */
public abstract class SeleniumBase
{
    protected WebDriver driver;
    protected String mainWindow;
    protected List<String> messageLog = new ArrayList<>();

    /**
     * Waits for an alert to display and then accepts it.
     */
    public void acceptAlert()
    {
        try
        {
            if (!waitForAlert(SystemProperties.sVal, false))
                throw new Exception();

            // Add the message text to a log.
            Alert alert = driver.switchTo().alert();
            messageLog.add(alert.getText());
            alert.accept();
            waitForAlertNotPresent(SystemProperties.sVal, false);
        } catch (Exception ex)
        {
            System.out.print("An alert was not displayed, continuing.");
        }
    }

    /**
     * Waits for an alert to display and then cancels it.
     */
    public void cancelAlert()
    {
        try
        {
            if (!waitForAlert(SystemProperties.sVal, false))
                throw new Exception();

            // Add the message text to a log.
            Alert alert = driver.switchTo().alert();
            messageLog.add(alert.getText());
            alert.dismiss();
            waitForAlertNotPresent(SystemProperties.sVal, false);
        } catch (Exception ex)
        {
            System.out.print("An alert was not displayed, continuing.");
        }
    }

    /**
     * This method will clear the text out of a field by sending backspaces
     * to it. The WebDriver.clear() method has caused some issues with fields
     * where a drop down list is dynamically displayed. Calling clear() somehow
     * causes the display of the dynamic drop down to fail.
     *
     * @param locator - The By locator of the field.
     * @param length  - The length of the text that might be in the field.
     */
    public void clear(By locator, int length)
    {
        WebElement item = driver.findElement(locator);

        clear(item, length);
    }

    /**
     * This method will clear the text out of a field by sending backspaces
     * to it. The WebDriver.clear() method has caused some issues with fields
     * that are dynamically displayed or are not interactable.
     *
     * @param item   - The WebElement object.
     * @param length - The length of the text that could be in the field.
     */
    public void clear(WebElement item, int length)
    {
        // Send backspaces to clear the text.
        String text;
        Actions action = new Actions(driver);
        int size = length;
        while (size > 0)
        {
            action
                .sendKeys(item, Keys.END)
                .sendKeys(item, Keys.BACK_SPACE)
                .build().perform();
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException ie)
            {
                System.out.print("Wait Interrupted");
            }
            text = item.getAttribute("value");
            size = text.length();
        }
    }

    public void click(By locator)
    {
        click(locator, 0);
    }

    /**
     * Finds the element and clicks it.
     *
     * @param locator  The locator to find the element to click on.
     * @param scroll_y The amount to scroll the window down in pixels.
     */
    public void click(By locator, int scroll_y)
    {
        click(driver.findElement(locator), 0, scroll_y);
    }


    public void click(WebElement item)
    {
        click(item, 0, 0);
    }

    /**
     * Click the WebElement.
     *
     * @param item     The item to be scrolled.
     * @param scroll_x The integer value to be scrolled horizontally.
     * @param scroll_y The integer value to be scrolled vertically.
     */
    public void click(WebElement item, int scroll_x, int scroll_y)
    {
        click(item, scroll_x, scroll_y, false);
    }

    /**
     * Click the WebElement.
     *
     * @param item         The item to be scrolled.
     * @param scroll_x     The integer value to be scrolled horizontally.
     * @param scroll_y     The integer value to be scrolled vertically.
     * @param asynchronous Click the button asynchronously
     */
    public void click(WebElement item, int scroll_x, int scroll_y, boolean asynchronous)
    {
        waitForExists(item);
        if (!isWebElementVisibleOnScreen(item))
        {
            // Make sure the object is scrolled into view.
            scrollItemIntoView(item);
            scrollWindow(scroll_x, scroll_y);
        }
        // Wait for the item to become visible and clickable.
        waitForClickable(item);

        // Perhaps wait for the object to be in a refreshed state, attached to the DOM.
        waitForRefreshed(item);

        // Using java script to click the button asynchronously
        if (asynchronous)
        {
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("var elem=arguments[0]; setTimeout(function() {elem.click();}, 100)", item);
        }
        else
        {
            try
            {
                item.click();
            } catch (Exception ex)
            {
                scrollItemIntoView(item);
                item.click();
            }
        }

        Utils.wait(SystemProperties.sVal);
    }

    /**
     * Performs a click, waits for a popup to display, and then switches to it.
     *
     * @param element The element to be clicked on.
     */
    public void clickAndSwitchToPopup(WebElement element)
    {
        Set<String> before = driver.getWindowHandles();
        click(element);
        Utils.wait(1);
        driver.switchTo().window(waitForPopup(before));
    }

    /**
     * Is the element passed into the method stale? Checks by clicking on the
     * element, so if not stale it will select the item.
     *
     * @param element The element to be checked.
     * @return {@link Boolean}
     */
    public boolean clickIfNotStale(WebElement element)
    {
        try
        {
            element.click();
            return false;
        } catch (StaleElementReferenceException sere)
        {
            return true;
        }
    }

    /**
     * Check for the existence of an object.
     *
     * @param item The WebElement that is being checked.
     * @return {@link Boolean}
     */
    public boolean elementExists(WebElement item)
    {
        boolean found = false;
        try
        {
            List<WebElement> elements = driver.findElements(By.xpath(getElementXPath(item)));
            found = !elements.isEmpty();
        } catch (Exception ex)
        {
            found = false;
        }
        return found;
    }

    /**
     * Check for the existence of an object. This will wait the timeout period, polling at some fixed intervals
     * until the timeout has passed.
     *
     * @param locator The By locator of the element.
     * @param timeout - The time to wait.
     * @return {@link Boolean}
     */
    public boolean elementExists(By locator, int timeout)
    {
        WebElement foo = null;
        try
        {
            foo = this.getElementByLocator(locator, timeout);
        } catch (TimeoutException te)
        {
            System.out.print("There was a timeout looking for element: " + locator.toString());
            return false;
        } catch (ElementNotVisibleException env)
        {
            System.out.print("The element was found but is invisible: " + locator.toString());
            return false;
        }

        return foo != null;
    }

    /**
     * Check for the visibility of an element
     *
     * @param element The element to be checked.
     * @return True if the element is visible; Otherwise, false
     */
    public boolean elementIsVisible(WebElement element)
    {
        try
        {
            waitForExists(element);
            return true;
        } catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Method to find and return the visible element in a list of WebElements.
     * Should only be used in the case that the only element expected to be visible is
     * the one you are targeting for action. If multiple visible elements exist in the
     * list then you are not guaranteed to find the correct one.
     *
     * @param elementList {@link List}
     * @return {@link WebElement}
     */
    public static WebElement findVisibleElement(List<WebElement> elementList)
    {
        WebElement visibleElement;
        for (WebElement e : elementList)
        {
            if (e.isDisplayed())
            {
                visibleElement = e;
                return visibleElement;
            }
        }

        // If a visible element was not returned, wait and try again.
        Utils.wait(SystemProperties.sVal);
        for (WebElement e : elementList)
        {
            if (e.isDisplayed())
            {
                visibleElement = e;
                return visibleElement;
            }
        }

        // If there is still no visible element, throw an exception.
        throw new NoSuchElementException("No visible element found from list: " + elementList);
    }

    /**
     * Takes a {@link WebElement} and attempts to convert it into a By locator by
     * converting it into a string and switching on the selector method.
     *
     * @param element {@link WebElement} of the item you wish to convert
     * @return {@link By} locator of the {@link WebElement}
     */
    public By getByFromWebElement(WebElement element)
    {
        By by;
        //[[ChromeDriver: chrome on XP (d85e7e220b2ec51b7faf42210816285e)] -> xpath: //input[@title='Search']]
        String eleString = element.toString();
        String[] pathVariables = (eleString.split("->")[1]
            .replaceFirst("(?s)(.*)\\]", "$1" + ""))
            .split(":");

        String selector = pathVariables[0].trim().replace(":", "");
        String value = pathVariables[1].trim();

        switch (selector)
        {
            case "id":
                by = By.id(value);
                break;

            case "className":
                by = By.className(value);
                break;

            case "tagName":
                by = By.tagName(value);
                break;

            case "xpath":
                pathVariables = (eleString.split("xpath:"));
                value = pathVariables[1].trim();
                value = value.substring(0, value.length() - 1);
                by = By.xpath(value);
                break;

            case "css selector":
                pathVariables = (eleString.split("css selector:"));
                value = pathVariables[1].trim();
                value = value.substring(0, value.length() - 1);
                by = By.cssSelector(value);
                break;

            case "linkText":
                by = By.linkText(value);
                break;

            case "name":
                by = By.name(value);
                break;

            case "partialLinkText":
                by = By.partialLinkText(value);
                break;

            default:
                throw new IllegalStateException("locator : " + selector + " not found!!!");
        }
        return by;
    }

    /**
     * Get the column's index based on the name of the column.
     *
     * @param table      The table to be used.
     * @param columnName The name of the column to be found.
     * @return {@link Integer}
     */
    public int getColumnIndex(WebElement table, String columnName)
    {
        int column = 0;

        List<WebElement> header = table.findElements(By.xpath("thead/tr/th"));
        for (int i = 1; i <= header.size(); i++)
        {
            String name = table.findElement(By.xpath("thead/tr/th[" + i + "]")).getText().trim();
            if (name.equalsIgnoreCase(columnName))
            {
                column = i;
                break;
            }
        }

        return column;
    }

    private WebElement getElementByLocator(By locator, int timeout)
    {
        int interval = 5;
        if (timeout <= 20) interval = 3;
        if (timeout <= 10) interval = 2;
        if (timeout <= 4) interval = 1;

        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(interval, TimeUnit.SECONDS)
            .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Return an element's xpath.
     * Here's the code written out:
     * <pre>
     * {@code
     *
     * function getElementXPath(elt)
     * {
     *    var path = \"\";
     *    for (; elt && elt.nodeType == 1; elt = elt.parentNode)
     *    {
     *        idx = getElementIdx(elt);
     *        xname = elt.tagName;
     *        xname += \"[\" + idx + \"]\";
     *        path = \"/\" + xname + path;
     *    }
     *    return path;
     * }
     *
     * function getElementIdx(elt)
     * {
     *    var count = 1;
     *    for (var sib = elt.previousSibling; sib; sib = sib.previousSibling)
     *    {
     *        if (sib.nodeType == 1 && sib.tagName == elt.tagName)
     *        {
     *            count++;
     *        }
     *    }
     *    return count;
     * }
     * }</pre>
     *
     * @param element The WebElement in question.
     * @return The xpath of the element.
     */
    public String getElementXPath(WebElement element)
    {
        String javaScript = "function getElementXPath(elt){" +
            "var path = \"\";" +
            "for (; elt && elt.nodeType == 1; elt = elt.parentNode){" +
            "idx = getElementIdx(elt);" +
            "xname = elt.tagName;" +
            "xname += \"[\" + idx + \"]\";" +
            "path = \"/\" + xname + path;" +
            "}" +
            "return path;" +
            "}" +
            "function getElementIdx(elt){" +
            "var count = 1;" +
            "for (var sib = elt.previousSibling; sib ; sib = sib.previousSibling){" +
            "if(sib.nodeType == 1 && sib.tagName == elt.tagName){" +
            "count++;" +
            "}" +
            "}" +
            "return count;" +
            "}" +
            "return getElementXPath(arguments[0]).toLowerCase();";

        return (String) ((JavascriptExecutor) driver).executeScript(javaScript, element);
    }

    /**
     * Get the column's index based on the name of the column even if the table does not have a table head.
     *
     * @param table      The table to be used.
     * @param columnName The name of the column to be found.
     * @return {@link Integer}
     */
    public int getHeadlessTableColumnIndex(WebElement table, String columnName)
    {
        int column = 0;

        List<WebElement> header = table.findElements(By.xpath("tbody/tr[1]/td"));
        for (int i = 1; i <= header.size(); i++)
        {
            String name = table.findElement(By.xpath("tbody/tr[1]/td[" + i + "]")).getText().trim();
            if (name.equalsIgnoreCase(columnName))
            {
                column = i;
                break;
            }
        }

        return column;
    }

    /**
     * Returns a wait object that will wait for 20 seconds.
     *
     * @return A 20 second long WebDriverWait instance.
     */
    public WebDriverWait getLongWait()
    {
        //return longWait;
        return SystemProperties.getLongWait();
    }

    /**
     * Returns a wait object that will wait for 300 seconds.
     *
     * @return A 300 second long WebDriverWait instance.
     */
    public WebDriverWait getxLongWait()
    {
        //return xLongWait;
        return SystemProperties.getxLongWait();
    }

    /**
     * Returns a wait object that will wait for 10 seconds.
     *
     * @return A 10 second long WebDriverWait instance.
     */
    public WebDriverWait getMediumWait()
    {
        //return mediumWait;
        return SystemProperties.getMediumWait();
    }

    /**
     * Get the message log.
     *
     * @return {@link List}
     */
    public List<String> getMessageLog()
    {
        return messageLog;
    }

    /**
     * Gets the parameters from the query portion of the current URL.
     *
     * @return A map with a key value pair for each of the parameters.
     */
    public java.util.Map<String, String> getQueryParams()
    {
        String url = driver.getCurrentUrl();
        try
        {
            java.util.Map<String, String> params = new java.util.HashMap<String, String>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1)
            {
                String query = urlParts[1];
                for (String param : query.split("&"))
                {
                    String[] pair = param.split("=");
                    String key = java.net.URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1)
                    {
                        value = java.net.URLDecoder.decode(pair[1], "UTF-8");
                    }

                    params.put(key, value);
                }
            }

            return params;
        } catch (java.io.UnsupportedEncodingException ex)
        {
            throw new AssertionError(ex);
        }
    }

    /**
     * Returns a wait object what will wait for 2 seconds.
     *
     * @return A 2 second long WebDriverWait instance.
     */
    public WebDriverWait getShortWait()
    {
        //return shortWait;
        return SystemProperties.getShortWait();
    }

    /**
     * Finds the first row that contains the specified text and returns the WebElement
     * for that row. This is not case sensitive.
     *
     * @param table The By locator to find the table.
     * @param text  The text that the row should contain.
     * @return The WebElement for the row that contains the matching text, or <tt>null</tt>
     * if the text wasn't found in the table.
     */
    public WebElement getTableRow(By table, String text)
    {
        return getTableRow(driver.findElement(table), text);
    }

    /**
     * Finds the first row that contains the specified text and returns the WebElement
     * for that row. This is not case sensitive.
     *
     * @param table - A WebElement reference to the needed table.
     * @param text  - The text of the element in the table that is to be found.
     * @return - The row corresponding to the text.
     */
    public WebElement getTableRow(WebElement table, String text)
    {
        // Lower case the text
        text = text.toLowerCase();

        // Make sure the table is in place.
        waitForExists(table);

        // Get the table's tbody
        WebElement el;
        try
        {
            el = table.findElement(By.tagName("tbody"));
        } catch (NoSuchElementException ex)
        {
            return null;
        }

        // Get the rows
        List<WebElement> rows = el.findElements(By.tagName("tr"));

        // Loop through the rows until we find a row that contains the text
        for (WebElement row : rows)
        {
            String rowText = row.getText().toLowerCase();
            if (rowText.contains(text))
                return row;
        }

        // Not found
        return null;
    }

    /**
     * This method returns the Selenium WebDriver.
     *
     * @return The WebDriver instance.
     */
    public WebDriver getWebDriver()
    {
        return driver;
    }

    /**
     * Checks to see whether an alert window is present.
     *
     * @return <tt>true</tt> if the alert is present, otherwise <tt>false</tt>.
     */
    public boolean isAlertPresent()
    {
        try
        {
            Alert alert = driver.switchTo().alert();
            messageLog.add(alert.getText());
            return true;
        } catch (NoAlertPresentException Ex)
        {
            return false;
        }
    }

    /**
     * Checks to see if an element is currently viewable on screen by first getting the window size and making sure the
     * locator falls within there. The minus 20 is to ensure that the item is visible even if there is a border on the edges.
     *
     * @param element Webelement object to look for
     * @return <tt>true</tt> If element is within the viewable screen
     */
    public boolean isWebElementVisibleOnScreen(WebElement element)
    {
        Dimension elementSize = element.getSize();
        Point elementLocation = element.getLocation();
        Dimension windowSize = driver.manage().window().getSize();

        int x = windowSize.getWidth() - 20;
        int y = windowSize.getHeight() - 20;
        int x2 = elementSize.getWidth() + elementLocation.getX();
        int y2 = elementSize.getHeight() + elementLocation.getY();

        return x2 <= x && y2 <= y;
    }

    /**
     * Click the element with the JavaScriptExecutor.
     *
     * @param locator The By locator for the element that needs to be clicked.
     */
    public void jsClick(By locator)
    {
        jsClick(driver.findElement(locator));
    }

    /**
     * Click the element with the JavaScriptExecutor.
     *
     * @param elem The WebElement that needs to be clicked.
     */
    public void jsClick(WebElement elem)
    {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", elem);
        try
        {
            Thread.sleep(1000L);
        } catch (InterruptedException ie)
        {
            System.out.print("For some reason the click did not work. Message: " + ie.getMessage());
            /* ignore */
        }
    }

    /**
     * <pre>
     * {@code
     *  if (document.createEvent)
     *  {
     *      var evObj = document.createEvent('MouseEvents');
     *      evObj.initEvent('mouseover',true , false );
     *      arguments[0].dispatchEvent(evObj);
     *  } else if (document.createEventObject)
     *  {
     *      arguments[0].fireEvent('onmouseover');
     *  }
     * }</pre>
     * <p>
     * Use this script to open the hover windows in SF Lightning.
     *
     * @param hoverElement The element where the hover window should be triggered.
     */
    public void mouseOver(WebElement hoverElement)
    {
        SeleniumManager sm = SystemProperties.getSeleniumManager();
        WebDriver driver = sm.getWebDriver();
        try
        {
            String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover',true , false ); arguments[0].dispatchEvent(evObj);} else if (document.createEventObject) { arguments[0].fireEvent('onmouseover'); }";
            ((JavascriptExecutor) driver).executeScript(mouseOverScript, hoverElement);
        } catch (JavaScriptException e)
        {
            System.out.print("Element with " + hoverElement + " is not attached to the page document: " + e.getStackTrace().toString());
        }
    }

    /**
     * Make sure that the item being interacted with is scrolled into view.
     *
     * @param item The item being interacted with.
     */
    public void scrollItemIntoView(WebElement item)
    {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", item);
    }

    /**
     * Scrolls the item into the viewable region, less the offset.
     * </br>
     * <pre>
     * {@code
     *
     * function scrollElmVert(el,num)
     * {
     *      // to scroll up use a negative number
     *      var re=/html$/i;
     *      while (!re.test(el.tagName) && (1 > el.scrollTop))
     *          el = el.parentNode;
     *      if (0 < el.scrollTop)
     *          el.scrollTop += num;
     *  }
     * }</pre>
     *
     * @param item   The item being interacted with.
     * @param offset The offset to scroll less than.
     */
    public void scrollItemIntoView(WebElement item, int offset)
    {
        String javaScript = "function scrollElmVert(el,num) { // to scroll up use a negative number\n" +
            "  var re=/html$/i;\n" +
            "  while(!re.test(el.tagName) && (1 > el.scrollTop)) el=el.parentNode;\n" +
            "  if(0 < el.scrollTop) el.scrollTop += num;\n" +
            "}\n" +
            "return scrollElmVert(arguments[0], arguments[1]);";

        // This scroll puts the item in the needed position first.
        scrollItemIntoView(item);

        // Now we can scroll the item down where we want it.
        ((JavascriptExecutor) driver).executeScript(javaScript, item, offset);
    }

    /**
     * Scroll the window by X and Y.
     *
     * @param x How far in the x-axis you want to scroll.
     * @param y How far in the y-axis you want to scroll.
     */
    public void scrollWindow(int x, int y)
    {
        // Execute a scroll command.
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("javascript:window.scrollBy(" + x + ", " + y + ")");
    }

    /**
     * Selects an element inside an HTML select list.
     *
     * @param locator The {@link By locator} to locate the select.
     * @param index   The index to select.
     * @return The WebElement that was selected.
     */
    public WebElement select(By locator, int index)
    {
        Select slct = null;
        waitForExists(locator);
        try
        {
            slct = new Select(driver.findElement(locator));
            slct.selectByIndex(index);
        } catch (Exception ex)
        {
            System.out.print("Unable to find the Select list locator: " + locator.toString() +
                " --- Stack Trace: " + ExceptionUtils.getStackTrace(ex));
            throw ex;
        }
        return slct.getFirstSelectedOption();
    }

    /**
     * Selects an element inside an HTML select list.
     *
     * @param locator     The {@link By locator} to locate the select.
     * @param visibleText The visible text of the option to select.
     */
    public void select(By locator, String visibleText)
    {
        waitForExists(locator);
        try
        {
            Select slct = new Select(driver.findElement(locator));
            slct.selectByVisibleText(visibleText);
        } catch (Exception ex)
        {
            System.out.print("Unable to find the Select list locator : " + locator.toString() +
                " --- Stack Trace: " + ExceptionUtils.getStackTrace(ex));
            throw ex;
        }
    }

    /**
     * Selects an element inside an HTML select list.
     *
     * @param element     The WebElement referencing the Select list.
     * @param visibleText The visible text of the option to select.
     */
    public void select(WebElement element, String visibleText)
    {
        waitForExists(element);
        try
        {
            Select slct = new Select(element);
            slct.selectByVisibleText(visibleText);
        } catch (Exception ex)
        {
            System.out.print("Unable to find the Select list: " + element.toString() +
                " --- Stack Trace: " + ExceptionUtils.getStackTrace(ex));
            throw ex;
        }
    }

    /**
     * Selects all elements in a multi-select list.
     *
     * @param locator The locator used to locate the select.
     * @apiNote There is a deselectAll() method in Selenium's Select class
     */
    public void selectAll(By locator)
    {
        Select slct = new Select(driver.findElement(locator));

        List<WebElement> listItems;

        slct.deselectAll();
        listItems = slct.getOptions();

        // Select all the items in the list.
        if (!listItems.isEmpty())
        {
            Actions action = new Actions(driver);
            action
                .keyDown(Keys.SHIFT)
                .click(listItems.get(0))
                .click(listItems.get(listItems.size() - 1))
                .keyUp(Keys.SHIFT)
                .build()
                .perform();
        }
    }

    /**
     * Select all elements in a multi-select list.
     *
     * @param selectList The WebElement object corresponding to the Select item.
     */
    public void selectAll(WebElement selectList)
    {
        Select list = new Select(selectList);
        List<WebElement> listItems = list.getOptions();

        // Select all the items in the list.
        if (!listItems.isEmpty())
        {
            Actions action = new Actions(driver);
            action
                .keyDown(Keys.SHIFT)
                .click(listItems.get(0))
                .click(listItems.get(listItems.size() - 1))
                .keyUp(Keys.SHIFT)
                .build()
                .perform();
        }
    }

    /**
     * Sets the selection state of a checkbox.
     *
     * @param locator - The By locator of the checkbox.
     * @param state   - The state that the checkbox should be.
     *                <tt>true</tt> for checked, <tt>false</tt> for un-checked.
     */
    public void setCheckBox(By locator, boolean state)
    {
        if (driver.findElement(locator).isSelected() != state)
            driver.findElement(locator).click();
    }

    /**
     * Sets the selection state of a checkbox.
     *
     * @param item  - The checkbox item to be selected/deselected.
     * @param state - The state that the checkbox should be.
     *              <tt>true</tt> for checked, <tt>false</tt> for un-checked.
     */
    public void setCheckBox(WebElement item, boolean state)
    {
        if (item.isSelected() != state)
            item.click();
    }

    /**
     * Switch to the main driver window
     */
    public void switchToMainWindow()
    {
        driver.switchTo().window(mainWindow);
    }

    /**
     * Type something into an input field. WebDriver doesn't normally clear these
     * before typing, so this method does that first.
     *
     * @param locator The by to locate the input field with.
     * @param text    The text to enter into the input field.
     */
    public void type(By locator, String text)
    {
        waitForExists(locator);
        type(driver.findElement(locator), text);
    }

    /**
     * Type text into a WebElement object.
     *
     * @param element The element to be typed into.
     * @param text    The text to be entered.
     */
    public void type(WebElement element, String text)
    {
        type(element, text, 0);
    }

    /**
     * Scrolls the element into view, minus an offset, then types
     * text into the field, clearing any text previously there.
     *
     * @param element The element to be typed into.
     * @param text    The text to be entered.
     * @param offset  Non-zero vertical scrolling if needed.
     */
    public void type(WebElement element, String text, int offset)
    {
        waitForExists(element, SystemProperties.mVal);

        // Bring the item into "normal" view then bring it down by the offset.
        // This accounts for the toolbar height. The element height accounts
        // for the element being fully scrolled into view.
        int elementHeight = element.getSize().getHeight();
        //Checking to see if the element is in the viewable area.
        if (!isWebElementVisibleOnScreen(element))
        {
            scrollItemIntoView(element, -(offset + elementHeight));
        }

        // Try to give the focus to the specific element.
        try
        {
            element.click();
            element.clear();
        } catch (Exception ex)
        {
            //just in case the element was not in view above, attempt to scroll it into view here.
            scrollItemIntoView(element, -(offset + elementHeight));
            element.click();
            element.clear();
        }

        // Use the action to type the character string into the element.
        Actions action = new Actions(driver);
        action
            .sendKeys(element, text)
            .build()
            .perform();
    }

    /**
     * Scrolls the element into view, minus an offset, then types
     * text into the field without clearing previous text. Be aware that
     * this method clicks the center of an element which may also place
     * typed text within text that was already there.
     *
     * @param element The element to be typed into.
     * @param text    The text to be entered.
     * @param offset  Non-zero vertical scrolling if needed.
     */
    public void typeNoClear(WebElement element, String text, int offset)
    {
        waitForExists(element);

        // Bring the item into "normal" view then bring it down by the offset.
        // This accounts for the toolbar height. The element height accounts
        // for the element being fully scrolled into view.
        int elementHeight = element.getSize().getHeight();
        //Checking to see if the element is in the viewable area.
        if (!isWebElementVisibleOnScreen(element))
        {
            scrollItemIntoView(element, -(offset + elementHeight));
        }

        // Try to give the focus to the specific element.
        try
        {
            element.click();
        } catch (Exception ex)
        {
            //just in case the element was not in view above, attempt to scroll it into view here.
            scrollItemIntoView(element, -(offset + elementHeight));
            element.click();
        }

        // Use the action to type the character string into the element.
        Actions action = new Actions(driver);
        action
            .sendKeys(element, text)
            .build()
            .perform();
    }

    /**
     * Returns an ExpectedCondition that only returns <tt>true</tt> when
     * the number of windows open is greater than the number of windows
     * in the before set.
     *
     * @param before A list of window handles before activating the popup.
     * @return The visibility of a popup window.
     */
    private ExpectedCondition<Boolean> visibilityOfPopup(final Set<String> before)
    {
        return new ExpectedCondition<Boolean>()
        {
            @Override
            public Boolean apply(WebDriver doesntMatter)
            {
                return (driver.getWindowHandles().size() > before.size());
            }
        };
    }

    /**
     * Wait for an alert to appear.
     * It will wait up to a mediumWait.
     *
     * @return {@link Boolean}
     */
    public boolean waitForAlert()
    {
        return waitForAlert(SystemProperties.mVal);
    }

    /**
     * Wait for an alert to appear.
     *
     * @param secondsToWait The number of seconds to wait until timeout.
     * @return {@link Boolean}
     */
    public boolean waitForAlert(int secondsToWait)
    {
        return waitForAlert(secondsToWait, true);
    }

    /**
     * Wait for an alert to appear.
     *
     * @param secondsToWait The number of seconds to wait until timeout.
     * @param failOnTimeout Fail on a timeout or not.
     * @return {@link Boolean}
     */
    public boolean waitForAlert(int secondsToWait, boolean failOnTimeout)
    {
        boolean result = true;
        try
        {
            WebDriverWait wait = new WebDriverWait(driver, secondsToWait);

            wait
                .pollingEvery(1, TimeUnit.SECONDS)
                .until(new ExpectedCondition<Boolean>()
                {
                    public Boolean apply(WebDriver doesntMatter)
                    {
                        return isAlertPresent();
                    }
                });
        } catch (TimeoutException ex)
        {
            if (failOnTimeout)
            {

                System.out.print("Timed out looking for the alert: " +
                    " --- Stack Trace: " + ExceptionUtils.getStackTrace(ex));
                throw ex;
            }

            result = false;
        }
        return result;
    }

    /**
     * Wait for an alert to disappear.
     * It will wait up to a mediumWait.
     *
     * @return {@link Boolean}
     */
    public boolean waitForAlertNotPresent()
    {
        return waitForAlertNotPresent(SystemProperties.mVal);
    }

    /**
     * Wait for an alert to disappear.
     *
     * @param secondsToWait The number of seconds to wait until timeout.
     * @return {@link Boolean}
     */
    public boolean waitForAlertNotPresent(int secondsToWait)
    {
        return waitForAlertNotPresent(secondsToWait, true);
    }

    /**
     * Wait for an alert to disappear.
     *
     * @param secondsToWait The number of seconds to wait until timeout.
     * @param failOnTimeout Fail on a timeout or not.
     * @return {@link Boolean}
     */
    public boolean waitForAlertNotPresent(int secondsToWait, boolean failOnTimeout)
    {
        boolean result = true;
        try
        {
            WebDriverWait wait = new WebDriverWait(driver, secondsToWait);

            wait
                .pollingEvery(1, TimeUnit.SECONDS)
                .until(new ExpectedCondition<Boolean>()
                {
                    public Boolean apply(WebDriver doesntMatter)
                    {
                        return !isAlertPresent();
                    }
                });
        } catch (TimeoutException ex)
        {
            if (failOnTimeout)
            {
                System.out.print("Timed out waiting for aler to disapper" +
                    " --- Stack Trace: " + ExceptionUtils.getStackTrace(ex));
                throw ex;
            }

            result = false;
        }
        return result;
    }

    /**
     * This utility waits for any element to be displayed in the table.
     *
     * @param tableLocator - This is the locator to the table itself.
     * @param rowsLocator  - This is the locator to <b>all</b> rows within the table.
     */
    public void waitForAnyItemInTable(final By tableLocator, final By rowsLocator)
    {
        final WebElement table = driver.findElement(tableLocator);
        getMediumWait().until(new ExpectedCondition<Boolean>()
        {
            @Override
            public Boolean apply(WebDriver doesntMatter)
            {
                return (table.findElements(rowsLocator)).size() > 0;
            }
        });
    }

    /**
     * This utility waits for any element to be displayed in the table.
     *
     * @param table       - This is the table element.
     * @param rowsLocator - This is the locator to <b>all</b> rows within the table.
     */
    public void waitForAnyItemInTable(final WebElement table, final By rowsLocator)
    {
        getMediumWait().until(new ExpectedCondition<Boolean>()
        {
            @Override
            public Boolean apply(WebDriver doesntMatter)
            {
                return (table.findElements(rowsLocator)).size() > 0;
            }
        });
    }

    /**
     * Wait for the availability of a WebElement that is acquired with a dynamic xpath.
     *
     * @param xpath The XPath of the element in question.
     * @return {@link WebElement} The element being waited for.
     * @throws Exception If the element is not found.
     */
    public WebElement waitForAvailable(String xpath) throws Exception
    {
        return waitForAvailable(By.xpath(xpath), 5);
    }

    /**
     * Wait for the availability of a WebElement that is acquired with a dynamic xpath.
     *
     * @param xpath The XPath of the element in question.
     * @param iterations The number of 2 second intervals to wait.
     * @return {@link WebElement} The element being waited for.
     * @throws Exception If the element is not found.
     */
    public WebElement waitForAvailable(String xpath, int iterations) throws Exception
    {
        return waitForAvailable(By.xpath(xpath), iterations);
    }

    /**
     * Wait for the availability of a WebElement.
     *
     * @param locator The locator of the element in question.
     * @return {@link WebElement} The element being waited for.
     * @throws Exception If the element is not found.
     */
    public WebElement waitForAvailable(By locator) throws Exception
    {
        return waitForAvailable(locator, 5);
    }

    /**
     * Wait for the availability of a WebElement.
     *
     * @param locator The locator of the element in question.
     * @param iterations The number of 2 second intervals to wait. The default is 5,
     * i.e. for a total of 10 seconds.
     * @return {@link WebElement} Returns the item being waited on.
     * @throws Exception If the element is not found.
     */
    public WebElement waitForAvailable(By locator, int iterations) throws Exception
    {
        // Build a list element from the xpath
        List<WebElement> elements = driver.findElements(locator);

        // Wait for the element to be available.
        int count = 0;
        while (elements.size() < 1 && (count < iterations))
        {
            System.out.print("Waiting for the element to display: " + count + 1 + " of " + iterations + "attempts.");
            Utils.wait(2); // Do NOT change this.
            elements = driver.findElements(locator);
            count++;
        }

        if (elements.size() == 0)
        {
            throw new Exception("The element being waited for did not display. Locator: " + locator.toString());
        }

        return elements.get(0);
    }

    /**
     * Wait for the item to be clickable.
     *
     * @param item The By locator of the element being waited on.
     */
    public void waitForClickable(By item)
    {
        waitForClickable(driver.findElement(item));
    }

    public void waitForClickable(WebElement item)
    {
        try
        {
            SystemProperties.getMediumWait().ignoring(StaleElementReferenceException.class).until(
                ExpectedConditions.elementToBeClickable(item));
        } catch (Exception ex)
        {
            System.out.print("Timed out waiting for item: " + item.toString() +
                " to become clickable. --- Stack Trace: " + ExceptionUtils.getStackTrace(ex));
            throw ex;
        }
    }

    /**
     * waits for a WebElement to exist when it is added dynamically to the dom
     * NOTE: be aware that this method will ignore no such element exceptions. If you need
     * to avoid that, use a wait for visible after the exists to make sure the element is present
     * and that this method did not fail.
     *
     * @param item the WebElement to wait for
     * @return {@link Boolean} true if the {@link WebElement} becomes visible
     */
    public boolean waitForExists(final WebElement item)
    {
        return waitForExists(item, SystemProperties.lVal);
    }

    public boolean waitForExists(final WebElement item, int timeToWait)
    {
        boolean found = false;
        WebElement element = null;
        try
        {
            element = (new WebDriverWait(driver, timeToWait))
                .until(ExpectedConditions.visibilityOf(item));
        } catch (Exception ex)
        {
            //do nothing, this may time out and that's fine.
        }

        if (element != null)
            found = true;

        return found;
    }

    /**
     * waits for a WebElement to exist when it is added dynamically to the dom
     * NOTE: be aware that this method will ignore no such element exceptions. If you need
     * to avoid that, use a wait for visible after the exists to make sure the element is present
     * and that this method did not fail.
     *
     * @param locator the By locator of the web element to wait for
     * @return {@link Boolean} true if the {@link WebElement} becomes clickable
     */
    public boolean waitForExists(final By locator)
    {
        return waitForExists(locator, SystemProperties.lVal);
    }

    public boolean waitForExists(final By locator, int timeToWait)
    {
        boolean found = false;
        WebElement element = null;
        try
        {
            element = (new WebDriverWait(driver, timeToWait))
                .until(ExpectedConditions.elementToBeClickable(locator));
        } catch (Exception ex)
        {
            // Do nothing.
        }

        if (element != null)
            found = true;

        return found;
    }

    /**
     * Wait for a frame to be available and switch into it.
     *
     * @param frameLocator The name of the frame to be switched into.
     */
    public void waitForFrame(String frameLocator)
    {
        try
        {
            // Wait until the IFrame is present on the page and then switch to it.
            if (elementIsVisible(driver.findElement(By.id(frameLocator))))
            {
                getWebDriver().switchTo().frame(frameLocator);
            }
        } catch (Exception ex)
        {
            System.out.print("Timed out waiting for an iFrame: " + frameLocator + " to be available: ");
            throw ex;
        }
    }

    /**
     * Wait for a frame to be available and switch into it.
     *
     * @param frameLocator The element of the frame to be switched into.
     */
    public void waitForFrame(WebElement frameLocator)
    {
        try
        {
            // Wait until the IFrame is present on the page and then switch to it.
            if (elementIsVisible(frameLocator))
            {
                getWebDriver().switchTo().frame(frameLocator);
            }
        } catch (Exception ex)
        {
            System.out.print("Timed out waiting for an iFrame to be available: ");
            throw ex;
        }
    }

    /**
     * Wait for the item to be invisible.
     *
     * @param item The By locator of the element being waited on.
     */
    public void waitForInvisibility(By item)
    {
        waitForInvisibility(driver.findElement(item));
    }

    public void waitForInvisibility(final WebElement element)
    {
        waitForInvisibility(element, SystemProperties.mVal);
    }

    public void waitForInvisibility(final WebElement element, int secondsToWait)
    {
        waitForPageLoad();
        try
        {
            WebDriverWait wait = new WebDriverWait(driver, secondsToWait);

            wait.pollingEvery(1, TimeUnit.MICROSECONDS)
                .until(new ExpectedCondition<Boolean>()
                {
                    public Boolean apply(WebDriver doesntMatter)
                    {
                        return !element.isDisplayed();
                    }
                });
        } catch (TimeoutException e)
        {
            System.out.print("Timed out waiting for item: " + element.toString());
            throw e;
        }
    }

    /**
     * Performs a long wait until the specified item is visible on the page.
     *
     * @param item The By locator of the item to wait for.
     */
    public void waitForLongVisibility(By item)
    {
        try
        {
            SystemProperties.getLongWait().until(ExpectedConditions.visibilityOfElementLocated(item));
        } catch (Exception ex)
        {
            System.out.print("Timed out waiting for item: " + item.toString());
            throw ex;
        }
    }

    public void waitForPageLoad()
    {
        ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>()
        {
            public Boolean apply(WebDriver doesntMatter)
            {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };

        Wait<WebDriver> wait = new WebDriverWait(driver, 30);
        try
        {
            wait.until(expectation);
        } catch (Throwable ex)
        {
            System.out.print("Timeout exception waiting for the page to load");
            throw new TimeoutException("Timeout exception waiting for the page to load --- Stack Trace: "
                + ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Wait for a poup window to be active. <tt><b>Uses the medium wait.</b></tt>
     *
     * @param before A list of window handles before the popup is activated.
     * @return The handle of the new popup window.
     */
    public String waitForPopup(final Set<String> before)
    {
        // Wait for the new window to popup.
        getMediumWait().until(visibilityOfPopup(before));

        // Get the window handles after the popup window is active.
        Set<String> afterHandles = driver.getWindowHandles();

        // Remove all before handles from after. Leaves you with new window handle
        afterHandles.removeAll(before);

        // Return the handle for the popup window
        return afterHandles.iterator().next();
    }

    /**
     * Wait for a poup window to be active.
     *
     * @param before        A list of window handles before the popup is activated.
     * @param timeInSeconds The maximum amount of time to wait before timing out.
     * @return The handle of the new popup window.
     */
    public String waitForPopup(final Set<String> before, int timeInSeconds)
    {
        // Wait for the new window to popup.
        WebDriverWait wait = new WebDriverWait(driver, timeInSeconds);
        wait.until(visibilityOfPopup(before));

        // Get the window handles after the popup window is active.
        Set<String> afterHandles = driver.getWindowHandles();

        // Remove all before handles from after. Leaves you with new window handle
        afterHandles.removeAll(before);

        // Return the handle for the popup window
        return afterHandles.iterator().next();
    }

    /**
     * Wait for the presence of an element.
     *
     * @param locator The By locator of the element being waited on.
     */
    public void waitForPresence(By locator)
    {
        waitForPageLoad();
        getMediumWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Wait for the presence of an element.
     *
     * @param item The element being waited on.
     */
    public void waitForPresence(WebElement item)
    {
        waitForPageLoad();
        String xpath = getElementXPath(item);
        By locator = By.xpath(xpath);
        getMediumWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Wait for the item referenced by the locator to be in a refreshed state.
     *
     * @param locator The By locator of the element being waited on.
     */
    public void waitForRefreshed(By locator)
    {
        waitForRefreshed(driver.findElement(locator));
    }

    /**
     * Wait for the item referenced by the locator to be in a refreshed state.
     *
     * @param element The element being waited on.
     */
    public void waitForRefreshed(WebElement element)
    {
        try
        {
            SystemProperties.getMediumWait().until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(element)));
        } catch (Exception ex)
        {
            System.out.print("Timed out item: " + element.toString() +
                " waiting to be in refreshed state." + "--- Stack Trace: " + ExceptionUtils.getStackTrace(ex));
            throw ex;
        }
    }

    public void waitForStaleOrNonExistentElement(WebElement element)
    {
        try
        {
            WebDriverWait wait = new WebDriverWait(driver, SystemProperties.mVal);

            wait.pollingEvery(10, TimeUnit.MICROSECONDS)
                .until(new ExpectedCondition<Boolean>()
                {
                    public Boolean apply(WebDriver driver)
                    {
                        boolean isGone = false;
                        try
                        {
                            element.isDisplayed();
                        } catch (StaleElementReferenceException | NoSuchElementException ex)
                        {
                            isGone = true;
                        }
                        return isGone;
                    }
                });
        } catch (TimeoutException e)
        {
            System.out.print("Timed out waiting for item: " + element.toString());
            throw e;
        }
    }

    /**
     * Zoom the window to the percent.
     *
     * @param percent The percent value for zooming.
     */
    public void zoom(String percent)
    {
//        String cmd = "document.body.style.transform='scale(" + String.valueOf(zoomLevel) + "))'";
        String script = "document.body.style.zoom='" + percent + "%'";
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(script);

        // A way to zoom through the settings.
/*
        driver.get("chrome://settings/content");
        driver.switchTo().frame("settings");
        Select list = new Select(driver.findElement(By.id("defaultZoomFactor")));
        list.selectByVisibleText(percent.trim() + "%");
*/
    }

    /**
     * Reset the zoom back to normal.
     */
    public void zoomReset()
    {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.body.style.zoom='100%'");
    }
}