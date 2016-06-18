package burlap.domain.singleagent.bees;

import burlap.domain.singleagent.bees.state.BeesState;
import burlap.mdp.core.Action;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

public class BeesRF implements RewardFunction {

	public double goalReward = 1000.0;
	public double lostReward = -1000.0;
	public double stingReward = -500.0;
	public double honeyReward = 200.0; 
	public double defaultReward = -1.0;
	private PropositionalFunction noHealth;
	private PropositionalFunction noHunger;
	private PropositionalFunction nextToBee;
	private PropositionalFunction atHoney;

	public BeesRF(OODomain domain) {
		this.nextToBee = domain.getPropFunction(Bees.PF_NEXT_TO_BEE);
		this.atHoney = domain.getPropFunction(Bees.PF_AT_HONEY);
		this.noHealth = domain.getPropFunction(Bees.PF_NO_HEALTH);
		this.noHunger = domain.getPropFunction(Bees.PF_NO_HUNGER);
	}

	@Override
	public double reward(State s, Action a, State sprime) {
		BeesState bs = (BeesState)s;
		BeesState bsprime = (BeesState)sprime;
		if (noHealth.somePFGroundingIsTrue((OOState)sprime))
			return lostReward;
		if (noHunger.somePFGroundingIsTrue((OOState)sprime))
			return goalReward;
		double actualHoneyReward = 0;
		if(atHoney.somePFGroundingIsTrue((OOState)sprime)) {
			actualHoneyReward = honeyReward;
		}
		if (bs.agent.health > bsprime.agent.health)			
			return (bsprime.agent.health - bs.agent.health)*stingReward + honeyReward;
		
		return defaultReward + actualHoneyReward;
	}

}
