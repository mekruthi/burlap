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
public class BeesCell implements ObjectInstance {

	public int x;
	public int y;

	protected String className;
	protected String name;

	private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y);

	public static BeesCell bee(String name, int x, int y){
		return new BeesCell(x, y, CLASS_BEE, name);
	}

	public static BeesCell honey(String name, int x, int y){
		return new BeesCell(x, y, CLASS_HONEY, name);
	}

	public BeesCell() {
	}

	public BeesCell(String className, String name) {
		this.className = className;
		this.name = name;
	}

	public BeesCell(int x, int y, String className, String name) {
		this.x = x;
		this.y = y;
		this.className = className;
		this.name = name;
	}

	public void setXY(int x, int y){
		this.x = x;
		this.y = y;
	}

	@Override
	public String className() {
		return this.className;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public ObjectInstance copyWithName(String objectName) {
		return new BeesCell(x, y, className, objectName);
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
		throw new RuntimeException("Unknown key " + variableKey);
	}

	@Override
	public BeesCell copy() {
		return new BeesCell(x, y, className, name);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return OOStateUtilities.objectInstanceToString(this);
	}
}
