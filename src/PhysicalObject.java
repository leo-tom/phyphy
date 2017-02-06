import java.awt.Color;
import java.lang.reflect.Field;
import java.util.HashMap;

public class PhysicalObject {
	private Color color;
	private double old_x = Double.NaN;
	private double old_y = Double.NaN;
	private double old_size = Double.NaN;
	public int oldXREAL;
	public int oldYREAL;
	public int oldSizeREAL;
	private String name = null;
	private Boolean AmIString = null;
	
	public double momentum_x;
	public double momentum_y;
	
	public final static String velocity = "v";
	public final static String velocity_x = "v_x";
	public final static String velocity_y = "v_y";
	public final static String force_x = "$f_x";
	public final static String force_y = "$f_y";
	public final static String Force = "f";
	public final static String direction = "v_direction";
	public final static String Mass = "mass";
	public final static String X = "x";
	public final static String Y = "y";
	public final static String Size = "size";
	public final static String Universe = "universe";
	public final static String Colour = "color";
	public final static String Shape = "shape";
	public final static String Charge = "charge";
	public final static String force_dir = "f_direction";
	public final static String Unmoveable ="unmoveable";
	public final static String String = "string";
	public final static String Length ="length";
	public final static String ConnectTo = "connect_to";
	public final static String ConnectFrom = "connect_from";

	public final static String Gravity = "gravity"; //gravity
	public final static String Fineness = "fineness"; //finess. value of smallest delta t
	public final static String Delta_t = "delta_t";
	public final static String DeltaT = "deltaT";
	public final static String Time = "timeLimit"; // how long
	public final static String Zoom = "zoom"; //zoom rate
	public final static String ShowStatus = "status"; //How often display status(1 is most frequent. 0 to disable) 
	public final static String Format = "format"; //printf format for status
	public final static String Permittivity = "permittivity"; //for Coulomb's law
	public final static String AfterImage = "afterImage"; //show after image or not
	public final static String Sleep = "sleep"; //Time that program sleep per delta t. (in millisecond)
	public final static String CurrentTime = "t";
	public final static String LazyDraw = "LazyDraw";
	public final static String GravitationalConstant = "GravitationalConstant";
	public final static String MemoryUpdateRate = "MemoryUpdateRate";
	//public final static String CoefficientOfRestitution = "CoefficientOfRestitution";
	public final static String LosingEnergyByCollision = "LosingEnergyByCollision";
	public final static String Thread = "thread";
	
	public final static int shapeRect = 1;
	public final static int shapeCircle = 0;
	
	private HashMap<String, String> maiMap;
	private HashMap<String,FunctionKun> functionMap = new HashMap<String,FunctionKun>();
	
	/**
	 *  Makes PhysicalObject which represent universe
	 */
	public PhysicalObject(){
		super();
		maiMap = new HashMap<String,String>();
		maiMap.put(IniParse.sectionNameIdentifer, Universe);
		maiMap.put(Gravity, "9.8");
		maiMap.put(Fineness, "delta_t");
		maiMap.put(Time, "10");
		maiMap.put(Zoom, "1");
		maiMap.put(ShowStatus, "0");
		maiMap.put(Permittivity, "8.854e-12");
		maiMap.put(AfterImage, "1");
		maiMap.put(Sleep, "0");
		maiMap.put(LazyDraw, "0");
		maiMap.put(GravitationalConstant, "6.6740831e-14");
		maiMap.put(MemoryUpdateRate, "0.5");
		maiMap.put(CurrentTime, "0");
		maiMap.put(LosingEnergyByCollision, "0.0f");
		maiMap.put(Delta_t, "deltaT");
		maiMap.put(DeltaT, "1e-3");
		maiMap.put(Thread, "1");
	}
	/**
	 * 
	 * @param Just make raw object
	 */
	private PhysicalObject(int n){
		super();
	}
	public PhysicalObject(HashMap<String, String> map){
		maiMap = map;
		String value;
		value = map.get(velocity);
		if(value != null){
			double v = Double.valueOf(value).doubleValue();
			value = map.get(direction);
			if(value != null){
				double dir = Double.valueOf(value).doubleValue();
				double x_v = v * Math.cos(Math.toRadians(dir));
				double y_v = v * Math.sin(Math.toRadians(dir));
				functionMap.put(PhysicalObject.velocity_x, new FunctionKun(x_v));
				functionMap.put(PhysicalObject.velocity_y, new FunctionKun(y_v));
			}
		}
	}
	public static PhysicalObject initPhysicalObject(){
		return new PhysicalObject(0);
	}
	public HashMap<String, FunctionKun> getFunctionKunMap(){
		return functionMap;
	}
	public void compile(ValueGetter getter){
		FunctionKun f;
		for(String key : maiMap.keySet()){
			try {
				if((f = functionMap.get(key))!=null){
					f.reCompile(maiMap.get(key));
					continue;
				}else{
					f = new FunctionKun(maiMap.get(key), getter,this);
				}
				functionMap.put(key, f);
			} catch (ParseFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public String getValue(String val){
		return maiMap.get(val);
	}
	public String getName(){
		if(name == null){
			name = maiMap.get(IniParse.sectionNameIdentifer);
		}
		return name;
	}
	public HashMap<String, String> getMap(){
		return maiMap;
	}
	public boolean AmIUniverse(){
		return getName().equals("universe");
	}
	public boolean AmIString() throws ParseFailedException{
		if(AmIString == null)
			AmIString = new Boolean(getValueOf(PhysicalObject.String)!=0.0f);
		return AmIString.booleanValue();
	}
	public Color getColor() {
		if(color == null){
			String cName = getStringValueOf(Colour);
			if(cName != null){
				try {
					Field f = Color.class.getField(cName);
					color = (Color) f.get(null);
				} catch (Exception e) {	
					color = Color.BLACK;
				}
			}else{
				color = Color.BLACK;
			}
		}
		return color;
	}
	public void printMyself(String format,double time){
		System.out.println(getName());
		System.out.println(maiMap.toString());
		System.out.println(functionMap.toString());
	}
	synchronized public void justfyForce(double x,double y,double deltaTime) throws ParseFailedException{
		double mass = getValueOf(Mass);
		FunctionKun fKun = null;
		if(mass == 0)
			return;
		if(x != 0.0f){
			fKun = getFunctionKun(velocity_x);
			fKun.setDval(fKun.Calc() + x * deltaTime / mass);
		}
		if(y != 0.0f){
			fKun = getFunctionKun(velocity_y);
			fKun.setDval(fKun.Calc() + y * deltaTime / mass);
		}
	}
	public void memolizeOldcordinate() throws ParseFailedException{
		old_x = getValueOf(X);
		old_y = getValueOf(Y);
		old_size = getValueOf(PhysicalObject.Size);
	}
	public double getOldX(){
		return old_x;
	}
	public double getOldY(){
		return old_y;
	}
	public double getOldSize(){
		return old_size;
	}
	public double getValueOf(String kee) throws ParseFailedException{
		FunctionKun f = functionMap.get(kee);
		if(f == null || !f.compileSucceed()){
			if(f != null)
				functionMap.remove(kee);
			if(!maiMap.containsKey(kee)){
				maiMap.put(kee, "0");
				functionMap.put(kee, new FunctionKun(0.0f));
			}
			return 0.0f;
		}
		return f.Calc();
	}
	public FunctionKun getFunctionKun(String name){
		FunctionKun f = functionMap.get(name);
		if(f == null){
			f = new FunctionKun(0.0f);
			functionMap.put(name, f);
		}
		return f;
	}
	public String getStringValueOf(String kee){
		String val = maiMap.get(kee);
		if(val == null){
			val = "0";
		}else{
			return val;
		}
		maiMap.put(kee, val);
		return val;
	}
	public void setValueOf(String name,String value,ValueGetter getter) throws ParseFailedException{
		maiMap.put(name, value);
		functionMap.put(name, new FunctionKun(value,getter,this));
	}
	public void setDval(String name,double val){
		getFunctionKun(name).setDval(val);
	}
}
