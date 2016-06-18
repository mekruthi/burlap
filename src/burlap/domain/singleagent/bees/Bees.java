package burlap.domain.singleagent.bees;

import burlap.behavior.learningrate.ExponentialDecayLR;
import burlap.behavior.learningrate.SoftTimeInverseDecayLR;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.domain.singleagent.bees.state.BeesAgent;
import burlap.domain.singleagent.bees.state.BeesCell;
import burlap.domain.singleagent.bees.state.BeesMap;
import burlap.domain.singleagent.bees.state.BeesState;
import burlap.domain.singleagent.blocksworld.BlocksWorldBlock;
import burlap.domain.singleagent.blocksworld.BlocksWorldState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.UniformCostRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.action.UniversalActionType;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.ArrayList;
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
	protected int maxx = 25;

	/**
	 * Domain parameter specifying the maximum y dimension value of the world
	 */
	protected int maxy = 25;

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
	 * Name for the propositional function that tests whether the agent is stanng next to a bee.
	 */
	public static final String PF_NEXT_TO_BEE = "nextToBee";

	/**
	 * Name for the propositional function that tests whether the agent is out of health.
	 */
	public static final String PF_NO_HEALTH = "noHealth";

	/**
	 * Name for the propositional function that tests whether the agent has no hunger.
	 */
	public static final String PF_NO_HUNGER = "noHunger";

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
		return Arrays.asList(new AtHoneyPF(), new AtBeePF(), new NextToBeePF(), new NoHungerPF(), new NoHealthPF());
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
		
		TerminalFunction tf = this.tf;
		RewardFunction rf = this.rf;

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
			BeesState s = (BeesState)st;
			BeesAgent a = (BeesAgent)s.object(params[0]);
			BeesCell h = (BeesCell)s.object(params[1]);
			return h.getClassName() == CLASS_HONEY && a.x == h.x && a.y == h.y;
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
			BeesState s = (BeesState)st;
			BeesAgent a = (BeesAgent)s.object(params[0]);
			BeesCell b = (BeesCell)s.object(params[1]);
			return b.getClassName() == CLASS_BEE && a.x == b.x && a.y == b.y;
		}
	}

	/**
	 * A {@link PropositionalFunction} that takes as arguments an agent object and a bee object
	 * and evaluates whether the agent is hit by a bee.
	 */
	public class NextToBeePF extends PropositionalFunction {

		public NextToBeePF() {
			super(PF_NEXT_TO_BEE, new String[]{CLASS_AGENT, CLASS_BEE});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			BeesState s = (BeesState)st;
			BeesAgent a = (BeesAgent)s.object(params[0]);
			BeesCell b = (BeesCell)s.object(params[1]);
			return b.getClassName() == CLASS_BEE && (
					a.x - 1 == b.x && a.y == b.y ||
					a.x + 1 == b.x && a.y == b.y ||
					a.x == b.x && a.y - 1 == b.y ||
					a.x == b.x && a.y + 1 == b.y);
		}
	}

	/**
	 * A {@link PropositionalFunction} that takes as arguments an agent object and evaluates
	 * whether the agent has no health.
	 */
	public class NoHealthPF extends PropositionalFunction {
		public NoHealthPF() {
			super(PF_NO_HEALTH, new String[]{CLASS_AGENT});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			BeesState s = (BeesState)st;
			BeesAgent a = (BeesAgent)s.object(params[0]);
			return a.health <= 0;
		}
	}

	/**
	 * A {@link PropositionalFunction} that takes as arguments an agent object and evaluates
	 * whether the agent is no longer hungry.
	 */
	public class NoHungerPF extends PropositionalFunction {
		public NoHungerPF() {
			super(PF_NO_HUNGER, new String[]{CLASS_AGENT});
		}

		@Override
		public boolean isTrue(OOState st, String... params) {
			BeesState s = (BeesState)st;
			BeesAgent a = (BeesAgent)s.object(params[0]);
			return a.hunger <= 0;
		}
	}

	/**
	 * Runs an interactive visual explorer for random bees level. The keys w,a,s,d,x correspond to actions
	 * north, west, south, east, idle
	 * @param args can be empty.
	 */
	public static void main(String[] args) {
		Bees be = new Bees();
		OOSADomain domain = be.generateDomain();
		be.setTf(new BeesTF());
		be.setRf(new BeesRF(domain));

		int [][] map = new int[25][25];
		ArrayList<BeesCell> bees = new ArrayList<BeesCell>();
		for(int i = 0; i < 4; i++) {
			BeesCell b = BeesCell.bee("b" + i, 23, 23);
			bees.add(b);
		}
		
		BeesState s = new BeesState(
				new BeesAgent(15, 1, 4, 1),
				new BeesMap(map),
				BeesCell.honey("honey", 3, 21),
				bees
		);
		
		//runInteractive(be, domain, s);
		//runPlanner(be, domain, s);
		runLearner(be, domain, s);
	}
	
	private static void runInteractive(Bees be, OOSADomain domain, BeesState initialState) {
		Visualizer v = BeesVisualizer.getVisualizer(be.maxx, be.maxy);
		VisualExplorer exp = new VisualExplorer(domain, v, initialState);

		exp.addKeyAction("w", ACTION_NORTH, "");
		exp.addKeyAction("d", ACTION_EAST, "");
		exp.addKeyAction("a", ACTION_WEST, "");
		exp.addKeyAction("s", ACTION_SOUTH, "");
		exp.addKeyAction("x", ACTION_IDLE, "");

		exp.initGUI();	
	}
	
	private static void runPlanner(Bees be, OOSADomain domain, BeesState initialState) {
		TFGoalCondition goalCondition = new TFGoalCondition(be.getTf());
		SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		
		Heuristic mdistHeuristic = new Heuristic() {
			public double h(State s) {
				BeesAgent a = ((BeesState)s).agent;
				BeesCell h = ((BeesState)s).honey;
				double mdist = Math.abs(a.x - h.x) + Math.abs(a.y - h.y);
				return -mdist;
			}
		};
		
		DeterministicPlanner planner = new AStar(domain, goalCondition, hashingFactory, mdistHeuristic);
		Policy p = planner.planFromState(initialState);
		PolicyUtils.rollout(p,  initialState, domain.getModel()).writeToFile("output/star/");
		
		Visualizer v = BeesVisualizer.getVisualizer(be.maxx, be.maxy);
		new EpisodeSequenceVisualizer(v, domain, "ouptut/bfs/");
	}
	
	private static void runLearner(Bees be, OOSADomain domain, BeesState initialState) {
		double SAVE_PERCENT = 0.05; // percent of last runs to save
		String OUTPUT_FOLDER = "output/ql/";
		int POST_WIN_SAVE = 3;
		
		double GAMMA = 0.9;
		double QINIT = 0.;
		double LEARNING_RATE = 0.1;
		int MAX_STEPS = 500;
		double LAMBDA = 0.9;
		double EPSILON = 0.1;
		
		SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		//LearningAgent agent = new QLearning(domain, GAMMA, hashingFactory, QINIT, LEARNING_RATE, MAX_STEPS);
		SarsaLam agent = new SarsaLam(domain, GAMMA, hashingFactory, QINIT, LEARNING_RATE, MAX_STEPS, LAMBDA);
		agent.setLearningPolicy(new EpsilonGreedy(agent, EPSILON));
		SimulatedEnvironment env = new SimulatedEnvironment(domain, initialState);

		ArrayList<Integer> steps = new ArrayList<Integer>();
		ArrayList<Double> rewards = new ArrayList<Double>();
		
		int EPISODE_COUNT = 10000;
		int last_win = 0 - POST_WIN_SAVE;
		PropositionalFunction noHunger = domain.getPropFunction(PF_NO_HUNGER);
		// Start the episodes
		for (int i = 0; i < EPISODE_COUNT; i++) {
			Episode e = agent.runLearningEpisode(env, MAX_STEPS);
			BeesState s = ((BeesState)e.getState(e.maxTimeStep()));
			
			// Save if win or immediately following win
			if(noHunger.somePFGroundingIsTrue(s)) {
				e.writeToFile(OUTPUT_FOLDER + i);
				System.out.println("Success on episode " + i);
				last_win = i;
			}
			else if(i <= last_win + POST_WIN_SAVE) {
				e.writeToFile(OUTPUT_FOLDER + i);
			}
			// Save last 10%
			else if(i > (EPISODE_COUNT * (1 - SAVE_PERCENT))) {
				e.writeToFile(OUTPUT_FOLDER + i);
			}
			
			
			steps.add(e.maxTimeStep());
			rewards.add(e.getReward(e.maxTimeStep()));

			if(i % (EPISODE_COUNT / 20) == 0 && i > 0) {
				System.out.println((i / (EPISODE_COUNT / 100)) + "% complete...");
			}
			env.resetEnvironment();
		}
		
		Visualizer v = BeesVisualizer.getVisualizer(be.maxx, be.maxy);
		new EpisodeSequenceVisualizer(v, domain, OUTPUT_FOLDER);
	}
}

