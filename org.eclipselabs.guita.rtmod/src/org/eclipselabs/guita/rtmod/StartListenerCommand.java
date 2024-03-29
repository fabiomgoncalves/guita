package org.eclipselabs.guita.rtmod;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipselabs.guita.rtmod.data.Request;
import org.eclipselabs.guita.rtmod.parse.FileParseUtil;
import org.eclipselabs.guita.rtmod.parse.FileVisitor;
import org.eclipselabs.guita.rtmod.parse.FirstPassVisitor;
import org.eclipselabs.guita.rtmod.parse.SecondPassVisitor;

public class StartListenerCommand extends AbstractHandler {

	private ServerSocket serverSocket;
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			serverSocket = new ServerSocket(7777);
		}
		catch (IOException e) {
			System.err.println("Could not listen on port: 7777");
		}

		new RequestListener().start();
		
		return null;
	}
	
	public class RequestListener extends Thread {
		@Override
		public void run() {
			Socket sock;
			while(true) {
				try {
					sock = serverSocket.accept();
					InputStream is = sock.getInputStream();  
					ObjectInputStream ois = new ObjectInputStream(is);  
					Request request = (Request) ois.readObject();  
					handleRequest(request);
					ois.close();
					is.close();
					sock.close();                                   
				}
				catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}                      
		}

		private void handleRequest(Request request) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			FileVisitor visitor = new FileVisitor(request.getLocation().fileName());
			try {
				root.accept(visitor);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}

			FileParseUtil file = null;
			try {
				file = new FileParseUtil(visitor.getICompilationUnit());
			} catch (IOException e) {
				e.printStackTrace();
			}

			CompilationUnit unit = file.getCompilationUnit();			
			FirstPassVisitor firstVisitor = new FirstPassVisitor(unit, request.getParameters(), request.getLocation().lineNumber());
			file.parse(firstVisitor);		
			
			SecondPassVisitor secondVisitor = new SecondPassVisitor(unit, firstVisitor.getVariablesMap(), firstVisitor.getReplaceVariablesMap());
			file.parse(secondVisitor);	
			
			Document document = null;
			try {
				document = new Document(visitor.getICompilationUnit().getSource());
				firstVisitor.applyChanges(document, visitor.getICompilationUnit());
				secondVisitor.applyChanges(document, visitor.getICompilationUnit());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
