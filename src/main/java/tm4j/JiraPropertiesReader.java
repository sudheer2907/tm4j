package tm4j;

import java.util.ResourceBundle;

public class JiraPropertiesReader {

  private JiraPropertiesReader() {
    System.out.println("Can be instantiated");
  }

  private static final ResourceBundle rb1;

  static {
    rb1 = ResourceBundle.getBundle("jira");
  }

  /**
   * get config properties of jira.properties file.
   *
   * @param keyString - key name whose value to be fetched.
   * @return - values of that key.
   * @author sudheer.singh
   */
  public static String getConfigProperties(String keyString) {
    return rb1.getString(keyString);
  }
}
