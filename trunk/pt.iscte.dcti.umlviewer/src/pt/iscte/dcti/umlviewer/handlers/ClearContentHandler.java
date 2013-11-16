package pt.iscte.dcti.umlviewer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ClearContentHandler extends AbstractHandler {

	private static final boolean SHOW_REPORTS = true;
	
	public ClearContentHandler() {
		if(SHOW_REPORTS) {
			System.out.println("CLEAR CONTENT READY");
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(SHOW_REPORTS) {
			System.out.println("CLEAR CONTENT PRESSED");
		}
		return null;
	}

}
