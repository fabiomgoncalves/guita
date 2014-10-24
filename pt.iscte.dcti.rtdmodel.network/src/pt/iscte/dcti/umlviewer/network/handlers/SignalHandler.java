package pt.iscte.dcti.umlviewer.network.handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipselabs.jmodeldiagram.JModelDiagram;
import org.eclipselabs.jmodeldiagram.model.JModel;

import pt.iscte.dcti.rtdmodel.network.Activator;

public class SignalHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		new RecordAgent().start();
		return null;
	}
 
	private class RecordAgent extends Thread {

		private InetAddress addr;
		private Socket socket;
		private ObjectOutputStream outputStream;
		private ObjectInputStream inputStream;

		private JModel fragment;

		public void run() {
			final int port = Activator.getInstance().getSocketPort(); 
			try {
				addr = InetAddress.getByName(null);
				socket = new Socket(addr, port);
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
				handleConnection();
			}
			catch(ConnectException e) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Connection Error", 
								"Connection refused on port " + port);
						
					}
				});

			}
			catch (UnknownHostException e) { e.printStackTrace(); } 
			catch (IOException e) { e.printStackTrace(); } 
			finally {
				if(inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
				if(outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e) { e.printStackTrace(); }
				}
			}

			if(fragment != null) {
				JModelDiagram.displayIncremental(fragment);
			}

		}

		private void handleConnection() {
			try {
				//the first message sent by aspect is the current status on recording.
				//both this class and aspect need to synchronize their status.
				Integer status = (Integer)inputStream.readObject();

				if(status.intValue() == 1) {
					outputStream.writeObject(new Integer(0));
					//					data = (Map<String, ClassDataTransferObject>)inputStream.readObject(); //#1
					fragment = (JModel) inputStream.readObject();
					System.out.println("RECEIVED\n" + fragment);
				}
				else if(status.intValue() == 0) {
					outputStream.writeObject(new Integer(1));
					status = (Integer)inputStream.readObject(); //The report, is recording or not.
				}
				else {
					outputStream.writeObject(new Integer(-1)); //end session
					inputStream.readObject(); //consume ack

					//ending session also resets the recording on aspect???
					//this ensures that server will not become permanently recording.
				}
			} 
			catch (ClassNotFoundException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); } 
		}
	}

}