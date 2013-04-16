package pt.iscte.dcti.umlviewer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class Command1 extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("TOGGLE INCREMENTAL");
		return null;
	}

}
