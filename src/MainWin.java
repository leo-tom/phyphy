import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class MainWin {

	private JFrame frame;
	private JPanel valuep;
	private View view;
	private IniParse ini;
	private JPanel panel;
	private JLabel label;
	private JButton btnCalculate;
	private Thinker thinking = null;
	private JButton btnClear;
	
	private final static String newLine = System.lineSeparator();
	private final static String default_value = "[Object A]"+newLine+
			"v=20"+newLine+
			"#in degree"+newLine+
			"v_direction=45"+newLine+
			"color=green"+newLine+
			"x=0"+newLine+
			"y=0"+newLine+
			"#size=0 means object is mass point"+newLine+
			"size=1"+newLine+
			"mass=10"+newLine+
			"charge=0"+newLine+
			"[universe]"+newLine+
			"timeLimit=8"+newLine+
			"zoom=2"+newLine+
			"delta_t=1e-3"+newLine+
			"gravity=9.8"+newLine+
			"status=0"+newLine+
			"format=%.3f"+newLine+
			"permittivity=8.854e-12"+newLine+
			"afterImage=1"+newLine+
			"sleep=0"+newLine+
			"t=0"+newLine+
			"LazyDraw=10"+newLine
			;

	private CalcStateListener Listener = new CalcStateListener() {
		long LastTime = 0;
		double Rate = -1;
		Runtime environment;
		@Override
		public void Calced(ValueGetter valueGetter) {
			try {
				long time = System.currentTimeMillis();
				if(Rate<0){
					Rate = valueGetter.getUniverse().getValueOf(PhysicalObject.MemoryUpdateRate);
					environment = Runtime.getRuntime();
					UpdateMemoryUsage(environment.totalMemory() - environment.freeMemory());
					LastTime = time;
				}
				if((time - LastTime) * 1e-3 >= Rate){
					UpdateMemoryUsage(environment.totalMemory() - environment.freeMemory());
					LastTime = time;
				}
				UpdateCurrentTime(valueGetter.getUniverse().getValueOf(PhysicalObject.CurrentTime));
				UpdateCurrentState();
			} catch (ParseFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void CalcFinished(ValueGetter valueGetter) {
			// TODO Auto-generated method stub
			UpdateCalculatingInfo(false);
		}
	};
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWin window = new MainWin();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWin() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent eve) {
				//view.init() cannot be executed before window is show.
				//Thats why initializing with event
				try{
					view.init();
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		});
		frame.setBounds(150, 150, 550, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		frame.getContentPane().add(splitPane);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane.setLeftComponent(splitPane_1);
		view = new View();
		view.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if(thinking!=null){
					UpdateCalculatingInfo(true);
					UpdateCurrentState();
					thinking.kill();
					thinking = view.calculate(ini.getValue());
					thinking.addcalcStateListener(Listener);
				}
			}
		});
		view.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Dimension d = view.getSize();
				double val;
				double rate = 1;
				if(d.getWidth() > d.getHeight()){
					val = d.height;
				}else{
					val = d.width;
				}
				if(thinking!=null){
					try {
						rate = thinking.getZoomRate();
					} catch (ParseFailedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				val /= 2;
				UpdateCurrentCordinate( (e.getX() - val)/rate, ((e.getY() - val)*-1 )/rate );
				UpdateCurrentState();
			}
		});
		view.setMinimumSize(new Dimension(300, 300));
		splitPane_1.setLeftComponent(view);
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane_1.setRightComponent(scrollPane);
		
		valuep = new JPanel();
		scrollPane.setViewportView(valuep);
		valuep.setLayout(new GridLayout(0, 1, 0, 0));
		
		panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new GridLayout(1, 0, 0, 0));
		
		label = new JLabel("");
		panel.add(label);
		
		btnCalculate = new JButton("Calculate");
		btnCalculate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				UpdateCalculatingInfo(true);
				UpdateCurrentState();
				if(thinking!=null){
					thinking.kill();
				}
				thinking = view.calculate(ini.getValue());
				thinking.addcalcStateListener(Listener);
			}
		});
		
		btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(thinking!=null){
					thinking.kill();
				}
				UpdateCalculatingInfo(false);
				UpdateCurrentState();
				view.updateUI();
			}
		});
		panel.add(btnClear);
		panel.add(btnCalculate);
		
		mkEverythingNew();
	}

	private void mkEverythingNew(){
		ini = new IniParse();
		ini.setText(default_value);
		ini.getValue();
		valuep.add(ini);
	}
	private String mouse_x = "?";
	private String mouse_y = "?";
	private String calculating = "waiting";
	private double current_time = 0.0f;
	private double usedMemory = 0.0f;
	private int unitIndex = 0;
	private static final String[] MemoryUnits = new String[]{
		"B","KB","MB","GB","TB"
	};
	String space = " ";
	private void UpdateCurrentCordinate(double x,double y){
		mouse_x = String.format("%.2f", x);
		mouse_y = String.format("%.2f", y);
	}
	private void UpdateCalculatingInfo(boolean val){
		calculating = (val) ? "calculating" : "waiting";
	}
	private void UpdateCurrentTime(double val){
		current_time = val;
	}
	private void UpdateMemoryUsage(double val){
		unitIndex = 0;
		while(val > 1024){
			val /= 1024;
			unitIndex++;
			if(unitIndex >= MemoryUnits.length){
				unitIndex--;
				val *= 1024;
				break;
			}
		}
		usedMemory =val;
	}
	private void UpdateCurrentState(){
		
		if(current_time > 60*60){
			long lval = (long)current_time;
			long h = TimeUnit.SECONDS.toHours(lval);
			long m = Math.round(TimeUnit.SECONDS.toMinutes(lval) - TimeUnit.HOURS.toMinutes(h));
			double s = current_time - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m);
			if(s < 0.0f)
				s = 0.0f;
			label.setText("x:"+mouse_x+space
					+"y:"+mouse_y+space
					+calculating+space
					+h+"h"+m+"m"+String.format("%.3f sec", s)+space
					+String.format("%.2f", usedMemory)+MemoryUnits[unitIndex]);
		}else{
			label.setText("x:"+mouse_x+space
					+"y:"+mouse_y+space
					+calculating+space
					+String.format("%.3f", current_time)+space
					+String.format("%.2f", usedMemory)+MemoryUnits[unitIndex]);
		}
		
		label.updateUI();
	}
}
