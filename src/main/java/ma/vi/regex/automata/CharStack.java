package ma.vi.regex.automata;

/**
 * Simple array-based character stack,
 * faster and uses less memory than java.util.Stack.
 * Used during conversion of regular expression to postfix notation
 *
 * @author Vikash Madhow
 * @version 1.0
 */
public class CharStack {
  /**
   * The underlying array
   */
  private char[] stack;

  /**
   * top element + 1 in stack. 0 = no elements
   */
  private int position = 0;

  /**
   * create a character stack that can hold up to
   * capacity characters
   */
  public CharStack(int capacity) {
    stack = new char[capacity];
  }

  /**
   * pops a character from the top of the stack
   */
  public synchronized char pop() throws IndexOutOfBoundsException {
    if (position > 0) {
      return stack[--position];
    } else {
      throw new IndexOutOfBoundsException("Stack empty");
    }
  }

  /**
   * Read the character at the top of the stack without popping it
   */
  public synchronized char read() throws IndexOutOfBoundsException {
    if (position > 0) {
      return stack[position - 1];
    } else {
      throw new IndexOutOfBoundsException("Stack empty");
    }
  }

  /**
   * pushes a character into the stack
   */
  public synchronized void push(char c) throws IndexOutOfBoundsException {
    if (position < stack.length)
      stack[position++] = c;
    else
      throw new IndexOutOfBoundsException("Stack full");
  }

  /**
   * returns the capacity of the stack
   */
  public int capacity() {
    return stack.length;
  }

  /**
   * return the number of items in the stack
   */
  public int length() {
    return position;
  }
}
