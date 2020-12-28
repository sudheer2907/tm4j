package tm4j;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;

import gherkin.deps.com.google.gson.JsonElement;
import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class JiraTestCasePush {

  private JiraTestCasePush() {
    System.out.println("Calling tm4j utility");
  }

  private static final String PROJECT_ID = JiraPropertiesReader.getConfigProperties("projectId");
  private static final String PRIORITY_STR = JiraPropertiesReader.getConfigProperties("priority");
  private static final String STATUS_STR = JiraPropertiesReader.getConfigProperties("status");
  private static final String USER_NAME = JiraPropertiesReader.getConfigProperties("userName");
  private static final String USER_PASSWORD =
      JiraPropertiesReader.getConfigProperties("userPassword");

  private static final String APPLICATION_JSON_STRING = "application/json";
  private static final String TM4J_BASE_URL = "https://jira.connectwisedev.com/rest";
  private static final String GET_STORY_ID_API = TM4J_BASE_URL + "/api/2/search";
  public static final String CONNECTWISE_JIRA_URL = TM4J_BASE_URL + "/tests/1.0";

  /**
   * This method will read json file available under src/main/resources/payload folder.
   *
   * @param fileName - json file name.
   * @return JsonObject - content in the form of json.
   * @author sudheer.singh
   */
  public static JsonObject readJsonFile(String fileName) throws IOException {
    JsonParser parser = new JsonParser();
    InputStream inputStream = JiraTestCasePush.class.getResourceAsStream("/payload/" + fileName);
    String jsonTxt = IOUtils.toString(inputStream, Charset.defaultCharset());
    JsonElement jsonElement = parser.parse(jsonTxt);
    return jsonElement.getAsJsonObject();
  }

  /**
   * Get status id of status passed into jira.properties file.
   *
   * @param projectId - project id where status id has to be fetched.
   * @param statusStr - Status String whose status id has to be fetched
   * @return - id of the status passed into jira.properties file
   * @throws JSONException - if in case of json exception.
   * @author sudheer.singh
   */
  public static int getStatusId(int projectId, String statusStr) throws JSONException {
    if (statusStr != null) {
      Response response = given().auth().preemptive().basic(USER_NAME, USER_PASSWORD)
          .queryParam("projectId", projectId)
          .get("https://jira.connectwisedev.com/rest/tests/1.0/testcasestatus");
      if (response.getStatusCode() == 200) {
        JSONArray resArr = new JSONArray(response.asString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("results", resArr);
        JsonPath extractor = JsonPath.from(jsonObject.toString());
        Map<Object, Object> map =
            extractor.get("results.find {results -> results.name=='" + statusStr + "'}");
        try {
          return Integer.parseInt(map.get("id").toString());
        } catch (NullPointerException e) {
          System.err.println("Unable to fetch Status id passed into jira.properties");
        }
      } else {
        System.err.println("Response code is not code " + response.getStatusCode());
      }
    }
    System.err.println("Status is not provided.");
    return 0;
  }

  /**
   * Get priority id of status passed into jira.properties file.
   *
   * @param projectId - project id where status id has to be fetched.
   * @param priorityStr - Priorty String whose status id has to be fetched
   * @return - id of the the priority passed into jira.properties file
   * @throws JSONException - if in case of json exception.
   * @author sudheer.singh
   */
  public static int getPriorityId(int projectId, String priorityStr) throws JSONException {
    if (priorityStr != null) {
      Response response = given().auth().preemptive().basic(USER_NAME, USER_PASSWORD)
          .queryParam("projectId", projectId)
          .get("https://jira.connectwisedev.com/rest/tests/1.0/testcasepriority");
      if (response.getStatusCode() == 200) {
        JSONArray resArr = new JSONArray(response.asString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("results", resArr);
        JsonPath extractor = JsonPath.from(jsonObject.toString());
        Map<Object, Object> map =
            extractor.get("results.find {results -> results.name=='" + priorityStr + "'}");
        try {
          return Integer.parseInt(map.get("id").toString());
        } catch (NullPointerException e) {
          System.err.println("Unable to fetch priority id passed into jira.properties");
        }
      } else {
        System.err.println("Response code is not code " + response.getStatusCode());
      }
    }
    System.err.println("Priority is not provided.");
    return 0;
  }

  /**
   * get story id.
   * 
   * @param storyName - name of the story whose ID has to be fetched.
   * @return - story id.
   * @throws IOException - if in case of IO exception.
   * @throws JSONException - if in case of json exception.
   * @author sudheer.singh
   */
  public static String getStoryId(String storyName) throws IOException, JSONException {
    if (storyName != null) {
      JsonObject payloadJsonObject = readJsonFile("getJiraStoryId.json");
      JSONObject payJsonObjecteJsonObject = new JSONObject(payloadJsonObject.toString());
      payJsonObjecteJsonObject.put("jql", "key = " + storyName);
      Response response = given().auth().preemptive().basic(USER_NAME, USER_PASSWORD)
          .contentType(APPLICATION_JSON_STRING).when().body(payJsonObjecteJsonObject.toString())
          .post(GET_STORY_ID_API);
      if (response.getStatusCode() == 200) {
        JSONObject responseJsonObject = new JSONObject(response.asString());
        String storyIdString =
            responseJsonObject.getJSONArray("issues").getJSONObject(0).getString("id");
        JiraBeans.setStoryId(storyIdString);
        return storyIdString;
      }
    }
    System.out.println(
        "Unable to fetch story ID, either not passed into jira.properties or issue with the API.");
    return null;
  }

  /**
   * Get total number of test cases available under given project id.
   *
   * @param projectId - project id.
   * @return - total number of available test cases under given project.
   * @throws JSONException - if in case of json exception,
   * @author sudheer.singh
   */
  public static int getTotalNumOfTestCasesUnderProject(String projectId) throws JSONException {
    if (projectId != null) {
      String urlString = CONNECTWISE_JIRA_URL + "/testcase/search";
      Response response = given().auth().preemptive().basic(USER_NAME, USER_PASSWORD)
          .contentType(APPLICATION_JSON_STRING)
          .queryParam("query", "testCase.projectId=" + projectId).when().get(urlString);
      JSONObject resJsonObject = new JSONObject(response.asString());
      return Integer.parseInt(resJsonObject.getString("total"));
    }
    return 0;
  }


  /**
   * Verify if running test cases are available in tm4j or not.
   *
   * @param testCaseTitle - test case title written in feature file.
   * @return - true if in case in case test case not available in tm4j else false.
   * @throws JSONException - if in case of json exception.
   * @author sudheer.singh
   */
  public static boolean isTestCaseAvailable(String testCaseTitle) throws JSONException {
    System.err.println("Fetching total number of available test cases.");
    int getTotalNumOfTestCasesUnderProject = getTotalNumOfTestCasesUnderProject(PROJECT_ID);
    System.err.println("Total number of available test case under given project:"
        + getTotalNumOfTestCasesUnderProject);
    System.err.println("Checking if test case already available under given project");
    Response response = given().auth().preemptive()
        .basic(JiraPropertiesReader.getConfigProperties("userName"),
            JiraPropertiesReader.getConfigProperties("userPassword"))
        .contentType(APPLICATION_JSON_STRING)
        .queryParam("maxResults", getTotalNumOfTestCasesUnderProject)
        .queryParam("query", "testCase.projectId=" + Integer.parseInt(PROJECT_ID)).when()
        .get(CONNECTWISE_JIRA_URL + "/testcase/search");
    if (response.statusCode() == 200) {
      System.err.println("Service to fetch test case title is working fine.");
      JsonPath extractor = JsonPath.from(response.asString());
      Map<Object, Object> map =
          extractor.get("results.find {results -> results.name=='" + testCaseTitle + "'}");
      if (map != null && map.get("name").toString().equals(testCaseTitle)) {
        int testCaseIdInt = Integer.parseInt(map.get("id").toString());
        JiraBeans.setTestCaseId(testCaseIdInt);
        System.err.println("Test case is already available & Test Case id: " + map.get("key"));
        return true;
      } else {
        System.err.println("Test case is not available in tm4j.");
      }
    } else {
      Assert.fail("Service to fetch list of test cases is not working");
    }
    return false;
  }

  /**
   * Create test case into JIRA.
   *
   * @param testCaseTitle - test case title written in feature file.
   * @param testStep - test steps.
   * @return - test case id if test case are getting created into tm4j.
   * @throws IOException - if in case of IO exception.
   * @throws JSONException - if in case of JSON exception.
   * @author sudheer.singh
   */
  public static int createTestCaseIntoJira(String testCaseTitle, List<String> testStep)
      throws IOException, JSONException {
    if (JiraPropertiesReader.getConfigProperties("pushTestCasesIntoJira")
        .equalsIgnoreCase("true")) {
      try {
        JsonObject payloadJsonObject = readJsonFile("createTestCaseInJira.json");
        JSONObject payJsonObjecteJsonObject = new JSONObject(payloadJsonObject.toString());
        payJsonObjecteJsonObject.put("name", testCaseTitle.toString());
        payJsonObjecteJsonObject.put("projectId", Integer.parseInt(PROJECT_ID));
        payJsonObjecteJsonObject.put("owner", JiraPropertiesReader.getConfigProperties("author"));
        StringBuilder builder = new StringBuilder();
        for (String step : testStep) {
          builder.append(step).append("\n");
        }
        payJsonObjecteJsonObject.getJSONObject("testScript").getJSONObject("bddScript").put("text",
            builder.toString());
        int statusId = getStatusId(Integer.parseInt(PROJECT_ID), STATUS_STR);
        int priorityId = getPriorityId(Integer.parseInt(PROJECT_ID), PRIORITY_STR);
        payJsonObjecteJsonObject.put("priorityId", priorityId);
        payJsonObjecteJsonObject.put("statusId", statusId);
        payJsonObjecteJsonObject.getJSONArray("labels")
            .put(JiraPropertiesReader.getConfigProperties("labels"));
        Response response = given().auth().preemptive().basic(USER_NAME, USER_PASSWORD)
            .relaxedHTTPSValidation().contentType(APPLICATION_JSON_STRING).when()
            .body(payJsonObjecteJsonObject.toString())
            .post("https://jira.connectwisedev.com/rest/tests/1.0/testcase");
        if (response.getStatusCode() == 201) {
          JSONObject responseJsonObject = new JSONObject(response.asString());
          JiraBeans.setTestCaseId(responseJsonObject.getInt("id"));
          System.err.println("Test case id created " + responseJsonObject.getString("key"));
          return Integer.parseInt(responseJsonObject.getString("id"));
        } else {
          System.err.println("Issue came while creating test cases into tm4j");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return JiraBeans.getTestCaseId();
  }

  /**
   * Link a test case id with story id.
   *
   * @throws IOException - if in case of IO Exception.
   * @throws JSONException - if in case of JSON exception.
   */
  public static void linkTestCaseWithStoryIntoJira(int testCaseId, String storyName)
      throws IOException, JSONException {
    String issueId = getStoryId(storyName);
    if (testCaseId != 0 && issueId != null) {
      try {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("testCaseId", testCaseId);
        jsonObject.put("issueId", issueId);
        jsonObject.put("typeId", 1);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);
        Response response = given().auth().preemptive().basic(USER_NAME, USER_PASSWORD)
            .contentType(APPLICATION_JSON_STRING).when().body(jsonArray.toString())
            .post(CONNECTWISE_JIRA_URL + "/tracelink/bulk/create");
        if (response.getStatusCode() == 200) {
          System.err.println("Test case is linked with story/epic.");
        }
      } catch (Exception e) {
        System.err.println("Unable to link test case with the story ID due to API call");
        e.printStackTrace();
      }
    } else {
      System.err.println("Issue came while linking test case with the story ID.");
    }
  }

  /**
   * Link the test case id at the Epic level
   *
   * @param testCaseId - id of the test case.
   * @param epicName - name of the Epic.
   * @throws JSONException - if in case of json exceptions.
   * @throws IOException - if in case of IO exceptions.
   * @author sudheer.singh
   */
  public static void linkTestCaseWithEpicIntoJira(int testCaseId, String epicName)
      throws IOException, JSONException {
    linkTestCaseWithStoryIntoJira(testCaseId, epicName);
  }
}
