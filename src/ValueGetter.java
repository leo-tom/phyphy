import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Set;

public class ValueGetter {
	private HashMap<String, PhysicalObject> map = new HashMap<String,PhysicalObject>();
	private PhysicalObject universe;
	private Graphics2D g = null;
	public ValueGetter(PhysicalObject universe){
		this.universe = universe;
	}
	public PhysicalObject getUniverse(){
		return universe;
	}
	public Graphics2D getG() {
		return g;
	}
	public void setG(Graphics2D g) {
		if(g == null)
			throw new NullPointerException("Null pointer is given to value getter.");
		this.g = g;
	}
	synchronized public PhysicalObject getObject(String name) {
		PhysicalObject obj = map.get(name);
		if(obj == null){
			obj = PhysicalObject.initPhysicalObject();
			map.put(name, obj);
			return obj;
		}
		return map.get(name);
	}
	synchronized public void addObject(PhysicalObject obj){
		PhysicalObject old = map.get(obj.getName());
		if(old!=null){
			HashMap<String, String> map = old.getMap();
			HashMap<String, String> newMap = obj.getMap();
			Set<String> set = obj.getMap().keySet();
			for(String key : set){
				map.put(key, newMap.get(key));
			}
			old.compile(this);
			return;
		}
		map.put(obj.getName(), obj);
	}
	synchronized public PhysicalObject findFunctionKun(String val){
		PhysicalObject obj = null;
		if(universe.getFunctionKunMap().containsKey(val)){
			return universe;
		}
		Set<String> list = map.keySet();
		for(String kee : list){
			obj = map.get(kee);
			if(obj.getFunctionKunMap().containsKey(val))
				break;
		}
		return obj;
	}
	
}
