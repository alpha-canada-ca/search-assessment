package ca.canada.digital.search.assessment.object;

import ca.canada.digital.search.assessment.process.TermEvaluationProcess;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public enum SeleniumDriver {
    INSTANCE;

    SeleniumDriver() {
    }

    public WebDriver getDriver() {
        return getSeleniumDriver();
    }

    private FirefoxDriver getSeleniumDriver() {
        //System.setProperty("webdriver.gecko.driver", "/usr/bin/geckodriver");
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("general.useragent.override", TermEvaluationProcess.USER_AGENT);
        options.addArguments("-headless");
        return new FirefoxDriver(options);
    }
}
