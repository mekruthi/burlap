package burlap.domain.singleagent.bees;

import burlap.domain.singleagent.bees.state.BeesState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;

/**
 * A {@link burlap.mdp.core.TerminalFunction} for {@link burlap.domain.singleagent.bees.Bees}. Returns true
 * when the agent not longer has any hunger.
 * @author Shawn Squire.
 */
public class BeesTF implements TerminalFunction {

	@Override
	public boolean isTerminal(State s) {

		BeesState bs = (BeesState)s;
		return bs.agent.hunger == 0 || bs.agent.health == 0;

	}
}

