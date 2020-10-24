package ma.vi.regex.automata;

import ma.vi.graph.DirectedEdge;
import ma.vi.graph.DirectedGraph;
import ma.vi.graph.VertexMap;

import java.util.*;

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

	public Automata(VertexMap<State, Character> vertexMap, State start) {
		super(vertexMap);
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
  public char[] symbols() {
    Set<Character> symbols = new HashSet<>();
		for (DirectedEdge<State, Character> edge: edges()) {
			if (edge.weight() != RegEx.EMPTY_STRING) {
				symbols.add(edge.weight());
			}
		}
    int j = 0;
    char[] c = new char[symbols.size()];
		for (Character symbol: symbols) {
			c[j++] = symbol;
		}
    return c;
  }

	/**
	 * The starting state
	 */
	protected State start;
}
