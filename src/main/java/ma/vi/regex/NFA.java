package ma.vi.regex;

import ma.vi.graph.Edge;

import java.util.HashSet;
import java.util.Set;

import static ma.vi.regex.RegEx.EMPTY_STRING;

/**
 * a non-deterministic finite automata
 *
 * @author Vikash Madhow
 * @version 1.0
 */
public class NFA extends Automata {
  public NFA(Set<Edge<State, Character>> edges, State start, State end) {
    super(edges, start);
    this.end = end;
  }

  /**
   * Builds a simple NFA with two states a & b,
   * with a transition under the supplied symbol
   * from a to b
   */
  public static NFA of(char symbol) {
    State start = new State(false);
    State end = new State(true);
    return new NFA(Set.of(new Edge<>(start, symbol, end)), start, end);
  }

  /**
   * return the end state of the NFA
   */
  public State end() {
    return end;
  }

  /**
   * Returns a new NFA which is a closure of this NFA as this:
   * <pre>
   *      +----------->--------------+
   *    /                             \
   *   a -> nfa(s) -> NFA -> nfa(e) -> b
   *          \               /
   *           +------<------+
   * </pre>
   */
  public NFA closure() {
    // first and last state in closure
    State a = new State(false);
    State b = new State(true);

    Set<Edge<State, Character>> edges = new HashSet<>(edges());
    edges.add(new Edge<>(a, EMPTY_STRING, start));
    edges.add(new Edge<>(a, EMPTY_STRING, b));
    edges.add(new Edge<>(end, EMPTY_STRING, b));
    edges.add(new Edge<>(end, EMPTY_STRING, start));

    end.setGoal(false);
    return new NFA(edges, a, b);
  }

  /**
   * Returns the positive closure of this NFA which is a concatenation
   * of (a copy of) this NFA followed by (a copy of) its closure.
   */
  public NFA positiveClosure() {
    return concat(closure());
  }

  /**
   * Returns a new NFA representing the concatenation of
   * this nfa and the supplied one.
   *
   *  s1 --> NFA1 --> e1 --> s2 --> NFA2 --> e2
   */
  public NFA concat(NFA other) {
    Set<Edge<State, Character>> edges = new HashSet<>(edges());
    edges.addAll(other.edges());
    edges.add(new Edge<>(end, EMPTY_STRING, other.start));

    end.setGoal(false);
    return new NFA(edges, start, other.end);
  }

  /**
   * returns the disjunction of this nfa and the provided one
   */
  public NFA or(NFA other) {
    State a = new State(false);
    State b = new State(true);

    Set<Edge<State, Character>> edges = new HashSet<>(edges());
    edges.addAll(other.edges());
    edges.add(new Edge<>(a, EMPTY_STRING, start));
    edges.add(new Edge<>(a, EMPTY_STRING, other.start()));
    edges.add(new Edge<>(end, EMPTY_STRING, b));
    edges.add(new Edge<>(other.end, EMPTY_STRING, b));

    end.setGoal(false);
    other.end.setGoal(false);
    return new NFA(edges, a, b);
  }

  /**
   * The final state of the NFA. The methods in this class ensures that all
   * NFAs that are built have only one ending state
   */
  protected final State end;
}
