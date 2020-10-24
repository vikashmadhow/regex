package ma.vi.regex;

import ma.vi.graph.DirectedEdge;
import ma.vi.graph.DirectedGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The super class of NFA and DFA.
 * Contains common fields and methods
 *
 * @author Vikash Madhow
 * @version 1.0
 */
public class Automata extends DirectedGraph<State, Character> {
  public Automata(Set<DirectedEdge<State, Character>> edges, State start) {
		super(edges);
		this.start = start;
	}

  /**
   * Return the start state.
   */
  public State start() {
    return start;
  }

  /**
   * Return all distinct input symbols (except empty string) in this automata.
   */
  public Set<Character> symbols() {
    Set<Character> symbols = new HashSet<>();
		for (DirectedEdge<State, Character> edge: edges()) {
			if (edge.weight() != RegEx.EMPTY_STRING) {
				symbols.add(edge.weight());
			}
		}
		return symbols;
  }

	/**
	 * The starting state
	 */
	protected final State start;
}
