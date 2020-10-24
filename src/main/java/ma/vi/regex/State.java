package ma.vi.regex.automata;

/**
 * Represents a state in an automaton.
 *
 * @author Vikash Madhow
 */
public class State {
  public State() {
    this(false);
  }

  public State(boolean goal) {
    this.goal = goal;
  }

  public boolean isGoal() {
    return goal;
  }

  public void setGoal(boolean goal) {
    this.goal = goal;
  }

  private boolean goal;
}
