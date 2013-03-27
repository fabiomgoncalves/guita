package org.eclipselabs.guita.paintwidgets;

import java.io.IOException;
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
import org.eclipselabs.variableanalyzer.service.VariableResolver;


public class PaintCommand extends AbstractHandler{

	public static final int PORT_IN = 8080;
	Socket socket = null;
	ObjectOutputStream oos = null;


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String color = "";

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

				try {
					color = event.getCommand().getName();
				} catch (NotDefinedException e) {
					e.printStackTrace();
				}
				
				WidgetRequest request = new WidgetRequest(loc, color);

				if(type != null) {
					String message = "\"" + text + "\"" + "\n" + type + "\"" + "\n" + loc;

					if(type.indexOf("org.eclipse.swt.widgets.") > -1) {
						if(ViewTable.getInstance().paintedWidget(text, type, loc, color)){
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
		}
		return null;
	}

	public void sendRequest(String text, String type, String loc, String color, WidgetRequest request){
		try {
			socket = new Socket("localhost", PORT_IN);
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(request);

			ViewTable.getInstance().addWidget(text, type, loc, color);

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


	@Override
	public boolean isEnabled() {
		ISelection selection =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		return selection instanceof TextSelection;
	}

}