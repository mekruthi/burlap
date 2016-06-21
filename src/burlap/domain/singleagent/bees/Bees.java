package burlap.domain.singleagent.bees;

import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCT;
import burlap.domain.singleagent.bees.state.BeesAgent;
import burlap.domain.singleagent.bees.state.BeesCell;
import burlap.domain.singleagent.bees.state.BeesMap;
import burlap.domain.singleagent.bees.state.BeesState;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.action.UniversalActionType;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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
	 * File to find config
	 */
	private static final String CONFIG_FILE = "config/bees.json";
	
	/**
	 * Config file saved as JSONObject
	 */
	private BeesConfig config;

	/**
	 * Initializes a world with a maximum 25x25 dimensionality and actions that use semi-deep state copies.
	 */
	public Bees() {
		Gson configJson = new Gson();
		try {
			config = configJson.fromJson(new FileReader(CONFIG_FILE), BeesConfig.class);
			this.maxx = config.domain.map.size[0];
			this.maxy = config.domain.map.size[1];
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes a world with the maximum space dimensionality provided and actions that use semi-deep state copies.
	 * @param maxx max x size of the world
	 * @param maxy max y size of the world
	 */
	public Bees(int maxx, int maxy) {
		this();
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
			HashMap<String, Double> rewards = new HashMap<String, Double>();
			rewards.put("goal", this.config.domain.rewards.goal);
			rewards.put("lost", this.config.domain.rewards.lost);
			rewards.put("sting", this.config.domain.rewards.sting);
			rewards.put("honey", this.config.domain.rewards.honey);
			rewards.put("default", this.config.domain.rewards.defaults);
			rf = new BeesRF(domain, rewards);
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

		int [][] map = new int[be.maxx][be.maxy];
		ArrayList<BeesCell> bees = new ArrayList<BeesCell>();
		for(int i = 0; i < be.config.domain.bees.count; i++) {
			BeesCell b = BeesCell.bee("b" + i, be.config.domain.bees.spawn[0], be.config.domain.bees.spawn[1]);
			bees.add(b);
		}
		
		BeesState s = new BeesState(
				new BeesAgent(be.config.domain.agent.spawn[0], be.config.domain.agent.spawn[1],
						be.config.domain.agent.health, be.config.domain.agent.hunger),
				new BeesMap(map),
				BeesCell.honey("honey", be.config.domain.honey.spawn[0], be.config.domain.honey.spawn[1]),
				bees
		);
		
		if(args.length > 0) {
			if(args[0].equals("-i")) {
				runInteractive(be, domain, s);
			} else if(args[0].equals("-p")) {
				runPlanner(be, domain, s);
			} else if(args[0].equals("-l")) {
				runLearner(be, domain, s);
			}
		} else {
			runInteractive(be, domain, s);
		}
	}
	
	private static void runInteractive(Bees be, OOSADomain domain, BeesState initialState) {
		Visualizer v = BeesVisualizer.getVisualizer(be.maxx, be.maxy);
		VisualExplorer exp = new VisualExplorer(domain, v, initialState);
		exp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		exp.addKeyAction("w", ACTION_NORTH, "");
		exp.addKeyAction("d", ACTION_EAST, "");
		exp.addKeyAction("a", ACTION_WEST, "");
		exp.addKeyAction("s", ACTION_SOUTH, "");
		exp.addKeyAction("x", ACTION_IDLE, "");

		exp.initGUI();	
	}
	
	private static void runPlanner(Bees be, OOSADomain domain, BeesState initialState) {
		SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		SimulatedEnvironment env = new SimulatedEnvironment(domain, initialState);
		
		UCT planner = new UCT(domain, be.config.planner.gamma, hashingFactory,
				be.config.planner.horizon, be.config.planner.max_rollouts,
				be.config.planner.exploration);
		planner.useGoalConditionStopCriteria(new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				BeesState bs = (BeesState)s;
				return (bs.agent.hunger == 0);
			}
		});
		
		Policy p = planner.planFromState(initialState);
		PolicyUtils.rollout(p, env).writeToFile(be.config.planner.output + "uct");
		
		BeesVisualizer.getEpisodeSequenceVisualizer(be, domain, be.config.planner.output);
	}
	
	private static void runLearner(Bees be, OOSADomain domain, BeesState initialState) {		
		SimpleHashableStateFactory hashingFactory = new SimpleHashableStateFactory();
		QLearning agent = new QLearning(domain, be.config.learner.gamma, hashingFactory,
				be.config.learner.qinit, be.config.learner.learning_rate, be.config.learner.max_steps);
		agent.setLearningPolicy(new EpsilonGreedy(agent, be.config.learner.max_steps));
		SimulatedEnvironment env = new SimulatedEnvironment(domain, initialState);
		
		int last_win = 0 - be.config.learner.post_win_save;
		PropositionalFunction noHunger = domain.getPropFunction(PF_NO_HUNGER);
		// Start the episodes
		for (int i = 0; i < be.config.learner.episodes; i++) {
			Episode e = agent.runLearningEpisode(env, be.config.learner.max_steps);
			BeesState s = ((BeesState)e.getState(e.maxTimeStep()));
			
			// Save if win or immediately following win
			if(noHunger.somePFGroundingIsTrue(s)) {
				e.writeToFile(be.config.learner.output + i);
				System.out.println("Success on episode " + i);
				last_win = i;
			}
			else if(i <= last_win + be.config.learner.post_win_save) {
				e.writeToFile(be.config.learner.output + i);
			}
			// Save last 10%
			else if(i > (be.config.learner.episodes * (1 - be.config.learner.save_percent))) {
				e.writeToFile(be.config.learner.output + i);
			}

			if(i % (be.config.learner.episodes / 20) == 0 && i > 0) {
				System.out.println((i / (be.config.learner.episodes / 100)) + "% complete...");
			}
			env.resetEnvironment();
		}
		
		BeesVisualizer.getEpisodeSequenceVisualizer(be, domain, be.config.learner.output);
	}
}

