package org.eclipselabs.guita.uitracer.view;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipselabs.guita.uitrace.common.Defaults;
import org.eclipselabs.guita.uitrace.common.Trace;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;




public class Activator implements BundleActivator {

	private ServerSocket serverSocket = null;
	private IWorkbenchPage page;

	@Override
	public void start(BundleContext context) throws Exception {	
		try {
			serverSocket = new ServerSocket(Defaults.ECLIPSE_PORT);
		} 
		catch (IOException e) {
			System.err.println("Could not listen on port: 4444");
		}

		new Listener().start();

	}

	public class Listener extends Thread {
		@Override
		public void run() {
			Socket sock;
			while(true) {
				try {
					sock = serverSocket.accept();

					InputStream is = sock.getInputStream();  
					ObjectInputStream ois = new ObjectInputStream(is);  
					Trace trace = (Trace) ois.readObject();  
					ois.close();
					is.close();
					sock.close();

					sock = serverSocket.accept();
					is = sock.getInputStream();
					ImageData[] data = new ImageLoader().load(is);
					Image image = new Image(Display.getDefault(), data[0]);
					is.close();
					sock.close();
					
					handleTraceRequest(trace, image);					
				}
				catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}			
		}

		private void handleTraceRequest(final Trace trace, final Image image) {
			System.out.println(trace.first() + " - " + trace.size());
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

					try {			
						page.showView(TraceView.ID);			
					} 
					catch (PartInitException e) {
						e.printStackTrace();
					}
					TraceView.getInstance().load(trace, image);
				}
			});
			
		}		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
