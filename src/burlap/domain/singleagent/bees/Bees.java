package burlap.domain.singleagent.bees;

import burlap.domain.singleagent.bees.state.BeesAgent;
import burlap.domain.singleagent.bees.state.BeesCell;
import burlap.domain.singleagent.bees.state.BeesMap;
import burlap.domain.singleagent.bees.state.BeesState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.action.UniversalActionType;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.Visualizer;

import java.util.Arrays;
import java.util.List;

/**
 */
public class Bees implements DomainGenerator {

	/**
	 * X position attribute name
	 */
	public static final String VAR_X = "x";

	/**
	 * Y position attribute name
	 */
	public static final String VAR_Y = "y";

	/**
	 * Health attribute name
	 */
	public static final String VAR_HEALTH = "health";

	/**
	 * Hunger attribute name
	 */
	public static final String VAR_HUNGER = "hunger";

	/**
	 * Name for the attribute that holds the map
	 */
	public static final String VAR_MAP = "map";

	/**
	 * Domain parameter specifying the maximum x dimension value of the world
	 */
	protected int										maxx = 25;

	/**
	 * Domain parameter specifying the maximum y dimension value of the world
	 */
	protected int										maxy = 25;

	/**
	 * Name for the agent OO-MDP class
	 */
	public static final String CLASS_AGENT = "agent";

	/**
	 * Name for the honey OO-MDP class
	 */
	public static final String CLASS_HONEY = "honey";

	/**
	 * Name for the map OO-MDP class
	 */
	public static final String CLASS_MAP = "map";

	/**
	 * Name for the bee OO-MDP class
	 */
	public static final String CLASS_BEE = "bee";

	protected RewardFunction rf;
	protected TerminalFunction tf;


	/**
	 * Name for the north action
	 */
	public static final String ACTION_NORTH = "north";

	/**
	 * Name for the north action
	 */
	public static final String ACTION_SOUTH = "south";

	/**
	 * Name for the east action
	 */
	public static final String ACTION_EAST = "east";

	/**
	 * Name for the west action
	 */
	public static final String ACTION_WEST = "west";

	/**
	 * Name for the idle action
	 */
	public static final String ACTION_IDLE = "idle";

	/**
	 * Name for the propositional function that tests whether the agent is at the honey.
	 */
	public static final String PF_AT_HONEY = "atHoney";

	/**
	 * Name for the propositional function that tests whether the agent is hit by a bee.
	 */
	public static final String PF_AT_BEE = "atBee";

	/**
	 * Initializes a world with a maximum 25x25 dimensionality and actions that use semi-deep state copies.
	 */
	public Bees() {
		//do nothing
	}

	/**
	 * Initializes a world with the maximum space dimensionality provided and actions that use semi-deep state copies.
	 * @param maxx max x size of the world
	 * @param maxy max y size of the world
	 */
	public Bees(int maxx, int maxy) {
		this.maxx = maxx;
		this.maxy = maxy;
	}

	public List<PropositionalFunction> generatePfs(){
		return Arrays.asList(new AtHoneyPF(), new AtBeePF());
	}

	@Override
	public OOSADomain generateDomain() {
		OOSADomain domain = new OOSADomain();

		domain.addStateClass(CLASS_AGENT, BeesAgent.class)
				.addStateClass(CLASS_MAP, BeesMap.class)
				.addStateClass(CLASS_HONEY, BeesCell.class)
				.addStateClass(CLASS_BEE, BeesCell.class);


		domain.addActionType(new UniversalActionType(ACTION_EAST))
				.addActionType(new UniversalActionType(ACTION_WEST))
				.addActionType(new UniversalActionType(ACTION_NORTH))
				.addActionType(new UniversalActionType(ACTION_SOUTH))
				.addActionType(new UniversalActionType(ACTION_IDLE));

		OODomain.Helper.addPfsToDomain(domain, this.generatePfs());

		RewardFunction rf = this.rf;
		TerminalFunction tf = this.tf;

		if(tf == null){
			tf = new BeesTF();
		}
		if(rf == null){
			rf = new UniformCostRF();
		}

		BeesModel smodel = new BeesModel(maxx, maxy);
		FactoredModel model = new FactoredModel(smodel, rf, tf);
		domain.setModel(model);
		return domain;
	}

	public int getMaxx() {
		return maxx;
	}

	public void setMaxx(int maxx) {
		this.maxx = maxx;
	}

	public int getMaxy() {
		return maxy;
	}

	public void setMaxy(int maxy) {
		this.maxy = maxy;
	}

	public RewardFunction getRf() {
		return rf;
	}

	public void setRf(RewardFunction rf) {
		this.rf = rf;
	}

	public TerminalFunction getTf() {
		return tf;
	}

	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}


	/**
	 * A {@link PropositionalFunction} that takes as arguments an agent object and a honey object
	 * and evaluates whether the agent is at the honey.
	 */
	public class AtHoneyPF extends PropositionalFunction {

		public AtHoneyPF() {
			super(PF_AT_HONEY, new String[]{CLASS_AGENT, CLASS_HONEY});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			return false;
		}
	}

	/**
	 * A {@link PropositionalFunction} that takes as arguments an agent object and a bee object
	 * and evaluates whether the agent is hit by a bee.
	 */
	public class AtBeePF extends PropositionalFunction {

		public AtBeePF() {
			super(PF_AT_BEE, new String[]{CLASS_AGENT, CLASS_BEE});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			return false;
		}
	}

	/**
	 * Runs an interactive visual explorer for random bees level. The keys w,a,s,d,x correspond to actions
	 * north, west, south, east, idle
	 * @param args can be empty.
	 */
	public static void main(String[] args) {

		Bees be = new Bees();
		SADomain domain = be.generateDomain();

		int [][] map = new int[25][25];
		
		BeesState s = new BeesState(
				new BeesAgent(15, 1, 15, 10),
				new BeesMap(map),
				BeesCell.honey("honey", 3, 21),
				BeesCell.bee("b0", 9, 1),
				BeesCell.bee("b1", 13, 1),
				BeesCell.bee("b1", 13, 3),
				BeesCell.bee("b1", 3, 20),
				BeesCell.bee("b1", 4, 21),
				BeesCell.bee("b1", 21, 6)
		);

		Visualizer v = BeesVisualizer.getVisualizer(be.maxx, be.maxy);
		VisualExplorer exp = new VisualExplorer(domain, v, s);

		exp.addKeyAction("w", ACTION_NORTH, "");
		exp.addKeyAction("d", ACTION_EAST, "");
		exp.addKeyAction("a", ACTION_WEST, "");
		exp.addKeyAction("s", ACTION_SOUTH, "");
		exp.addKeyAction("x", ACTION_IDLE, "");

		exp.initGUI();



	}
}

