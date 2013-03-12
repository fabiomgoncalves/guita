package org.eclipselabs.guita.paintwidgets;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipselabs.variableanalyzer.service.VariableResolver;


public class PaintCommand extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if(selection instanceof TextSelection) {
			TextSelection s = (TextSelection) selection;
			int line =  s.getStartLine() + 1;
			int offset = s.getOffset();
			String text = s.getText();

			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			
			IEditorInput input = editor.getEditorInput();
			IPersistable pers = input.getPersistable();
			
			if(pers instanceof FileEditorInput) {
				FileEditorInput fileInput = (FileEditorInput) pers;
				String type = VariableResolver.resolve(fileInput.getFile(), text, line);
				
				String loc = fileInput.getPath().lastSegment() + ":" + line;
				
				if(type != null) {
					String message = "\"" + text + "\"" + "\n" + type + "\n" + fileInput.getPath() + "\n" + line + "\n" + offset;
					MessageDialog.open(MessageDialog.INFORMATION, editor.getSite().getShell(), "Paint", message + "\n" + loc, SWT.NONE);
					try {
						Socket socket = new Socket("localhost", 5658);
						ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream());
						stream.writeObject(loc);
						
						stream.reset();
						stream.close();
						socket.close();

					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else {
					MessageDialog.open(MessageDialog.ERROR, editor.getSite().getShell(), "Variable", "Not found", SWT.NONE);
				}
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		ISelection selection =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		return selection instanceof TextSelection;
	}

}