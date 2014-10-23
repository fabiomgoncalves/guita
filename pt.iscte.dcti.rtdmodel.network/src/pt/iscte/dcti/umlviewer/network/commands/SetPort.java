package pt.iscte.dcti.umlviewer.network.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.resolver.DisabledInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pt.iscte.dcti.rtdmodel.network.Activator;

public class SetPort extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = Display.getDefault().getActiveShell();
		int port = Activator.getInstance().getSocketPort();
		SetPortDialog dialog = new SetPortDialog(shell, port);
		dialog.create();
		if(dialog.open() == Window.OK) {
			Activator.getInstance().setSocketPort(dialog.getPortNumber());
		}
		return null;
	}


}
