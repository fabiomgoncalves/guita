package pt.iscte.dcti.umlviewer.network.handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipselabs.jmodeldiagram.model.JModel;
import org.eclipselabs.jmodeldiagram.view.JModelViewer;

import pt.iscte.dcti.umlviewer.service.Service;
import pt.iscte.dcti.umlviewer.service.ServiceHelper;

public class SignalHandler extends AbstractHandler {

	private static final int DEFAULT_PORT = 8081;

	private JModelViewer viewer;

	public SignalHandler() {
		viewer = JModelViewer.getInstance();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		new RecordAgent().start();
		return null;
	}

	private class RecordAgent extends Thread {

		private InetAddress addr;
		private Socket socket;
		private ObjectOutputStream outputStream;
		private ObjectInputStream inputStream;

//		private Map<String, ClassDataTransferObject> data = null;

		private JModel fragment;
		
		public void run() {
			//spamming the "option click" will trigger various threads.
			try {
				addr = InetAddress.getByName(null);
				socket = new Socket(addr, DEFAULT_PORT);
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
				handleConnection();
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

//			if (data != null) {
//				Map<Class<?>, Collection<Method>> fragmentClasses = loadClassesFromData();
//				service.showFragment(fragmentClasses);
//			}
			if(fragment != null) {
				viewer.displayModel(fragment, true);
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