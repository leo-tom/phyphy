import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;

public class IniParse extends JTextArea {

	public static final String sectionNameIdentifer = "$sectionName";
	private IniParse me = this;
	/**
	 * Create the panel.
	 */
	public IniParse() {
		super();
		JPopupMenu menu = new JPopupMenu();
		JMenuItem item = new JMenuItem("OpenFile");
		item.addActionListener(new ActionListener() {
			JFileChooser chooser = null;
			String newLine = System.lineSeparator();
			@Override
			public void actionPerformed(ActionEvent eve) {
				// TODO Auto-generated method stub
				if(chooser == null)
					chooser = new JFileChooser();
				try{
					if(chooser.showOpenDialog(me) == JFileChooser.APPROVE_OPTION){
						BufferedReader reader = new BufferedReader(new FileReader(chooser.getSelectedFile()));
						StringBuilder builder = new StringBuilder();
						String str;
						while((str = reader.readLine())!=null){
							builder.append(str);
							builder.append(newLine);
						}
						me.setText(builder.toString());
					}
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
			}
		});
		menu.add(item);
		item = new JMenuItem("Execute awk");
		item.addActionListener(new ActionListener() {
			AwkInput dialog = null;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(dialog == null){
					dialog = new AwkInput();
					dialog.addExecutelistener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							try{
								ExecuteAwk(dialog.getText());
							}catch (Exception err) {
								JOptionPane.showMessageDialog(null, err.getMessage());
								err.printStackTrace();
							}
						}
					});
				}else{
					dialog.setVisible(true);
				}
			}
		});
		menu.add(item);
		setComponentPopupMenu(menu);
	}
	private boolean osChecked = false;
	private void ExecuteAwk(String str) throws IOException, InterruptedException{
		if(!osChecked){
			if(!System.getProperty("os.name").toUpperCase().contains("LINUX")){
				JOptionPane.showMessageDialog(this, "This function cannot be used with "+System.getProperty("os.name"));
			}else{
				osChecked = true;
			}
		}
		ProcessBuilder builder = new ProcessBuilder();
		File inputFile = new File("/tmp/" + str.hashCode());
		builder.command(new String[]{
			"awk","-f",inputFile.getAbsolutePath()
		});
		FileWriter writer = new FileWriter(inputFile);
		writer.write(str);
		writer.close();
		Process process = builder.start();
		InputStream st =  process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(st));
		BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		process.getOutputStream().close(); //to kill process
		String line,newLine;
		newLine = System.lineSeparator();
		StringBuilder strbuilder = new StringBuilder();
		while((line = reader.readLine())!=null || process.isAlive()){
			if(line!=null){
				strbuilder.append(line);
				strbuilder.append(newLine);
			}else{
				process.waitFor(1000,TimeUnit.NANOSECONDS);
			}
		}
		if(process.exitValue() != 0){
			strbuilder.delete(0, strbuilder.length());
			while((line = errReader.readLine())!=null){
				strbuilder.append(line);
				strbuilder.append(newLine);
			}
			JOptionPane.showMessageDialog(this, strbuilder.toString());
		}else if(strbuilder.length() > 0){
			setText(strbuilder.toString());
		}
		reader.close();
		errReader.close();
		inputFile.delete();
	}
	
	public  ArrayList<HashMap<String, String>> getValue(){
		InputStream input = new ByteArrayInputStream(this.getText().getBytes());
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		try{
			Ini ini = new Ini(input);
			Set<String> sections = ini.keySet();
			for(String sectionName : sections){
				HashMap<String, String> map = new HashMap<String,String>();
				Section section = ini.get(sectionName);
				map.put(sectionNameIdentifer, sectionName);
				for(String valName : section.keySet()){
					map.put(valName, section.get(valName));
				}
				list.add(map);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

}
