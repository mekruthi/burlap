package burlap.domain.singleagent.bees.state;

import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.annotations.DeepCopyState;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.bees.Bees.*;

/**
 * @author James MacGlashan.
 */
@DeepCopyState
public class BeesAgent implements ObjectInstance {

	public int x;
	public int y;
	public int health;
	public int hunger;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_HEALTH, VAR_HUNGER);

	public BeesAgent() {
	}

	public BeesAgent(int x, int y, int health, int hunger) {
		this.x = x;
		this.y = y;
		this.health = health;
		this.hunger = hunger;
	}

	@Override
	public String className() {
		return CLASS_AGENT;
	}

	@Override
	public String name() {
		return CLASS_AGENT;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		throw new RuntimeException("Bees agent must always be called agent");
	}

	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if(variableKey.equals(VAR_X)){
			return x;
		}
		else if(variableKey.equals(VAR_Y)){
			return y;
		}
		else if(variableKey.equals(VAR_HEALTH)){
			return health;
		}
		else if(variableKey.equals(VAR_HUNGER)){
			return hunger;
		}
		throw new RuntimeException("Unknown key " + variableKey);
	}

	@Override
	public BeesAgent copy() {
		return new BeesAgent(x, y, health, hunger);
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
