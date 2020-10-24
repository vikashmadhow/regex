package ma.vi.regex;

/**
 * Thrown if a syntax error is found in a regular expression.
 *
 * @author Vikash Madhow (vikash.madhow@gmail.com)
 */
public class SyntaxException extends RuntimeException {
  public SyntaxException(String error) {
    super(error);
  }

  public SyntaxException(Throwable cause) {
    super(cause);
  }

  public SyntaxException(String error, Throwable cause) {
    super(error, cause);
  }
}
