package org.eclipselabs.guita.paintwidgets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipselabs.guita.paintwidgets.view.ViewTable;
import org.eclipselabs.guita.paintwidgets.view.TableWidgetReference;
import org.eclipselabs.guita.request.Request;
import org.eclipselabs.guita.request.Request.Color;
import org.eclipselabs.variableanalyzer.service.VariableResolver;


public class PaintCommand extends AbstractHandler{

	public static final int PORT1 = 8080;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if(selection instanceof TextSelection) {
			TextSelection s = (TextSelection) selection;
			int line =  s.getStartLine() + 1;
			String text = s.getText();

			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			IEditorInput input = editor.getEditorInput();
			IPersistable pers = input.getPersistable();

			if(pers instanceof FileEditorInput) {
				FileEditorInput fileInput = (FileEditorInput) pers;
				String type = VariableResolver.resolve(fileInput.getFile(), text, line);
				String loc = fileInput.getPath().lastSegment() + ":" + line;

				String color = null;
				try {
					color = event.getCommand().getName();
				} catch (NotDefinedException e) {
					e.printStackTrace();
				}
				
				handleVar(editor, type, text, loc, color);
			}
		}
		return null;
	}
	
	public void handleVar(IEditorPart editor, String type, String text, String loc, String color){
		if(type != null) {
			String message = "\"" + text + "\"" + "\n" + type + "\"" + "\n" + loc;

			if(type.indexOf("org.eclipse.swt.widgets.") > -1) {
				Request request = Request.newPaintRequest(loc, mapColor(color));

				if(ViewTable.getInstance().alreadyPainted(text, loc)){
					MessageBox messageDialog = new MessageBox(editor.getSite().getShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					messageDialog.setText("Paint");
					messageDialog.setMessage("This widget is already painted. Do you wish to change its color?");
					if(messageDialog.open() == SWT.OK){
						sendRequest(text, type, loc, color, request);
					}
				}else {
					sendRequest(text, type, loc, color, request);
				}
			}
			else {
				MessageDialog.open(MessageDialog.INFORMATION, editor.getSite().getShell(), "Variable", message, SWT.NONE);
			}
		}
		else {
			MessageDialog.open(MessageDialog.ERROR, editor.getSite().getShell(), "Variable", "Variable Not found", SWT.NONE);
		}
	}


	public Color mapColor(String color){
		if(color.equals("Red")){
			return Color.RED;
		}else if(color.equals("Blue")){
			return Color.BLUE;
		}else if(color.equals("Green")){
			return Color.GREEN;
		}else if(color.equals("Yellow")){
			return Color.YELLOW;
		}else{
			return Color.PINK;
		}
	}

	public void sendRequest(String text, String type, String loc, String color, Request request){
		Socket socket = null;
		Socket socket2 = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		
		try {
			socket = new Socket("localhost", PORT1);
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(request);
			
			socket2 = ViewTable.getInstance().getServerSocket().accept();
			ois = new ObjectInputStream(socket2.getInputStream());
			int number = (Integer) ois.readObject();

			TableWidgetReference newWidget = new TableWidgetReference(text, type, loc, color, number);
			ViewTable.getInstance().addWidget(newWidget);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(oos != null){
				try {
					oos.close(); } catch(IOException e){}
			}
			if(ois != null){
				try {
					ois.close(); } catch(IOException e){}
			}
			if(socket != null){
				try {
					socket.close(); } catch(IOException e){}
			}
			if(socket2 != null){
				try {
					socket2.close(); } catch(IOException e){}
			}
		}
	}

	@Override
	public boolean isEnabled() {
		ISelection selection =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		return selection instanceof TextSelection;
	}

}