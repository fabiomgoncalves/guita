package org.eclipselabs.guita.codetrace.plugin.commands;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipselabs.guita.codetrace.common.Request;
import org.eclipselabs.guita.codetrace.plugin.view.TableWidgetReference;
import org.eclipselabs.guita.codetrace.plugin.view.ViewTable;

public class UnpaintCommand extends AbstractHandler{

	private static final int PORT1 = 8080;

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Socket socket = null;
		ObjectOutputStream oos = null;

		ViewTable view = ViewTable.getInstance();
		IStructuredSelection sel = view.getSelection();

		if (sel != null) {
			Iterator<TableWidgetReference> iterator = sel.iterator();
			while (iterator.hasNext()) {
				TableWidgetReference w = iterator.next();
				Request request = Request.newUnpaintRequest(w.getLocation(), w.getInfo(), w.getName());

				try {
					socket = new Socket("localhost", PORT1);
					oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(request);
					oos.writeObject(null);

					view.removeWidget(w);

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					view.removeWidget(w);
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