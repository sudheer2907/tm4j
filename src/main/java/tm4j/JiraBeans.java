package tm4j;

public class JiraBeans {

  private int projectId;
  private int testCaseId;
  private String storyId;
  private static JiraBeans jiraBeans = new JiraBeans();

  /**
   * Private no argument constructor for DataBaseBean restricting instance creation.
   */
  private JiraBeans() {}

  public static void setProjectId(int projectId) {
    jiraBeans.projectId = projectId;
  }

  public static int getProjectId() {
    return jiraBeans.projectId;
  }

  public static void setTestCaseId(int testCaseId) {
    jiraBeans.testCaseId = testCaseId;
  }

  public static int getTestCaseId() {
    return jiraBeans.testCaseId;
  }

  public static void setStoryId(String storyId) {
    jiraBeans.storyId = storyId;
  }

  public static String getStoryId() {
    return jiraBeans.storyId;
  }
}
