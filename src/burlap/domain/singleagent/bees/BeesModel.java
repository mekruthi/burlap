package burlap.domain.singleagent.bees;

import burlap.domain.singleagent.bees.state.BeesAgent;
import burlap.domain.singleagent.bees.state.BeesCell;
import burlap.domain.singleagent.bees.state.BeesState;
import burlap.mdp.core.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

import static burlap.domain.singleagent.bees.Bees.*;

/**
 * @author Shawn Squire.
 */
public class BeesModel implements FullStateModel {

	protected int maxx;
	protected int maxy;

	public BeesModel(int maxx, int maxy) {
		this.maxx = maxx;
		this.maxy = maxy;
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		return FullStateModel.Helper.deterministicTransition(this, s, a);
	}

	@Override
	public State sample(State s, Action a) {

		BeesState bs = (BeesState)s.copy();
		String aname = a.actionName();
		if(aname.equals(ACTION_WEST)){
			move(bs, -1, 0);
		}
		else if(aname.equals(ACTION_EAST)){
			move(bs, 1, 0);
		}
		else if(aname.equals(ACTION_NORTH)){
			move(bs, 0, 1);
		}
		else if(aname.equals(ACTION_SOUTH)){
			move(bs, 0, -1);
		}
		else if(aname.equals(ACTION_IDLE)){
			move(bs, 0, 0);
		}
		else {
			throw new RuntimeException("Unknown action " + aname);
		}
		return bs;
	}


	/**
	 * Modifies state s to be the result of a movement. The agent will not
	 * be able to move to an x or y position &lt; 0 or &gt;= the maximum x or y dimensionality
	 * @param s the state to modify
	 * @param dx the change in x direction; should only be +1 (east) or -1 (west).
	 * @param dy the change in x direction; should only be +1 (east) or -1 (west).
	 */
	public void move(BeesState s, int dx, int dy){
		BeesAgent agent = s.agent.copy();
		s.agent = agent;
		int [][] map = s.map.map;

		int ax = agent.x;
		int ay = agent.y;
		int nx = ax+dx;
		int ny = ay+dy;
		
		if(map[nx][ny] == 1) {
			return;
		}

		if(nx < 0 || nx >= maxx){
			return;
		}
		if(ny < 0 || ny >= maxy){
			return;
		}
		
		BeesCell bee;
		while((bee = this.getBeesAt(s, nx, ny)) != null) {
			agent.health = agent.health - 1;
			s.removeObject(bee.getName());
		}
		
		if(nx == s.honey.x && ny == s.honey.y) {
			agent.hunger = agent.hunger - 1;
		}

		agent.x = nx;
		agent.y = ny;
	}
	
	/**
	 * Finds a bee object in the {@link State} located at the provided position and returns it
	 * @param s the state to check
	 * @param x the x position
	 * @param y the y position
	 * @return the {@link BeesCell} for the corresponding block object in the state at the given position or null if one does not exist.
	 */
	protected BeesCell getBeesAt(BeesState s, int x, int y){
		for(BeesCell b : s.bees){
			if(b.x == x && b.y == y){
				return b;
			}
		}

		return null;
	}
}

