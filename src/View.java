import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Color;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;

public class View extends JPanel{

	/**
	 * Create the panel.
	 */
	private View me = this;
	private ArrayList<HashMap<String, String>> list = null;
	private Thinker thinker;
	
	public View() {
		super();
	}
	public void init(){
		Dimension d = me.getSize();
		double val;
		if(d.getWidth() > d.getHeight()){
			val = d.height;
		}else{
			val = d.width;
		}
		d.setSize(val, val);
		Graphics2D g = (Graphics2D) me.getGraphics();
		g.setColor(Color.black);
		try{
			g.drawLine((int)d.getWidth() / 2, 0, (int)d.getWidth() /2 , (int)d.getHeight());
			g.drawLine(0, (int)d.getHeight() / 2, (int) d.getWidth(), (int)d.getHeight() / 2);
			g.dispose();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public Thinker calculate(ArrayList<HashMap<String, String>> list){
		this.list = list;
		thinker = new Thinker(list,me);
		return thinker;
	}
	@Override
	public void paint(Graphics g){
		super.paint(g);
		
	}
}
