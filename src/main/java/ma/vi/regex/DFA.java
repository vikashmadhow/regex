package ma.vi.regex;

import ma.vi.graph.DirectedEdge;

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
  public DFA(Set<DirectedEdge<State, Character>> edges, State start) {
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

      Set<DirectedEdge<State, Character>> edges = outgoing(state);
      DirectedEdge<State, Character> edge = null;
      for (DirectedEdge<State, Character> e: edges) {
        if (e.weight() == c) {
          edge = e;
          break;
        }
      }

      if (edge == null) {
        return false;
      } else {
        state = edge.endPoint2();
      }
      if (state.isGoal() && i == len - 1) {
        return true;
      }
    }
    return false;
  }
}
