import java.awt.Color;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

public class FunctionKun{
	private ArrayList<FunctionKunEle> arrayList = null;
	private ValueGetter valGetter = null;
	private String str = null;
	private ParseFailedException err = null;
	private PhysicalObject MaiObj = null;
	private boolean HaveICompiled = false;
	private double dval = Double.NaN;
	
	private static final String KEY_COS      = "cos";
	private static final String KEY_SIN      = "sin";
	private static final String KEY_TAN      = "tan";
	private static final String KEY_LOG      = "log";
	private static final String KEY_ASIN     = "asin";
	private static final String KEY_ACOS     = "acos";
	private static final String KEY_ATAN     = "atan";
	private static final String KEY_EXP      = "exp";
	private static final String KEY_ROUND    = "round";
	private static final String KEY_TORADIAN = "toradians";
	private static final String KEY_TODEGREE = "todigrees";
	private static final String KEY_RANDOM   = "random";
	private static final String KEY_ABSOLUTE = "absolute";
	public FunctionKun(String str,ValueGetter val,PhysicalObject MaiObj) throws ParseFailedException{
		if(val == null){
			throw new ParseFailedException("null was given");
		}
		valGetter = val;
		this.str = str;
		this.MaiObj = MaiObj;
		HaveICompiled = true;
		startParse();
	}
	public FunctionKun(double val){
		arrayList = null;
		this.str = null;
		dval = val;
		HaveICompiled = false;
	}
	public boolean compileSucceed(){
		if(err == null)
			return true;
		return false;
	}
	public ParseFailedException getException(){
		return err;
	}
	public void setDval(double val){
		if(Double.isNaN(val)){
			System.err.println("Nan is given");
			Thread.dumpStack();
			return;
		}
		err = null;
		this.str = null;
		dval = val;
		HaveICompiled = false;
	}
	public void reCompile(String val){
		this.str = val;
		HaveICompiled = true;
		startParse();
	}
	private void startParse(){
		StringReader reader = null;
		if(str==null){
			setDval(0.0f);
			return;
		}
		try {
			reader = new StringReader(str);
			arrayList = parse(new PushbackReader(reader, 1));
			if(arrayList.size()<=0){
				setDval(0.0f);
			}
			double dval = Optimize(arrayList);
			if(!Double.isNaN(dval)){
				setDval(dval);
			}
		} catch (ParseFailedException e) {
			err = e;
			e.printStackTrace();
		}
		reader.close();
	}
	private double Optimize(ArrayList<FunctionKunEle> eleList) throws ParseFailedException{
		for(FunctionKunEle ele : eleList){
			switch (ele.getType()) {
			case FunctionKunEle.VARIABLE:
			case FunctionKunEle.RANDOM:
					return Double.NaN;
			case FunctionKunEle.BRACKET:
				if(Double.isNaN(Optimize(ele.getArray()))){
					return Double.NaN;
				}
			}
		}
		return Calc();
	}
	@Override
	public String toString(){
		if(str == null){
			str = "" + dval;
		}
		return str;
	}
	private ArrayList<FunctionKunEle> parse(PushbackReader stream) throws ParseFailedException{
		int c;
		ArrayList<FunctionKunEle> list = new ArrayList<FunctionKunEle>();
		end:{
		try {
			while((c = stream.read())!=-1){
				switch (c) {
				case '+':
					list.add(new FunctionKunEle(FunctionKunEle.PLUS));
					break;
				case '-':
					list.add(new FunctionKunEle(FunctionKunEle.MINUS));
					break;
				case '*':
					list.add(new FunctionKunEle(FunctionKunEle.MULTI));
					break;
				case '/':
					list.add(new FunctionKunEle(FunctionKunEle.DIVIDE));
					break;
				case '(':
					list.add(new FunctionKunEle(parse(stream)));
					break;
				case ')':
					break end;
				case '^':
					list.add(new FunctionKunEle(FunctionKunEle.POW));
					break;
				case '!':
					list.add(new FunctionKunEle(FunctionKunEle.FACTRIAL));
					break;
				case ' ':
					break;
				default:
					StringBuilder builder;
					if(Character.isAlphabetic(c) || c == '_'){
						builder = new StringBuilder();
						builder.append((char)c);
						String str = null;
						while(Character.isLetterOrDigit(c = stream.read()) || c == '_' ){
							builder.append((char)c);
						}
						if(c == '.'){
							// This is name of object
							String ObjName = builder.toString().trim();
							PhysicalObject Obj;
							if(ObjName.equals("this")){
								Obj = MaiObj;
							}else{
								Obj = valGetter.getObject(ObjName);
							}
							builder.delete(0, builder.length());
							if((c = stream.read()) == ' '){
								throw new ParseFailedException("There is space next to \'.\'.");
							}
							stream.unread(c);
							while(Character.isLetterOrDigit(c = stream.read()) || c == '_' ){
								builder.append((char)c);
							}
							stream.unread(c);
							list.add(new FunctionKunEle(Obj,builder.toString()));
							break;
						}else{
							stream.unread(c);
						}
						str = builder.toString();
						switch (str) {
						case FunctionKun.KEY_ACOS:
							list.add(new FunctionKunEle(FunctionKunEle.ACOS));
							break;
						case FunctionKun.KEY_ASIN:
							list.add(new FunctionKunEle(FunctionKunEle.ASIN));
							break;
						case FunctionKun.KEY_ATAN:
							list.add(new FunctionKunEle(FunctionKunEle.ATAN));
							break;
						case FunctionKun.KEY_COS:
							list.add(new FunctionKunEle(FunctionKunEle.COS));
							break;
						case FunctionKun.KEY_EXP:
							list.add(new FunctionKunEle(FunctionKunEle.EXP));
							break;
						case FunctionKun.KEY_LOG:
							list.add(new FunctionKunEle(FunctionKunEle.LOG));
							break;
						case FunctionKun.KEY_RANDOM:
							list.add(new FunctionKunEle(FunctionKunEle.RANDOM));
							break;
						case FunctionKun.KEY_ROUND:
							list.add(new FunctionKunEle(FunctionKunEle.ROUND));
							break;
						case FunctionKun.KEY_SIN:
							list.add(new FunctionKunEle(FunctionKunEle.SIN));
							break;
						case FunctionKun.KEY_TAN:
							list.add(new FunctionKunEle(FunctionKunEle.TAN));
							break;
						case FunctionKun.KEY_TODEGREE:
							list.add(new FunctionKunEle(FunctionKunEle.TODEGREES));
							break;
						case FunctionKun.KEY_TORADIAN:
							list.add(new FunctionKunEle(FunctionKunEle.TORADIAN));
							break;
						case FunctionKun.KEY_ABSOLUTE:
							list.add(new FunctionKunEle(FunctionKunEle.ABSOLUTE));
							break;
						default:
							//variable
							if(MaiObj.getFunctionKunMap().containsKey(str)){
								list.add(new FunctionKunEle(MaiObj, str));
							}else{
								list.add(new FunctionKunEle(valGetter.getUniverse(),str));
							}
							break;
						}
					}else if(Character.isDigit(c)){
						builder = new StringBuilder();
						builder.append((char)c);
						while( Character.isDigit((c = stream.read())) || c == '.' || c == '+' || c == 'e'){
							if(c == 'e'){
								builder.append((char)c);
								c = stream.read();
								if(c == '-')
									builder.append((char)c);
								else
									stream.unread(c);
							}else{
								builder.append((char)c);
							}
						}
						stream.unread(c);
						list.add(new FunctionKunEle(Double.valueOf(builder.toString()).doubleValue()));
					}
					break;
				}
			}
		} catch (IOException e) {
			throw new ParseFailedException("IOException was thrown.["+e.getMessage()+"]");
		}catch (Exception e) {
			throw new ParseFailedException(e.getMessage());
		}
		}
		return list;
	}
	
	synchronized public double Calc() throws ParseFailedException{
		if(err != null){
			throw err;
		}
		if(!HaveICompiled){
			return dval;
		}
		return Calculate(arrayList);
	}
	private static double Calculate(ArrayList<FunctionKunEle> list) throws ParseFailedException{
		double value = 0;
		double tmp;
		long intTmp;
		FunctionKunEle ele,eleLeft,eleRight;
		long Counter  = list.get(0).getCounter();
		int i;
		Iterator<FunctionKunEle> ite = list.iterator();
		while(ite.hasNext()){
			ele = ite.next();
			if(ele.getType() == FunctionKunEle.BRACKET){
				ele.setValue(Calculate(ele.getArray()));
			}
		}
		try{
			for(i = 0; i < list.size();i++){
				ele = list.get(i);
				if(ele.getCounter() > Counter){
					continue;
				}
				switch (ele.getType()) {
				case FunctionKunEle.SIN:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.sin(eleRight.getValue()));
					break;
				case FunctionKunEle.COS:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.cos(eleRight.getValue()));
					break;
				case FunctionKunEle.TAN:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.tan(eleRight.getValue()));
					break;
				case FunctionKunEle.LOG:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.log(eleRight.getValue()));
					break;
				case FunctionKunEle.ASIN:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.asin(eleRight.getValue()));
					break;
				case FunctionKunEle.ACOS:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.acos(eleRight.getValue()));
					break;
				case FunctionKunEle.ATAN:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.atan(eleRight.getValue()));
					break;
				case FunctionKunEle.TODEGREES:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.toDegrees(eleRight.getValue()));
					break;
				case FunctionKunEle.TORADIAN:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.toRadians(eleRight.getValue()));
					break;
				case FunctionKunEle.RANDOM:
					ele.setValue(Math.random());
					break;
				case FunctionKunEle.EXP:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.exp(eleRight.getValue()));
					break;
				case FunctionKunEle.FACTRIAL:
					eleLeft = getLeftEle(list, i, Counter);
					tmp = eleLeft.getValue();
					value = 1.0f;
					intTmp = Math.round(tmp);
					while(tmp>0){
						value *= intTmp;
						intTmp--;
					}
					ele.setValue(value);
					break;
				case FunctionKunEle.ABSOLUTE:
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(Math.abs(eleRight.getValue()));
					break;
				}
			}
			for(i = 0; i < list.size();i++){
				ele = list.get(i);
				if(ele.getCounter() > Counter){
					continue;
				}
				switch (ele.getType()) {
				case FunctionKunEle.DIVIDE:
					eleLeft  = getLeftEle(list, i, Counter);
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(eleLeft.getValue() / eleRight.getValue());
					break;
				case FunctionKunEle.MULTI:
					eleLeft  = getLeftEle(list, i, Counter);
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(eleLeft.getValue() * eleRight.getValue());
					break;
				}
			}
			
			for(i = 0; i < list.size();i++){
				ele = list.get(i);
				if(ele.getCounter() > Counter){
					continue;
				}
				switch (ele.getType()) {
				case FunctionKunEle.PLUS:
					eleLeft  = getLeftEle(list, i, Counter);
					eleRight = getRightEle(list, i, Counter);
					ele.setValue(eleLeft.getValue() + eleRight.getValue());
					break;
				case FunctionKunEle.MINUS:
					eleLeft  = getLeftEle(list, i, Counter);
					eleRight = getRightEle(list, i, Counter);
					if(eleLeft == null || !eleLeft.canGetValue()){
						ele.setValue(eleRight.getValue() * -1.0f);
						if(eleLeft != null)
							eleLeft.decrementCounter(Counter);
					}else{
						ele.setValue(eleLeft.getValue() - eleRight.getValue());
					}
					break;
				}
			}
		}catch (IndexOutOfBoundsException e) {
			throw new ParseFailedException(e.getMessage());
		}catch(NullPointerException e){
			e.printStackTrace();
			throw new ParseFailedException("Invalid syntax?" + e.getMessage());
		}
		ite = list.iterator();
		value = 1.0f;
		while(ite.hasNext()){
			ele = ite.next();
			if(ele.getCounter() == Counter){
				ele.incrementCounter(Counter);
				value *= ele.getValue();
			}
			ele.resetValue();
		}
		return value;
	}
	private static FunctionKunEle getLeftEle(ArrayList<FunctionKunEle> list,int index,long Counter) throws ParseFailedException{
		FunctionKunEle ele = null;
		index--;
		while(index >= 0){
			ele = list.get(index);
			if(ele.getCounter() == Counter){
				ele.incrementCounter(Counter);
				break;
			}
			index--;
		}
		return ele;
	}
	private static FunctionKunEle getRightEle(ArrayList<FunctionKunEle> list,int index,long Counter) throws ParseFailedException{
		FunctionKunEle ele = null;
		int size = list.size();
		index++;
		while(index < size){
			ele = list.get(index);
			if(ele.getCounter() == Counter){
				ele.incrementCounter(Counter);
				break;
			}
			index++;
		}
		return ele;
	}
}
