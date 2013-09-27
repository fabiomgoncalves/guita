package org.eclipselabs.guita.codetrace.plugin.commands;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipselabs.guita.codetrace.common.Request;
import org.eclipselabs.guita.codetrace.plugin.view.TableWidgetReference;
import org.eclipselabs.guita.codetrace.plugin.view.ViewTable;

public class UnpaintAllCommand extends AbstractHandler{

	public static final int PORT_IN = 8080;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Socket socket = null;
		ObjectOutputStream oos = null;

		ViewTable view = ViewTable.getInstance();

		List<TableWidgetReference> widgets = view.getWidgetsTable();
		
		try {
			socket = new Socket("localhost", PORT_IN);
			oos = new ObjectOutputStream(socket.getOutputStream());

			Iterator<TableWidgetReference> iterator = widgets.iterator();
			while (iterator.hasNext()) {
				TableWidgetReference w = iterator.next();
				Request request = Request.newUnpaintRequest(w.getLocation(), w.getInfo(), w.getName());

				oos.writeObject(request);
			}
			oos.writeObject(null);
			
			view.clearAll();

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

		return null;
	}

}
