package tm4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

public class JiraStatusReporter implements Formatter, Reporter {
  private String testCaseTitle;
  private List<String> testStepList;
  private String featureFileName;

  /**
   * Start Cucumber scenario.
   *
   * @param scenario - Cucumber Scenario
   */
  protected void beforeScenario(Scenario scenario) {
    testCaseTitle = featureFileName + " " + scenario.getName();
    testStepList = new ArrayList<String>();
  }

  /**
   * This will be called at the end of an scenario.
   *
   * @param scenario - Scenario.
   * @throws IOException - if in case of IO Exception.
   * @throws JSONException - if in case of JSON exception.
   */
  protected void afterScenario(Scenario scenario) throws IOException, JSONException {
    if (JiraPropertiesReader.getConfigProperties("pushTestCasesIntoJira")
        .equalsIgnoreCase("true")) {
      System.err.println("Ready to push test case into tm4j");
      if (JiraPropertiesReader.getConfigProperties("checkIfTCAlreadyAvailable")
          .equalsIgnoreCase("true")) {
        if (!JiraTestCasePush.isTestCaseAvailable(testCaseTitle)) {
          System.err.println("Creating new test case in tm4j.");
          JiraTestCasePush.createTestCaseIntoJira(testCaseTitle, testStepList);
        }
      } else {
        System.err.println("Skipping check if test case already available or not.");
        JiraTestCasePush.createTestCaseIntoJira(testCaseTitle, testStepList);
      }
    } else {
      System.err.println("Task to create test case is skipped.");
    }
    if ((JiraBeans.getTestCaseId() != 0)
        && (JiraPropertiesReader.getConfigProperties("storyName") != null) && JiraPropertiesReader
            .getConfigProperties("pushTestCasesIntoJira").equalsIgnoreCase("true")) {
      System.err.println("Linking test case with story.");
      JiraTestCasePush.linkTestCaseWithStoryIntoJira(JiraBeans.getTestCaseId(),
          JiraPropertiesReader.getConfigProperties("storyName"));
    } else {
      System.err.println("Task to link test case with story is skipped.");
      System.err.println("Either test case is not generated or something wrong in jira.properties");
    }
    if (JiraBeans.getTestCaseId() != 0
        && JiraPropertiesReader.getConfigProperties("epicName") != null) {
      System.err.println("Linking test case with epic.");
      JiraTestCasePush.linkTestCaseWithEpicIntoJira(JiraBeans.getTestCaseId(),
          JiraPropertiesReader.getConfigProperties("epicName"));
    } else {
      System.err.println("Task to link test case with Epic is skipped.");
      System.err.println("Either test case is not generated or something wrong in jira.properties");
    }
  }

  @Override
  public void step(Step step) {
    testStepList.add(step.getKeyword() + " " + step.getName());
  }

  @Override
  public void startOfScenarioLifeCycle(Scenario scenario) {
    beforeScenario(scenario);
  }

  @Override
  public void endOfScenarioLifeCycle(Scenario scenario) {
    try {
      afterScenario(scenario);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void before(Match match, Result result) {}

  @Override
  public void result(Result result) {}

  @Override
  public void after(Match match, Result result) {}

  @Override
  public void match(Match match) {}

  @Override
  public void embedding(String mimeType, byte[] data) {}

  @Override
  public void write(String text) {}

  @Override
  public void syntaxError(String state, String event, List<String> legalEvents, String uri,
      Integer line) {}

  @Override
  public void uri(String uri) {}

  @Override
  public void feature(Feature feature) {
    featureFileName = feature.getName();
  }

  @Override
  public void scenarioOutline(ScenarioOutline scenarioOutline) {}

  @Override
  public void examples(Examples examples) {}

  @Override
  public void background(Background background) {}

  @Override
  public void scenario(Scenario scenario) {}

  @Override
  public void done() {}

  @Override
  public void close() {}

  @Override
  public void eof() {}
}
