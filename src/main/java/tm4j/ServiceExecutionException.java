package tm4j;

/**
 * The ServiceExecutionException wraps all checked standard Java exception and enriches them with a
 * custom error code. USe this exception to show that Error has occured when the Rest API service
 * returned error code other than what expected.
 *
 * @author akash.dubey
 *
 */
public class ServiceExecutionException extends RuntimeException {

  private static final long serialVersionUID = -4845817036311824180L;

  public ServiceExecutionException(String message) {
    super(message);
    this.errorMessage = message;
  }

  public ServiceExecutionException(Throwable cause) {
    super(cause);
    this.errorMessage = cause.getMessage();
  }

  private final String errorMessage;

  @Override
  public String getMessage() {
    return errorMessage;
  }

}
