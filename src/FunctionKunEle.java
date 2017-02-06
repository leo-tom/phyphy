import java.util.ArrayList;
import java.util.Iterator;

public class FunctionKunEle {
	private int type = 0;
	private long Counter = 0;
	private double value = Double.NaN;
	private double constValue;
	private ArrayList<FunctionKunEle> array = null;
	private PhysicalObject physicalObj = null;
	private String strBuff = null;
	
	public FunctionKunEle(int type){
		this.type = type;
	}
	public FunctionKunEle(ArrayList<FunctionKunEle> list) throws ParseFailedException{
		if(list == null)
			throw new ParseFailedException("Null ptr");
		array = list;
		type = FunctionKunEle.BRACKET;
	}
	public FunctionKunEle(PhysicalObject obj,String variableName) throws ParseFailedException{
		if(variableName == null || obj == null)
			throw new ParseFailedException("Null ptr");
		if(variableName.length() <= 0){
			throw new ParseFailedException("Invalid variable is being initialized.");
		}
		this.physicalObj = obj;
		type = FunctionKunEle.VARIABLE;
		strBuff = variableName;
	}
	public FunctionKunEle(double val){
		type = NUMBER;
		this.value = val;
		constValue = val;
	}
	public ArrayList<FunctionKunEle> getArray() throws ParseFailedException{
		if(array == null){
			throw new ParseFailedException("Trying to get array even though it does not have one.");
		}
		return array;
	}
	public long getCounter(){
		return Counter;
	}
	public void incrementCounter(long oldVal)throws ParseFailedException{
		if(Counter != oldVal){
			throw new ParseFailedException("Invalid calculation was executed.");
		}
		Counter++;
	}
	public void decrementCounter(long oldVal)throws ParseFailedException{
		if(Counter - 1 != oldVal){
			throw new ParseFailedException("Invalid calculation was executed");
		}
		Counter--;
	}
	public double getValue() throws ParseFailedException{
		if(type==NUMBER){
			return value;
		}else if(type == VARIABLE){
			return physicalObj.getValueOf(strBuff);
		}else if(value != Double.NaN){
			return value;
		}
		throw new ParseFailedException("Invalid calculation.[Trying to get double from "+String.format("%x", type) +"]");
	}
	public void setValue(double val) throws ParseFailedException{
		this.value = val;
	}
	public void resetValue(){
		if(type == NUMBER){
			value = constValue;
			return;
		}
		value = Double.NaN;
	}
	@Override
	public Object clone(){
		FunctionKunEle ele = new FunctionKunEle(type);
		ele.Counter = Counter;
		ele.value = value;
		ele.array = array;
		return ele;
	}
	public int getType(){
		return this.type;
	}
	public boolean canGetValue(){
		if(type == NUMBER || type == VARIABLE || !Double.isNaN(value)){
			return true;
		}
		return false;
	}
	public static final int NUMBER   = 1 << 0;
	public static final int VARIABLE = 1 << 1;
	public static final int MULTI    = 1 << 2;
	public static final int PLUS     = 1 << 3;
	public static final int MINUS    = 1 << 4;
	public static final int DIVIDE   = 1 << 5;
	public static final int POW      = 1 << 6;
	public static final int FACTRIAL = 1 << 7;
	public static final int BRACKET  = 1 << 8;
	public static final int SIN      = 1 << 9;
	public static final int COS      = 1 << 21;
	public static final int TAN      = 1 << 10;
	public static final int LOG      = 1 << 11;
	public static final int ASIN     = 1 << 12;
	public static final int ACOS     = 1 << 13;
	public static final int ATAN     = 1 << 14;
	public static final int EXP      = 1 << 15;
	public static final int ABSOLUTE = 1 << 16;
	public static final int ROUND    = 1 << 17;
	public static final int TORADIAN = 1 << 18;
	public static final int TODEGREES= 1 << 19;
	public static final int RANDOM   = 1 << 20;
}
