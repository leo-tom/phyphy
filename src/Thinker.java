import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class Thinker extends Thread{
	private View view;
	private ArrayList<HashMap<String, String>> list;
	private ArrayList<CalcStateListener> calcstateListenerList = new ArrayList<CalcStateListener>();
	public void addcalcStateListener(CalcStateListener val){calcstateListenerList.add(val);}
	private int viewSize;
	private PhysicalObject universe = new PhysicalObject();
	private boolean die = false;
	private String format = null;
	public void kill(){die = true;}
	
	public double getZoomRate() throws ParseFailedException{return universe.getValueOf(PhysicalObject.Zoom);}

	public Thinker(ArrayList<HashMap<String, String>> list,View view) {
		super();
		this.view = view;
		this.list = list;
		this.viewSize = view.getWidth() > view.getHeight() ? view.getHeight() : view.getWidth();
		view.init();
		try{
			start();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	private boolean isDisplayable(PhysicalObject obj) throws ParseFailedException{
		double zoomRate = universe.getValueOf(PhysicalObject.Zoom);
		int x = (int) (viewSize / 2 + obj.getValueOf(PhysicalObject.X) * zoomRate);
		if(x > viewSize || x < 0){
			return false;
		}
		int y = (int)(viewSize / 2 - obj.getValueOf(PhysicalObject.Y) * zoomRate);
		if(y > viewSize || y < 0){
			return false;
		}
		return true;
	}
	private void deleteOldDot(Graphics2D g,PhysicalObject obj) throws ParseFailedException{
		g.setColor(g.getBackground());
		int size = obj.oldSizeREAL;
		int shape = (int) obj.getValueOf(PhysicalObject.Shape);
		int x = obj.oldXREAL;
		int y = obj.oldYREAL;
		double n_x = obj.getValueOf(PhysicalObject.X);
		double n_y = obj.getValueOf(PhysicalObject.Y);
		double n_size = obj.getValueOf(PhysicalObject.Size);
		double zoomRate = universe.getValueOf(PhysicalObject.Zoom);
		n_size *= zoomRate;
		n_x *= zoomRate;
		n_y *= zoomRate;
		n_x = (viewSize / 2 + n_x)  - (n_size != 1 ? n_size /2 : 0);
		n_y = (viewSize / 2 + n_y)  - (n_size != 1 ? n_size /2 : 0);
		execute:{
			if(Double.isNaN(n_x) || Double.isNaN(n_y))
				break execute; //sometimes those values be NaN. When it became NaN delete any way
			if( ((int)n_x) == x && ((int)n_y) == y && ((int)n_size) == size){
				return;
			}else if(Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(size)){
				return;
			}
		}
		if(shape == PhysicalObject.shapeRect){
			if(size <= 0){
				size = 1;
			}
			g.fillRect(x,y,size,size);
		}else if(shape == PhysicalObject.shapeCircle){
			if(size <= 1){
				size = 2;
			}
			g.fillOval(x,y,size,size);
		}
	}
	private void setObject(Graphics2D g,PhysicalObject obj) throws ParseFailedException{
		double size = obj.getValueOf(PhysicalObject.Size);
		double x = obj.getValueOf(PhysicalObject.X);
		double y = obj.getValueOf(PhysicalObject.Y);
		double zoomRate = universe.getValueOf(PhysicalObject.Zoom);
		int x_at,y_at,size_real;
		int shape = (int) obj.getValueOf(PhysicalObject.Shape);
		if(!isDisplayable(obj)){
			return;
		}
		obj.memolizeOldcordinate();
		size *= zoomRate;
		x *= zoomRate;
		y *= zoomRate;
		x_at = (int) ((viewSize / 2 + x)  - (size != 1 ? size /2 : 0));
		y_at = (int) ((viewSize / 2 - y) - (size != 1 ? size /2 : 0));
		size_real = (int)size;
		if(obj.oldXREAL == x_at && obj.oldYREAL == y_at && obj.oldSizeREAL == size_real){
			if(universe.getValueOf(PhysicalObject.AfterImage) != 0.0f)
				return; //object did not move;
		}
		obj.oldXREAL = x_at;
		obj.oldYREAL = y_at;
		obj.oldSizeREAL = size_real;
		if(x <= 1.0f || x >= -1.0f || y >= -1.0f || y <= 1.0f){
			view.init();
		}
		g.setColor(obj.getColor());
		if(shape == PhysicalObject.shapeRect){
			if(size_real <= 0){
				size_real = 1;
			}
			g.fillRect(x_at,y_at,size_real, size_real);
		}else if(shape == PhysicalObject.shapeCircle){
			if(size_real <= 1){
				size_real = 2;
			}
			g.fillOval(x_at,y_at,size_real, size_real);
		}
	}
	private ArrayList<PhysicalObject> collisionList = new ArrayList<PhysicalObject>();
	private ArrayList<PhysicalObject> checkCollision(PhysicalObject obj,ArrayList<PhysicalObject> objects) throws ParseFailedException{
		if(obj.getValueOf(PhysicalObject.Mass)==0)
			return null;
		int shape = (int) obj.getValueOf(PhysicalObject.Shape);
		collisionList.clear();
		if(shape == PhysicalObject.shapeRect){
			for (PhysicalObject another : objects) {
				if(obj != another && another.getValueOf(PhysicalObject.Size) != 0){
					double distance = obj.getValueOf(PhysicalObject.X) - another.getValueOf(PhysicalObject.X);
					distance = distance < 0 ? distance * -1.0f : distance;
					if(distance - (obj.getValueOf(PhysicalObject.Size)/2+another.getValueOf(PhysicalObject.Size)/2) <= 0.0f){
						//check y too(do this to make things faster
						distance = obj.getValueOf(PhysicalObject.Y) - another.getValueOf(PhysicalObject.Y);
						distance = distance < 0 ? distance * -1.0f : distance;
						if(distance - (obj.getValueOf(PhysicalObject.Size)/2+another.getValueOf(PhysicalObject.Size)/2) <= 0.0f){
							if(collisionList==null) collisionList = new ArrayList<PhysicalObject>();
							collisionList.add(another);
						}
					}
				}
			}
		}else{
			double x_distance;
			double y_distance;
			double distance;
			for (PhysicalObject another : objects) {
				if(obj != another && another.getValueOf(PhysicalObject.Size) != 0){
					x_distance = obj.getValueOf(PhysicalObject.X) - another.getValueOf(PhysicalObject.X);
					y_distance = obj.getValueOf(PhysicalObject.Y) - another.getValueOf(PhysicalObject.Y);
					distance = Math.sqrt(x_distance*x_distance + y_distance * y_distance);
					if(distance - (obj.getValueOf(PhysicalObject.Size)/2+another.getValueOf(PhysicalObject.Size)/2) <= 0.0f){
						if(collisionList==null)
							collisionList = new ArrayList<PhysicalObject>();
						collisionList.add(another);
					}
				}
			}
		}
		if(collisionList.size() <= 0)
			return null;
		return collisionList;
	}
	private void collide(ArrayList<PhysicalObject> collisionList,ValueGetter valGetter) throws ParseFailedException{
		double rate = valGetter.getUniverse().getValueOf(PhysicalObject.LosingEnergyByCollision);
		rate = 1 - rate;
		for (PhysicalObject obj : collisionList) {
			double x = obj.getValueOf(PhysicalObject.velocity_x) * obj.getValueOf(PhysicalObject.Mass);
			double y = obj.getValueOf(PhysicalObject.velocity_y) * obj.getValueOf(PhysicalObject.Mass);
			x *= rate;
			y *= rate;
			obj.momentum_x -= x;
			obj.momentum_y -= y;
			x /= (collisionList.size() - 1);
			y /= (collisionList.size() - 1);
			for (PhysicalObject target : collisionList) {
				if(target != obj){
					target.momentum_x += x;
					target.momentum_y += y;
				}
			}
		}
		double mass;
		for (PhysicalObject obj : collisionList){
			mass = obj.getValueOf(PhysicalObject.Mass);
			obj.setDval(PhysicalObject.velocity_x, obj.getValueOf(PhysicalObject.velocity_x) + obj.momentum_x / mass);
			obj.setDval(PhysicalObject.velocity_y, obj.getValueOf(PhysicalObject.velocity_y) + obj.momentum_y / mass);
			obj.momentum_x = 0;
			obj.momentum_y = 0;
		}
	}
	@Override
	public void run() {
		Graphics2D g = (Graphics2D) view.getGraphics();
		ArrayList<PhysicalObject> objects = new ArrayList<PhysicalObject>();
		ArrayList<PhysicalObject> strings = new ArrayList<PhysicalObject>();
		ArrayList<LittleThinker> thinkers = new ArrayList<LittleThinker>();
		boolean infinite = true;
		ValueGetter valueGetter = new ValueGetter(universe);
		valueGetter.setG(g);
		double fineness;
		double sleep;
		int LazyDraw;
		try{
			for (HashMap<String, String> map : list) {
				PhysicalObject obj = new PhysicalObject(map);
				if(obj.AmIUniverse()){
					HashMap<String, String> uniMap = universe.getMap();
					for (String kee : obj.getMap().keySet()) {
						uniMap.put(kee, obj.getValue(kee));
					}
					universe.compile(valueGetter);
				}else{
					valueGetter.addObject(obj);
					obj.compile(valueGetter);
					if(obj.AmIString())
						strings.add(obj);
					else
						objects.add(obj);
				}
			}
			ArrayList<PhysicalObject> collisionList = null;
			int counter = 0;
			int statusOutputRate = Integer.valueOf(universe.getValue(PhysicalObject.ShowStatus)).intValue();
			format = universe.getValue(PhysicalObject.Format);
			if(universe.getValueOf(PhysicalObject.Fineness) <= 0.0f){
				universe.setValueOf(PhysicalObject.Fineness, "1e-3", valueGetter);
			}
			
			if(statusOutputRate > 0){
				System.out.print("["+universe.getName()+"]");
				for (String key : universe.getMap().keySet()) {
					if(!key.equals(IniParse.sectionNameIdentifer)){
						System.out.print(key+":"+universe.getValue(key)+" ");
					}
				}
				System.out.println("");
			}
			for (PhysicalObject obj : objects) {
				setObject(g, obj);
				if(statusOutputRate > 0 ){
					System.out.print(
							"["+obj.getName()+"]");
					for (String key : obj.getMap().keySet()) {
						if(!key.equals(IniParse.sectionNameIdentifer)){
							System.out.print(key+":"+obj.getValue(key)+" ");
						}
					}
					System.out.println("");
				}
			}
			if(statusOutputRate > 0){
				for (PhysicalObject obj : objects) {
					obj.printMyself(format,0.0f);
				}
			}
			if(universe.getValueOf(PhysicalObject.Time) == 0.0f){
				infinite = true;
			}else{
				infinite = false;
			}
			LazyDraw = (int) universe.getValueOf(PhysicalObject.LazyDraw);
			if(LazyDraw < 0){
				LazyDraw = 0;
			}
			int threadCount = (int)universe.getValueOf(PhysicalObject.Thread);
			if(threadCount > objects.size()){
				threadCount = objects.size();
				System.err.println(threadCount+" threads is more than objects in this universe.");
				System.err.println("Executing it with "+objects.size()+" threads.");
			}
			int objPerThread = (int)(objects.size() / threadCount);
			if(threadCount <= 0){
				throw new ParseFailedException("Invalid thread count.");
			}
			for(int cou = 0;cou<threadCount;cou++){
				if(cou+1 == threadCount)
					thinkers.add(
							new LittleThinker(valueGetter,objects,view,cou*objPerThread,objects.size() - cou*objPerThread));
				else
					thinkers.add(
							new LittleThinker(valueGetter,objects,view,cou*objPerThread,objPerThread));
			}
			FunctionKun t_Function;
			for(//get Function-Kun
				t_Function = universe.getFunctionKun(PhysicalObject.CurrentTime);
				//check time
				infinite || t_Function.Calc() <= universe.getValueOf(PhysicalObject.Time) ;
				//increment current time
				t_Function.setDval(t_Function.Calc() + universe.getValueOf(PhysicalObject.Fineness)))
				{
				if(die){
					g.dispose();
					for(CalcStateListener listener : calcstateListenerList)
						listener.CalcFinished(valueGetter);
					for(LittleThinker thinker : thinkers)
						thinker.kill();
					return;
				}
				for(int i = 0;i<threadCount;i++)
					thinkers.get(i).calcCharge();
				for(int i = 0;i<threadCount;i++)
					thinkers.get(i).calcGravity();
				for(int i = 0;i<threadCount;i++)
					thinkers.get(i).calcForce();
				for(LittleThinker thinker : thinkers){
					thinker.waitForForceCalc();
					thinker.waitForChargeCalc();
					thinker.waitForGravityCalc();
				}
				FunctionKun f_x,f_y;
				double x_tmp;
				double y_tmp;
				double velocity;
				fineness = universe.getValueOf(PhysicalObject.Fineness);
				for (PhysicalObject obj : objects) {
					if(obj.getValueOf(PhysicalObject.Unmoveable) != 0.0f){
						continue;
					}
					f_x = obj.getFunctionKun(PhysicalObject.X);
					x_tmp = f_x.Calc();
					if((velocity = obj.getValueOf(PhysicalObject.velocity_x)) != 0.0f){
						f_x.setDval(x_tmp + velocity * fineness);
					}
					f_y = obj.getFunctionKun(PhysicalObject.Y);
					y_tmp = f_y.Calc();
					if((velocity = obj.getValueOf(PhysicalObject.velocity_y)) != 0.0f){
						f_y.setDval(y_tmp + velocity * fineness);
					}
					if((collisionList = checkCollision(obj, objects)) != null){
						f_x.setDval(x_tmp);
						f_y.setDval(y_tmp);
						collisionList.add(obj);
						collide(collisionList,valueGetter);
						collisionList = null;
					}
					if(statusOutputRate > 0 && counter >= statusOutputRate){
						obj.printMyself(format,t_Function.Calc());
					}
				}
				if(LazyDraw <= 0){
					int val = (int)universe.getValueOf(PhysicalObject.AfterImage);
					boolean delete = val == 0 ? true : false;
					for(PhysicalObject obj : objects){
						if(delete){
							deleteOldDot(g, obj);
						}
						setObject(g, obj);
					}
					LazyDraw = (int) universe.getValueOf(PhysicalObject.LazyDraw);
					if(LazyDraw < 0)
						LazyDraw = 0;
				}else{
					LazyDraw--;
				}
				sleep = universe.getValueOf(PhysicalObject.Sleep);
				if(sleep!=0.0f){
					try{
						if(sleep<1){
							Thread.sleep(0, (int) (sleep * Math.pow(10, 6)));
						}else{
							Thread.sleep((int)sleep);
						}
					}catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
				if(statusOutputRate > 0 && counter >= statusOutputRate){
					counter = 0;
				}
				counter++;
				for(CalcStateListener listener : calcstateListenerList){
					listener.Calced(valueGetter);
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
		}
		g.dispose();
		for(CalcStateListener listener : calcstateListenerList)
			listener.CalcFinished(valueGetter);
		for(LittleThinker thinker : thinkers)
			thinker.kill();
	}
	public double getDistance(PhysicalObject obj1,PhysicalObject obj2) throws ParseFailedException{
		double x_distance = obj2.getValueOf(PhysicalObject.X) - obj1.getValueOf(PhysicalObject.X);
		double y_distance = obj2.getValueOf(PhysicalObject.Y) - obj1.getValueOf(PhysicalObject.Y);
		return Math.sqrt(Math.pow(x_distance, 2.0f) + Math.pow(y_distance, 2.0f));
	}
}
