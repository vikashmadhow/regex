package ma.vi.regex;

import ma.vi.graph.Edge;

import java.util.*;

/**
 * This class is used to recognize regular expressions.
 *
 * @author Vikash Madhow
 * @version 1.0
 */
public class RegEx {

  /**
   * Creates a RegEx object that is capable of recognizing patterns
   * corresponding to the specified regular expression.]]
   */
  public RegEx(String expression) throws SyntaxException {
    this.expression = expression;

    // escape characters, insert concatenation operators and convert
    // to postfix to help creation of NFA
    expression = normalize(expression);
    expression = insertConcatOp(expression);
    expression = toPostfix(expression);

    // build NFA and DFA
    this.dfa = buildDFA(buildNFA(expression));
  }

  /**
   * Returns true if the expression is part of the language represented
   * by this regular expression
   */
  public boolean match(String exp) {
    return dfa.match(exp);
  }

  /**
   * Returns the precedence of an operator. Used during the conversion
   * of the regular expression to postfix notation.
   */
  private int precedence(char op) {
    return switch (op) {
      case OP_OR -> 5;
      case OP_CONCAT -> 10;
      case OP_CLOSURE, OP_POS_CLOSURE -> 15;
      case OP_OPEN_PARENTHESIS, OP_CLOSE_PARENTHESIS -> 0;
      default -> -1;
    };
  }

  /**
   * Returns the operator type: prefix, infix or postfix
   */
  public int operatorType(char op) {
    return switch (op) {
      case OP_CLOSURE, OP_POS_CLOSURE -> POSTFIX;
      case OP_CONCAT, OP_OR -> INFIX;
      default -> -1;
    };
  }

  /**
   * Return true if the character denotes an operator
   */
  private boolean isOperator(char c) {
    return (c == OP_ESCAPE)
        || (c == OP_CLOSURE)
        || (c == OP_POS_CLOSURE)
        || (c == OP_OR)
        || (c == OP_CONCAT)
        || (c == OP_OPEN_PARENTHESIS)
        || (c == OP_CLOSE_PARENTHESIS);
  }

  private boolean isEscapedOperator(char c) {
    return isOperator((char)(c - ESCAPE_AREA));
  }

  /**
   * Return true if the character denotes an opening or closing parenthesis
   */
  private boolean isParenthesis(char c) {
    return (c == OP_OPEN_PARENTHESIS)
        || (c == OP_CLOSE_PARENTHESIS);
  }

  /**
   * Return true if the character denotes a symbol
   */
  private boolean isSymbol(char c) {
    return !isOperator(c);
  }

  /**
   * <p>
   * Escapes operators which are preceded by the '\' character. Escaped operators are
   * temporarily mapped to the unicode private area which starts at 0xE000. They are
   * mapped back during creation of the NFA.
   * </p>
   *
   * <p>
   * This method also removes parenthesis which are not enclosing any expressions
   * [of the form () ].
   * </p>
   */
  private String normalize(String expression) throws SyntaxException {
    if (expression.length() == 0) {
      throw new SyntaxException("Empty regular expression");
    }
    StringBuilder exp = new StringBuilder(expression);
    for (int i = 0; i < exp.length(); i++) {
      char c = exp.charAt(i);
      if (c == OP_ESCAPE) {
        if (i < exp.length() - 1) {
          char esc = exp.charAt(i + 1);
          if (isSymbol(esc)) {
            throw new SyntaxException("Can't escape '" + esc + "'");
          }
          exp.delete(i, i + 2);
          exp.insert(i, (char)(esc + ESCAPE_AREA));
        } else {
          throw new SyntaxException("No operand for escape operator");
        }
      } else if (c == OP_OPEN_PARENTHESIS) {
        if (i < exp.length() - 1) {
          if (exp.charAt(i + 1) == OP_CLOSE_PARENTHESIS) {
            exp.delete(i, i + 2);
            i -= 2;
          }
        } else {
          throw new SyntaxException("Unmatched parenthesis");
        }
      }
    }
    return exp.toString();
  }

  /**
   * <p>
   * Inserts the internal concatenation operator ('\1') in the regular expression.
   * This is done to facilitate the generation of the NFA from the regular expression.
   * A concatenation operator is inserted between the following:
   * <ol>
   *   <li>Between 2 symbols [e.g ab]</li>
   *   <li>A symbol followed by an opening parenthesis [e.g ab(c ]</li>
   *   <li>A closing parenthesis followed by a symbol [e.g c)ab ]</li>
   *   <li>A closing parenthesis followed by an opening parenthesis [e.g (a|b)(b|c) ]</li>
   *   <li>a postfix unary operator and a symbol</li>
   *   <li>a postfix unary operator and an opening parenthesis</li>
   * </ol>
   * </p>
   *
   * <p>
   * These are the only cases where concatenation is implied. the '\1' is
   * used to denote the concatenation operator.
   * </p>
   */
  private String insertConcatOp(String expression) {
    if (expression.length() == 0) {
      return expression;
    }
    StringBuilder exp = new StringBuilder(expression);
    char lastChar = exp.charAt(0);
    for (int i = 1; i < exp.length(); i++) {
      char c = exp.charAt(i);
      if (isSymbol(lastChar) && isSymbol(c)) {
        exp.insert(i, OP_CONCAT);
        i++;
      } else if (isSymbol(lastChar) && (c == OP_OPEN_PARENTHESIS)) {
        exp.insert(i, OP_CONCAT);
        i++;
      } else if ((lastChar == OP_CLOSE_PARENTHESIS) && isSymbol(c)) {
        exp.insert(i, OP_CONCAT);
        i++;
      } else if ((lastChar == OP_CLOSE_PARENTHESIS) && (c == OP_OPEN_PARENTHESIS)) {
        exp.insert(i, OP_CONCAT);
        i++;
      } else if (((lastChar == OP_CLOSURE) || (lastChar == OP_POS_CLOSURE)) && isSymbol(c)) {
        exp.insert(i, OP_CONCAT);
        i++;
      } else if (((lastChar == OP_CLOSURE) || (lastChar == OP_POS_CLOSURE)) && (c == OP_OPEN_PARENTHESIS)) {
        exp.insert(i, OP_CONCAT);
        i++;
      }
      lastChar = c;
    }
    return exp.toString();
  }

  /**
   * Converts a regular expression to postfix (reverse polish notation)
   * for easier evaluation (conversion to NFA).
   */
  private String toPostfix(String exp) throws SyntaxException {
    StringBuilder postfix = new StringBuilder();
    Deque<Character> stack = new ArrayDeque<>(exp.length());

    for (int i = 0; i < exp.length(); i++) {
      char c = exp.charAt(i);
      if (isSymbol(c)) {
        postfix.append(c);
      } else if (c == OP_OPEN_PARENTHESIS) {
        stack.push(c);
      } else if (c == OP_CLOSE_PARENTHESIS) {
        char x;
        try {
          while ((x = stack.pop()) != OP_OPEN_PARENTHESIS) {
            postfix.append(x);
          }
        } catch (IndexOutOfBoundsException e) {
          throw new SyntaxException("Unmatched parenthesis");
        }
      } else {
        if (operatorType(c) == POSTFIX) {
          postfix.append(c);
        } else {
          int precedence = precedence(c);
          while (stack.size() > 0
              && precedence(stack.peek()) >= precedence) {
            postfix.append(stack.pop());
          }
          stack.push(c);
        }
      }
    }
    while (stack.size() > 0) {
      char op = stack.pop();
      if (op == OP_OPEN_PARENTHESIS) {
        throw new SyntaxException("Unmatched parenthesis");
      }
      postfix.append(op);
    }
    return postfix.toString();
  }

  /**
   * Builds an NFA from the postfix representation
   * of the regular expression
   */
  private NFA buildNFA(String exp) throws SyntaxException {
    Deque<NFA> stack = new ArrayDeque<>();
    int len = exp.length();
    for (int i = 0; i < len; i++) {
      char c = exp.charAt(i);
      if (isSymbol(c)) {
        if (isEscapedOperator(c)) {
          c -= ESCAPE_AREA;
        }
        stack.push(NFA.of(c));
      } else
        try {
          NFA nfa1, nfa2;
          switch (c) {
            case OP_CLOSURE -> {
              nfa1 = stack.pop();
              stack.push(nfa1.closure());
            }
            case OP_POS_CLOSURE -> {
              nfa1 = stack.pop();
              stack.push(nfa1.positiveClosure());
            }
            case OP_CONCAT -> {
              nfa1 = stack.pop();
              nfa2 = stack.pop();
              stack.push(nfa2.concat(nfa1));
            }
            case OP_OR -> {
              nfa1 = stack.pop();
              nfa2 = stack.pop();
              stack.push(nfa2.or(nfa1));
            }
            default -> throw new SyntaxException("Unknown operator while converting regular expression to NFA");
          }
        } catch (EmptyStackException e) {
          throw new SyntaxException("Insufficient operands for operator");
        }
    }
    return stack.pop();
  }

  /**
   * Returns the e-closure of a state, i.e., the provided state and
   * all states that are	reachable from the provided state through
   * e-transitions only.
   */
  private Set<State> eClosure(Automata automata, State state) {
    return eClosure(automata, state, new HashSet<>());
  }

  /**
   * Returns the e-closure of a state, i.e, the provided state and
   * all states that are	reachable from the provided state
   * through e-transitions alone
   */
  private Set<State> eClosure(Automata automata, State state, Set<State> closure) {
    closure.add(state);
    for (Edge<State,Character> edge: automata.outgoing(state)) {
      if (edge.weight == RegEx.EMPTY_STRING
       && !closure.contains(edge.endPoint2)) {
        closure.addAll(eClosure(automata, edge.endPoint2, closure));
      }
    }
    return closure;
  }

  /**
   * Returns the e-closure of the provided set of states
   * which is the union of the e-closure of each state
   * in the set
   */
  public Set<State> eClosure(Automata automata, Set<State> states) {
    Set<State> closure = new HashSet<>();
    for (State state: states) {
      closure.addAll(eClosure(automata, state));
    }
    return closure;
  }

  /**
   * Returns the set of states that can be reached
   * from the provided states through transitions on
   * the provided symbol.
   */
  public Set<State> canReach(Automata automata, Set<State> states, char symbol) {
    Set<State> reachable = new HashSet<>();
    for (State s: states) {
      for (Edge<State,Character> edge: automata.outgoing((s))) {
        if (edge.weight == symbol)
          reachable.add(edge.endPoint2);
      }
    }
    return reachable;
  }

  /**
   * Builds a DFA from an NFA using a slightly
   * modified version of the subset algorithm from [Aho, Ullman]
   */
  private DFA buildDFA(NFA nfa) {
    Map<Set<State>, State> stateMap = new HashMap<>();      // maps set of nfa states to particular set of dfa state
    Deque<Set<State>> unmarked = new ArrayDeque<>();        // unmarked sets of nfa states

    // first state of DFA is the e-closure of first state in NFA
    Set<State> closure = eClosure(nfa, nfa.start);
    unmarked.push(closure);
    State x = new State();
    stateMap.put(closure, x);
    State dfaStart = x;

    Set<Edge<State, Character>> dfaEdges = new HashSet<>();

    State y;
    Set<Character> symbols = nfa.symbols();
    while (!unmarked.isEmpty()) {
      Set<State> nfaState = unmarked.pop();
      x = stateMap.get(nfaState);

      for (char symbol: symbols) {
        Set<State> reachable = canReach(nfa, nfaState, symbol);
        closure = eClosure(nfa, reachable);

        if (!stateMap.containsKey(closure)) {
          unmarked.push(closure);
          y = new State();
          if (closure.contains(nfa.end())) {
            y.setGoal(true);
          }
          stateMap.put(closure, y);
        } else {
          y = stateMap.get(closure);
        }

        dfaEdges.add(new Edge<>(x, symbol, y));
      }
    }
    return new DFA(dfaEdges, dfaStart);
  }

  /**
   * a string representation of this regular expression
   */
  public String toString() {
    return expression;
  }

  /**
   * Regular expression string.
   */
  private final String expression;

  /**
   * The DFA used to match patterns with this regular expression.
   */
  private final DFA dfa;

  /**
   * Regular expression operators. There is no operator symbol for
   * concatenation. To facilitate evaluation the concatenation operation
   * is taken to be the character with ascii code 01.
   */
  public static final char OP_CLOSURE = '*',
      OP_POS_CLOSURE = '+',
      OP_OR = '|',
      OP_CONCAT = '\1',
      OP_ESCAPE = '\\',
      OP_OPEN_PARENTHESIS = '(',
      OP_CLOSE_PARENTHESIS = ')';

  /**
   * empty string symbol. \0 for convenience
   */
  public static final char EMPTY_STRING = '\0';

  /**
   * Unicode area where escaped operators are temporarily
   * mapped. This is the start of the unicode private area
   */
  public static final int ESCAPE_AREA = 0xE000;

  /**
   * operator types
   */
  public static final int PREFIX = 1,
      INFIX = 2,
      POSTFIX = 3;
}
