package burlap.domain.singleagent.bees;

import burlap.domain.singleagent.bees.state.BeesAgent;
import burlap.domain.singleagent.bees.state.BeesCell;
import burlap.domain.singleagent.bees.state.BeesState;
import burlap.mdp.core.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static burlap.domain.singleagent.bees.Bees.*;

/**
 * @author Shawn Squire.
 */
public class BeesModel implements FullStateModel {

	protected int maxx;
	protected int maxy;
	
	private double PROB_RANDOM_BEE = 0.2;

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

		if(nx >= 0 && nx < maxx && ny >= 0 && ny < maxy && map[nx][ny] != 1){
			agent.x = nx;
			agent.y = ny;
		}

		// Let the bees move
		for(BeesCell b : s.bees) {			
			b = this.moveBeeCloser(s, b, PROB_RANDOM_BEE);			
			if(b.x == agent.x && b.y == agent.y) {
				agent.health = Math.max(agent.health - 1, 0);
				s.removeObject(b.name());
			}
		}
		
		if(nx == s.honey.x && ny == s.honey.y) {
			agent.hunger = Math.max(agent.hunger - 1, 0);
		}
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
	
	private BeesCell moveBee(BeesState s, BeesCell b, int nx, int ny) {
		s.copyBees();
		s.bees.remove(b);
		b = b.copy();
		s.bees.add(b);
		
		b.x = nx;
		b.y = ny;
		
		return b;
	}
	
	private BeesCell moveBeeRandom(BeesState s, BeesCell b, double probMove) {
		Random rn = new Random();
		int [][] map = s.map.map;

		int bx = b.x;
		int by = b.y;
		int nx = bx;
		int ny = by;
		
		double i = rn.nextDouble();
		// Roll dice for move, might move into wall, because bees are dumb
		if(i < probMove) {
			double e = rn.nextDouble();
			if(e < 0.25) {
				nx = bx - 1;
			} else if(e < 0.5) {
				nx = bx + 1;
			} else if(e < 0.75) {
				ny = by - 1;
			} else {
				ny = by + 1;
			}
		}

		if(nx < 0 || nx >= maxx || ny < 0 || ny >= maxy){
			return b;
		}
		if(map[nx][ny] == 1) {
			return b;
		}
		
		return this.moveBee(s, b, nx, ny);
	}
	
	private BeesCell moveBeeCloser(BeesState s, BeesCell b, double probRandom) {
		Random rn = new Random();
		BeesAgent agent = (BeesAgent)s.agent;
		int [][] map = s.map.map;

		int ax = agent.x;
		int ay = agent.y;
		int bx = b.x;
		int by = b.y;
		int nx = bx;
		int ny = by;
		
		double i = rn.nextDouble();
		// Chance for bee to choose to roam randomly
		if(i < probRandom) {
			moveBeeRandom(s, b, 1.0);
			return b;
		}
		
		nx = bx + Math.max(Math.min(ax - bx, 1), -1);
		ny = by + Math.max(Math.min(ay - by, 1), -1);
		if(nx != bx && ny != by) {
			// Do not move diagonally, randomly pick one
			if(rn.nextFloat() < 0.5) {
				nx = bx;
			} else {
				ny = by;
			}
		}

		if(nx < 0 || nx >= maxx || ny < 0 || ny >= maxy){
			return b;
		}
		if(map[nx][ny] == 1) {
			return b;
		}
		
		return this.moveBee(s, b, nx, ny);
	}
}

