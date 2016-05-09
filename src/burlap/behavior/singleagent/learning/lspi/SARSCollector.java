package burlap.behavior.singleagent.learning.lspi;

import java.util.List;

import burlap.debugtools.RandomFactory;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.Action;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.RewardFunction;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;


/**
 * This object is used to collected {@link SARSData} (state-action-reard-state tuples) that can then be used by algorithms like LSPI for learning.
 * @author James MacGlashan
 *
 */
public abstract class SARSCollector {
	
	/**
	 * The actions used for collecting data.
	 */
	protected List<Action> actions;
	
	
	/**
	 * Initializes the collector's action set using the actions that are part of the domain.
	 * @param domain the domain containing the actions to use
	 */
	public SARSCollector(Domain domain){
		this.actions = domain.getActions();
	}
	
	/**
	 * Initializes this collector's action set to use for collecting data.
	 * @param actions the action set to use for collecting data.
	 */
	public SARSCollector(List<Action> actions){
		this.actions = actions;
	}
	
	/**
	 * Collects data from an initial state until either a terminal state is reached or until the maximum number of steps is taken.
	 * Data is stored into the dataset intoDataset and returned. If intoDataset is null, then it is first created.
	 * @param s the initial state from which data should be collected.
	 * @param rf the reward function from which rewards are tracks.
	 * @param maxSteps the maximum number of steps that can be taken.
	 * @param tf the terminal function that ends data collection.
	 * @param intoDataset the dataset into which data will be stored. If null, a dataset is created.
	 * @return the resulting dataset with the newly collected data.
	 */
	public abstract SARSData collectDataFrom(State s, RewardFunction rf, int maxSteps, TerminalFunction tf, SARSData intoDataset);

	/**
	 * Collects data from an {@link burlap.mdp.singleagent.environment.Environment}'s current state until either the maximum
	 * number of steps is taken or a terminal state is reached.
	 * Data is stored into the dataset intoDataset and returned. If intoDataset is null, then it is first created.
	 * @param env the {@link burlap.mdp.singleagent.environment.Environment} from which data will be collected.
	 * @param maxSteps the maximum number of steps to take in the environment.
	 * @param intoDataset the dataset into which data will be stored. If null, a dataset is created.
	 * @return the resulting dataset with the newly collected data.
	 */
	public abstract SARSData collectDataFrom(Environment env, int maxSteps, SARSData intoDataset);
	
	
	/**
	 * Collects nSamples of SARS tuples and returns it in a {@link SARSData} object.
	 * @param sg a state generator for finding initial state from which data can be collected.
	 * @param rf the reward function that defines the reward received.
	 * @param nSamples the number of SARS samples to collect.
	 * @param maxEpisodeSteps the maximum number of steps that can be taken when rolling out from a state generated by {@link StateGenerator} sg, before a new rollout is started.
	 * @param tf the terminal function that caused a rollout to stop and a new state to be generated from {@link StateGenerator} sg.
	 * @param intoDataset the dataset into which the results will be collected. If null, a new dataset is created.
	 * @return the intoDataset object, which is created if it is input as null.
	 */
	public SARSData collectNInstances(StateGenerator sg, RewardFunction rf, int nSamples, int maxEpisodeSteps, TerminalFunction tf, SARSData intoDataset){
		
		if(intoDataset == null){
			intoDataset = new SARSData(nSamples);
		}
		
		while(nSamples > 0){
			int maxSteps = Math.min(nSamples, maxEpisodeSteps);
			int oldSize = intoDataset.size();
			this.collectDataFrom(sg.generateState(), rf, maxSteps, tf, intoDataset);
			int delta = intoDataset.size() - oldSize;
			nSamples -= delta;
		}
		
		return intoDataset;
		
	}


	/**
	 * Collects nSamples of SARS tuples from an {@link burlap.mdp.singleagent.environment.Environment} and returns it in a {@link burlap.behavior.singleagent.learning.lspi.SARSData} object.
	 * Each sequence of samples is no longer than maxEpisodeSteps and samples are collected using this object's {@link #collectDataFrom(burlap.mdp.singleagent.environment.Environment, int, SARSData)}
	 * method. After each call to {@link #collectDataFrom(burlap.mdp.singleagent.environment.Environment, int, SARSData)}, the provided {@link burlap.mdp.singleagent.environment.Environment}
	 * is sent the {@link burlap.mdp.singleagent.environment.Environment#resetEnvironment()} message.
	 * @param env The {@link burlap.mdp.singleagent.environment.Environment} from which samples should be collected.
	 * @param nSamples The number of samples to generate.
	 * @param maxEpisodeSteps the maximum number of steps to take from any initial state of the {@link burlap.mdp.singleagent.environment.Environment}.
	 * @param intoDataset the dataset into which the results will be collected. If null, a new dataset is created.
	 * @return the intoDataset object, which is created if it is input as null.
	 */
	public SARSData collectNInstances(Environment env, int nSamples, int maxEpisodeSteps, SARSData intoDataset){

		if(intoDataset == null){
			intoDataset = new SARSData(nSamples);
		}

		while(nSamples > 0){
			int maxSteps = Math.min(nSamples, maxEpisodeSteps);
			int oldSize = intoDataset.size();
			this.collectDataFrom(env, maxSteps, intoDataset);
			int delta = intoDataset.size() - oldSize;
			nSamples -= delta;
			env.resetEnvironment();
		}

		return intoDataset;

	}
	
	
	
	/**
	 * Collects SARS data from source states generated by a {@link StateGenerator} by choosing actions uniformly at random.
	 * @author James MacGlashan
	 *
	 */
	public static class UniformRandomSARSCollector extends SARSCollector{

		/**
		 * Initializes the collector's action set using the actions that are part of the domain.
		 * @param domain the domain containing the actions to use
		 */
		public UniformRandomSARSCollector(Domain domain) {
			super(domain);
		}
		
		/**
		 * Initializes this collector's action set to use for collecting data.
		 * @param actions the action set to use for collecting data.
		 */
		public UniformRandomSARSCollector(List<Action> actions) {
			super(actions);
		}

		@Override
		public SARSData collectDataFrom(State s, RewardFunction rf, int maxSteps,
				TerminalFunction tf, SARSData intoDataset) {
			
			if(intoDataset == null){
				intoDataset = new SARSData();
			}
			
			State curState = s;
			int nsteps = 0;
			while(!tf.isTerminal(curState) && nsteps < maxSteps){
				
				List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, curState);
				GroundedAction ga = gas.get(RandomFactory.getMapped(0).nextInt(gas.size()));
				State nextState = ga.sample(curState);
				double r = rf.reward(curState, ga, nextState);
				intoDataset.add(curState, ga, r, nextState);
				curState = nextState;
				
				nsteps++;
				
			}
			
			
			return intoDataset;
			
		}

		@Override
		public SARSData collectDataFrom(Environment env, int maxSteps, SARSData intoDataset) {

			if(intoDataset == null){
				intoDataset = new SARSData();
			}

			int nsteps = 0;
			while(!env.isInTerminalState() && nsteps < maxSteps){
				List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, env.getCurrentObservation());
				GroundedAction ga = gas.get(RandomFactory.getMapped(0).nextInt(gas.size()));
				EnvironmentOutcome eo = ga.executeIn(env);
				intoDataset.add(eo.o, eo.a, eo.r, eo.op);

				nsteps++;
			}

			return intoDataset;
		}
	}
	
}
