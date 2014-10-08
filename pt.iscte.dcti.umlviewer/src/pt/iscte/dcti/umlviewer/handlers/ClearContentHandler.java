package pt.iscte.dcti.umlviewer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import pt.iscte.dcti.umlviewer.service.Service;
import pt.iscte.dcti.umlviewer.service.ServiceHelper;

public class ClearContentHandler extends AbstractHandler {

	private static final boolean SHOW_REPORTS = true;

	private Service service;

	public ClearContentHandler() {
		service = ServiceHelper.getService();

		if(SHOW_REPORTS) {
			System.out.println("CLEAR CONTENT READY");
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(SHOW_REPORTS) {
			System.out.println("CLEAR CONTENT PRESSED");
		}

		service.clear();

		return null;
	}

}