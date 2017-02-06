import java.awt.Graphics2D;
import java.util.ArrayList;

public class LittleThinker extends Thread{
	private ArrayList<PhysicalObject> objects;
	private PhysicalObject universe = null;
	private boolean calcChargeB = false;
	private boolean calcGravityB = false;
	private boolean calcForceB = false;
	private boolean die = false;
	private Exception err = null;
	private ParseFailedException errParse = null;
	private int begin = -1;
	private int size = -1;
	public LittleThinker(ValueGetter valgetter,ArrayList<PhysicalObject> list,View view,int begin,int size)throws ParseFailedException{
		super();
		if(valgetter == null)
			throw new ParseFailedException("Failed to create new thread");
		this.universe = valgetter.getUniverse();
		this.objects = list;
		this.begin = begin;
		this.size = size;
		start();
	}
	public void calcCharge() throws ParseFailedException{
		if(this.calcChargeB == true)
			throw new ParseFailedException("Duplicated execution");
		this.calcChargeB = true;
		this.interrupt();
	}
	public void calcGravity() throws ParseFailedException{
		if(this.calcGravityB == true)
			throw new ParseFailedException("Duplicated execution");
		this.calcGravityB = true;
		this.interrupt();
	}
	public void calcForce() throws ParseFailedException{
		if(this.calcForceB == true)
			throw new ParseFailedException("Duplicated execution");
		this.calcForceB = true;
		this.interrupt();
	}
	private void calculateCharge()throws ParseFailedException{
		double fineness = universe.getValueOf(PhysicalObject.Fineness);
		double coulombConstant = 1 / ( 4 * Math.PI * universe.getValueOf(PhysicalObject.Permittivity));
		int i,to;
		to = begin + size;
		try{
			for(i = begin;i < to;i++){
				//electrical force
				PhysicalObject obj = objects.get(i);
				double objCharge = obj.getValueOf(PhysicalObject.Charge);
				if(objCharge != 0.0f){
					for(PhysicalObject target : objects){
						double targetCharge = target.getValueOf(PhysicalObject.Charge);
						double f_x = 0;
						double f_y = 0;
						if(target != obj && targetCharge != 0.0f){
							double x_distance = target.getValueOf(PhysicalObject.X) - obj.getValueOf(PhysicalObject.X);
							double y_distance = target.getValueOf(PhysicalObject.Y) - obj.getValueOf(PhysicalObject.Y);
							double distance = Math.sqrt(Math.pow(x_distance, 2.0f) + Math.pow(y_distance, 2.0f));
							double cos = x_distance / distance;
							double sin = y_distance / distance;
							double force 
								= coulombConstant * (targetCharge * objCharge / distance * distance);
							f_x += force * cos; // F*cosx
							f_y += force * sin;// F*sinx
						}
						target.justfyForce(f_x, f_y, fineness);
					}
				}
			}
		}catch (ParseFailedException e) {
			throw e;
		}catch (Exception e) {
			throw new ParseFailedException(e.getMessage());
		}
	}
	private void calculateGravity()throws ParseFailedException{
		double gravity = universe.getValueOf(PhysicalObject.Gravity);
		double gConst = universe.getValueOf(PhysicalObject.GravitationalConstant);
		double fineness = universe.getValueOf(PhysicalObject.Fineness);
		int i,to;
		to = begin + size;
		try{
			for(i = begin;i < to;i++){
				PhysicalObject obj = objects.get(i);
				double objMass = obj.getValueOf(PhysicalObject.Mass);
				if(gravity != 0.0f){
					obj.justfyForce(0, objMass * gravity * -1.0f, fineness);
				}
				for(PhysicalObject target : objects){
					double x_force = 0;
					double y_force = 0;
					if(obj!=target){
						double x_distance = target.getValueOf(PhysicalObject.X) - obj.getValueOf(PhysicalObject.X);
						double y_distance = target.getValueOf(PhysicalObject.Y) - obj.getValueOf(PhysicalObject.Y);
						double distance = Math.sqrt(Math.pow(x_distance, 2.0f) + Math.pow(y_distance, 2.0f));
						double cos = x_distance / distance;
						double sin = y_distance / distance;
						double force = gConst * objMass / distance * distance;
						x_force += force * cos * -1.0f;
						y_force += force * sin * -1.0f;
					}
					target.justfyForce(x_force, y_force, fineness);
				}
			}
		}catch (ParseFailedException e) {
			throw e;
		}catch (Exception e) {
			throw new ParseFailedException(e.getMessage());
		}
	}
	private void calculateForce() throws ParseFailedException{
		int i,to;
		double force_x,force_y,force_dir,force;
		double deltaT = universe.getValueOf(PhysicalObject.Fineness);
		to = begin + size;
		try{
			for(i = begin;i < to;i++){
				PhysicalObject obj = objects.get(i);
				force = obj.getValueOf(PhysicalObject.Force);
				if(force == 0.0f)
					continue;
				force_dir = obj.getValueOf(PhysicalObject.force_dir);
				force_dir = Math.toRadians(force_dir);
				force_x = force * Math.cos(force_dir);
				force_y = force * Math.sin(force_dir);
				obj.justfyForce(force_x, force_y, deltaT);
			}
		}catch (ParseFailedException e) {
			throw e;
		}catch (Exception e) {
			throw new ParseFailedException(e.getMessage());
		}
	}
	public void waitForChargeCalc()throws InterruptedException, ParseFailedException{
		while(calcChargeB){
			if(err != null)
				throw new ParseFailedException(""+err.getMessage());
			if(errParse != null)
				throw errParse;
			Thread.sleep(0);
		}
	}
	public void waitForGravityCalc() throws ParseFailedException, InterruptedException{
		while(calcGravityB){
			if(err != null)
				throw new ParseFailedException(""+err.getMessage());
			if(errParse != null)
				throw errParse;
			Thread.sleep(0);
		}
	}
	public void waitForForceCalc() throws ParseFailedException, InterruptedException{
		while(calcForceB){
			if(err != null)
				throw new ParseFailedException(""+err.getMessage());
			if(errParse != null)
				throw errParse;
			Thread.sleep(0);
		}
	}
	public void kill(){
		die = true;
		this.interrupt();
	}
	@Override
	public void run(){
		while(true){
			if(loop())
				return;
		}
	}
	private boolean calculate(){
		if(die)
			return true;
		try{
			if(calcChargeB){
				calculateCharge();
				calcChargeB = false;
			}
			if(calcGravityB){
				calculateGravity();
				calcGravityB = false;
			}
			if(calcForceB){
				calculateForce();
				calcForceB = false;
			}
			return false;
		}catch (ParseFailedException err) {
			errParse = err;
			return false;
		}
	}
	public boolean loop(){
		try{
			while(true){
				if(Thread.interrupted())
					calculate();
				Thread.sleep(999999999);
			}
		}catch (InterruptedException i) {
			return calculate();
		}catch (Exception e) {
			err = e;
			return true;
		}
	}
}
