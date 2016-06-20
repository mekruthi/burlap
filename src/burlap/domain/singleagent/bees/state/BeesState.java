package burlap.domain.singleagent.bees.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static burlap.domain.singleagent.bees.Bees.*;

/**
 * @author James MacGlashan.
 */
@ShallowCopyState
public class BeesState implements MutableOOState {

	public BeesAgent agent;
	public BeesMap map;
	public BeesCell honey;
	public List<BeesCell> bees;

	public BeesState() {
	}

	public BeesState(int w, int h, int nBees) {
		this.agent = new BeesAgent();
		this.map = new BeesMap(w, h);
		this.honey = new BeesCell(CLASS_HONEY, CLASS_HONEY);
		this.bees = new ArrayList<BeesCell>(nBees);
		for(int i = 0; i < nBees; i++){
			this.bees.add(new BeesCell(CLASS_BEE, CLASS_BEE+i));
		}
	}

	public BeesState(BeesAgent agent, BeesMap map, BeesCell honey, BeesCell...bees) {
		this.agent = agent;
		this.map = map;
		this.honey = honey;
		this.bees = Arrays.asList(bees);
	}

	public BeesState(BeesAgent agent, BeesMap map, BeesCell honey, List<BeesCell> bees) {
		this.agent = agent;
		this.map = map;
		this.honey = honey;
		this.bees = bees;
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {

		if(!(o instanceof BeesCell) || !o.className().equals(CLASS_BEE)){
			throw new RuntimeException("Can only add bee objects to state.");
		}
		//copy on write
		this.bees = new ArrayList<BeesCell>(bees);
		bees.add((BeesCell) o);

		return this;
	}

	@Override
	public MutableOOState removeObject(String oname) {

		int ind = this.beeForName(oname);
		if(ind == -1){
			throw new RuntimeException("Can only remove bee objects from state.");
		}
		//copy on write
		this.bees = new ArrayList<BeesCell>(bees);
		this.bees.remove(ind);

		return this;
	}

	@Override
	public MutableOOState renameObject(String objectName, String newName) {

		int ind = this.beeForName(objectName);
		if(ind == -1){
			throw new RuntimeException("Can only rename block objects.");
		}
		BeesCell oldBlock = this.bees.get(ind);
		//copy on write
		this.bees = new ArrayList<BeesCell>(bees);
		this.bees.remove(ind);
		this.bees.add((BeesCell) oldBlock.copyWithName(newName));

		return this;
	}

	@Override
	public int numObjects() {
		return this.bees.size() + 3;
	}

	@Override
	public ObjectInstance object(String oname) {
		if(oname.equals(CLASS_AGENT)){
			return agent;
		}
		else if(oname.equals(CLASS_MAP)){
			return map;
		}
		else if(oname.equals(honey.name())){
			return honey;
		}
		int ind = this.beeForName(oname);
		if (ind != -1){
			return this.bees.get(ind);
		}


		return null;
	}

	@Override
	public List<ObjectInstance> objects() {
		ArrayList<ObjectInstance> obs = new ArrayList<ObjectInstance>(this.bees);
		obs.add(agent);
		obs.add(map);
		obs.add(honey);
		return obs;
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		if(oclass.equals(CLASS_AGENT)){
			return Arrays.<ObjectInstance>asList(agent);
		}
		else if(oclass.equals(CLASS_MAP)){
			return Arrays.<ObjectInstance>asList(map);
		}
		else if(oclass.equals(CLASS_HONEY)){
			return Arrays.<ObjectInstance>asList(honey);
		}
		else if(oclass.equals(CLASS_BEE)){
			return new ArrayList<ObjectInstance>(bees);
		}
		throw new RuntimeException("No object class " + oclass);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {

		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		if(key.obName.equals(CLASS_AGENT)){
			this.agent = this.agent.copy();
			if(variableKey.equals(VAR_X)){
				agent.x = StateUtilities.stringOrNumber(value).intValue();
			}
			else if(variableKey.equals(VAR_Y)){
				agent.y = StateUtilities.stringOrNumber(value).intValue();
			}
			else if(variableKey.equals(VAR_HEALTH)) {
				agent.health = StateUtilities.stringOrNumber(value).intValue();
			}
			else if(variableKey.equals(VAR_HUNGER)) {
				agent.hunger = StateUtilities.stringOrNumber(value).intValue();
			}
		}
		else if(key.obName.equals(CLASS_MAP)){
			this.map = map.copy();
			this.map.map = (int[][])value;
		}
		else if(key.obName.equals(honey.name())){
			Integer iv = StateUtilities.stringOrNumber(value).intValue();
			if(variableKey.equals(VAR_X)){
				honey.x = iv;
			}
			else if(variableKey.equals(VAR_Y)){
				honey.y = iv;
			}
		}
		else{
			int ind = beeForName(key.obName);
			if(ind != -1){
				BeesCell block = this.bees.get(ind).copy();
				this.bees = new ArrayList<BeesCell>(bees);
				this.bees.set(ind, block);
				Integer iv = StateUtilities.stringOrNumber(value).intValue();
				if(variableKey.equals(VAR_X)){
					block.x = iv;
				}
				else if(variableKey.equals(VAR_Y)){
					block.y = iv;
				}
			}
		}


		return this;
	}

	@Override
	public List<Object> variableKeys() {
		return OOStateUtilities.flatStateKeys(this);
	}

	@Override
	public Object get(Object variableKey) {
		return OOStateUtilities.get(this, variableKey);
	}

	@Override
	public State copy() {
		return new BeesState(agent, map, honey, bees);
	}

	public BeesCell block(int i){
		return this.bees.get(i);
	}

	protected int beeForName(String ob){
		int i = 0;
		for(BeesCell b : this.bees){
			if(b.name().equals(ob)){
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public int numBeesAt(int x, int y) {
		int num = 0;
		for(BeesCell b : this.bees) {
			if(b.x == x && b.y == y) {
				num += 1;
			}
		}
		return num;
	}

	@Override
	public String toString() {
		return OOStateUtilities.ooStateToString(this);
	}

	public void copyBees(){
		this.bees = new ArrayList<BeesCell>(bees);
	}
}

