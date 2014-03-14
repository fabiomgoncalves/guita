package org.eclipselabs.guita.rtmod.aspectj;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.reflect.SourceLocation;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipselabs.guita.rtmod.data.Location;
import org.eclipselabs.guita.rtmod.data.Request;


public aspect EditTitle {
	private Map<Button, SourceLocation> map = new HashMap<>();

	after() returning(final Button b) : call(Button.new(..)) {
		b.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {	
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					Socket socket = new Socket("127.0.0.1", 7777);
					OutputStream outputstream = (OutputStream) socket.getOutputStream();  
					ObjectOutputStream objectstream = new ObjectOutputStream(outputstream);
					b.setText("Novo Texto");
					System.out.println(map.get(b));
					Location location = new Location(map.get(b), 7777);
					Request request = new Request(location, "Novo Texto", "setText");
					objectstream.writeObject(request);  
					objectstream.close();  
					outputstream.close();  
					socket.close();  
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		});
	}

	after(String s) : call(void Button.setText(String)) && args(s) && !within(EditTitle) {
		SourceLocation source = thisJoinPoint.getSourceLocation();
		map.put((Button) thisJoinPoint.getTarget(), source);
	}
}
