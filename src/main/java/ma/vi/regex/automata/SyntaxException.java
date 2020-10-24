package ma.vi.regex.automata;

/** Thrown if a syntax error is found in a regular expression or program */
public class SyntaxException extends RuntimeException {
	/** 1-based value for line and column where syntax 
		error was detected. 0 means unknown */ 
	public int line=0, column=0;
	
	public SyntaxException(String error) {
		super(error);
	}
	
	public SyntaxException(Throwable cause) {
		super(cause);
	}
	
	public SyntaxException(String error, Throwable cause) {
		super(error, cause);
	}
	
	public SyntaxException(String error, int line, int column) {
		super(error);
		this.line = line;
		this.column = column;
	}
}
