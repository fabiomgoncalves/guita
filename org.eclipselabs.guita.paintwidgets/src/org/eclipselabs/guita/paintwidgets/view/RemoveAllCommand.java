package org.eclipselabs.guita.paintwidgets.view;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipselabs.guita.request.Request;

public class RemoveAllCommand extends AbstractHandler{
	
	public static final int PORT_IN = 8080;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Socket socket = null;
		ObjectOutputStream oos = null;

		ViewTable view = ViewTable.getInstance();

		List<TableWidgetReference> widgets = view.getWidgetsTable();
		

		Iterator<TableWidgetReference> iterator = widgets.iterator();
		while (iterator.hasNext()) {
			TableWidgetReference w = iterator.next();
			Request request = Request.newUnpaintRequest(w.getLocation(), w.getInfo());

			try {
				socket = new Socket("localhost", PORT_IN);
				oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(request);

				view.removeWidget(w);

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(oos != null){
					try {
						oos.close();
					} catch(IOException e){
					}
				}
				if(socket != null){
					try {
						socket.close();
					} catch(IOException e){
					}
				}
			}
		}

		return null;
	}

}
