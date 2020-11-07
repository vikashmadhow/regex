package ma.vi.regex;

import ma.vi.graph.Edge;

import java.util.Set;

/**
 * A deterministic finite automata (DFA).
 *
 * @author Vikash Madhow
 */
public class DFA extends Automata {
  /**
   * creates an empty DFA
   */
  public DFA(Set<Edge<State, Character>> edges, State start) {
    super(edges, start);
  }

  /**
   * returns true if the string is recognized by this DFA
   */
  public boolean match(String s) {
    int len = s.length();
    State state = start;
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);

      Set<Edge<State, Character>> edges = outgoing(state);
      Edge<State, Character> edge = null;
      for (Edge<State, Character> e: edges) {
        if (e.weight == c) {
          edge = e;
          break;
        }
      }

      if (edge == null) {
        return false;
      } else {
        state = edge.endPoint2;
      }
      if (state.isGoal() && i == len - 1) {
        return true;
      }
    }
    return false;
  }
}
