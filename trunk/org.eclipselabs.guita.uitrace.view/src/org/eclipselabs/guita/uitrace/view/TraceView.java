package org.eclipselabs.guita.uitrace.view;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.eclipse.ui.part.ViewPart;
import org.eclipselabs.guita.uitrace.common.Defaults;
import org.eclipselabs.guita.uitrace.common.Trace;



public class TraceView extends ViewPart {
	public static final String ID = "org.eclipselabs.guita.uitrace.view";
	private static TraceView instance;
	private ServerSocket serverSocket;
	private RequestHandler requestHandler;

	private static class StopThread implements Serializable {
		private static final long serialVersionUID = 1L;
	}
	
	private static final StopThread STOP_THREAD = new StopThread();

	private CTabFolder folder;

	public TraceView() {
		instance = this;
		try {
			serverSocket = new ServerSocket(Defaults.ECLIPSE_PORT);
			System.out.println("GUI Trace View: listening on port: " + Defaults.ECLIPSE_PORT);
		} 
		catch (IOException e) {
			System.err.println("Could not listen on port: " + Defaults.ECLIPSE_PORT);
		}

		requestHandler = new RequestHandler();
		requestHandler.start();
	}

	public static TraceView getInstance() {
		return instance;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		folder = new CTabFolder(parent, SWT.BORDER);
		folder.setSimple(false);
		folder.setUnselectedImageVisible(true);
		folder.setUnselectedCloseVisible(false);
		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((TraceItem) folder.getItem(folder.getSelectionIndex())).navigateToSelection();
			}
		});

	}

	@Override
	public void setFocus() {

	}

	public void load(final Trace trace, final Image image) {
		TraceItem existing = locateTab(trace);

		if(existing == null) {
			existing = new TraceItem(folder, trace, image);					
		}
		else {
			existing.reload(trace, image);
		}

		folder.setSelection(existing);
		existing.navigate(trace.first());
	}

	private TraceItem locateTab(final Trace trace) {
		TraceItem existing = null;
		CTabItem[] items = folder.getItems();

		for(int i = 0; i < items.length && existing == null; i++) {
			TraceItem titem = (TraceItem) items[i];
			if(titem.hasTrace(trace)) { 				
				existing = titem;
			}
		}
		return existing;
	}


	public void removeAll() {
		for(CTabItem tab : folder.getItems())
			tab.dispose();
	}

	@Override
	public void dispose() {
		super.dispose();
		try {
			Socket clientSocket = new Socket("localhost", Defaults.ECLIPSE_PORT);	
			OutputStream os = clientSocket.getOutputStream();  
			ObjectOutputStream oos = new ObjectOutputStream(os);  
			oos.writeObject(STOP_THREAD);
			oos.close();
			os.close();
			clientSocket.close();
			serverSocket.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	private class RequestHandler extends Thread {
		@Override
		public void run() {
			Socket sock;
			while(true) {
				try {
					sock = serverSocket.accept();

					InputStream is = sock.getInputStream();  
					ObjectInputStream ois = new ObjectInputStream(is); 
					Object obj = ois.readObject();
					Trace trace = null;
					if(!(obj instanceof StopThread))
						trace = (Trace) obj;  
				
					ois.close();
					is.close();
					sock.close();

					if(trace == null) {
						System.out.println("thread stop");
						return;
					}
					final String traceMessage = trace.getMessage();
					
					if(!trace.isNormalTrace()) {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								MessageBox message = new MessageBox(getSite().getShell(), SWT.ICON_ERROR);
								message.setText("No trace available");
								message.setMessage(traceMessage);
								message.open();
							}
						});
					}
					else {
						sock = serverSocket.accept();
						is = sock.getInputStream();
						ImageData[] data = new ImageLoader().load(is);
						Image image = new Image(Display.getDefault(), data[0]);
						is.close();
						sock.close();
						handleTraceRequest(trace, image);
					}

				}
				catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}			
		}

		private void handleTraceRequest(final Trace trace, final Image image) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {			
						page.showView(ID);			
					} 
					catch (PartInitException e) {
						e.printStackTrace();
					}
					load(trace, image);
				}
			});

		}		
	}
}
