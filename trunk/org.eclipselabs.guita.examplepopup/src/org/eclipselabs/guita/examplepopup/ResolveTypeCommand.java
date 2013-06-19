package org.eclipselabs.guita.examplepopup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipselabs.guita.variableanalyzer.service.TypeFilter;
import org.eclipselabs.guita.variableanalyzer.service.VariableInfo;
import org.eclipselabs.guita.variableanalyzer.service.VariableResolver;




public class ResolveTypeCommand extends AbstractHandler {

	private static class SWTControlFilter implements TypeFilter {

		private Map<String, Boolean> cache = new HashMap<String,Boolean>();
		
		@Override
		public boolean include(String type) {
			if(cache.containsKey(type)) {
				return cache.get(type);
			}
			else {
				try {
					Class<?> clazz = Class.forName("org.eclipse.swt.widgets." + type);
					boolean include = Control.class.isAssignableFrom(clazz);
					cache.put(type, include);
					return include;
				}
				catch(ClassNotFoundException e) {
					return false;
				}				
			}
		}
	}
	
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
				VariableInfo info = VariableResolver.resolve(fileInput.getFile(), text, line, new SWTControlFilter());
				if(info != null) {
					String message = "\"" + text + "\"" + "\n" + info + fileInput.getPath() + "\n" + line + "\n" + offset;
					MessageDialog.open(MessageDialog.INFORMATION, editor.getSite().getShell(), "Variable", message, SWT.NONE);
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
