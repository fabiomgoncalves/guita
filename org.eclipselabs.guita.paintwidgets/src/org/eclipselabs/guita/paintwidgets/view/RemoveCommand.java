package org.eclipselabs.guita.paintwidgets.view;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;

public class RemoveCommand extends AbstractHandler{

	public static final int PORT_IN = 8080;

	private String color = "";

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Socket socket = null;
		ObjectOutputStream oos = null;

		ViewTable view = ViewTable.getInstance();
		IStructuredSelection sel = view.getSelection();

		if (sel != null) {
			Iterator<WidgetReference> iterator = sel.iterator();
			while (iterator.hasNext()) {
				WidgetReference w = iterator.next();

				try {
					socket = new Socket("localhost", PORT_IN);
					oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(w.getLocation());
					oos.writeObject(color);

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
		}
		return null;
	}

}