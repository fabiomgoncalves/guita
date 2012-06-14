package pt.guita.tasks;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContextManager;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class ClearContext implements IViewActionDelegate {
	private IViewPart view;

	@Override
	public void run(IAction action) {
		Activator.getInstance().clearContext();
		IInteractionContextManager contextManager = ContextCore.getContextManager();
		//		try {
		//			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_ONE, null);
		//		} catch (CoreException e) {
		//			e.printStackTrace();
		//		}
		String id = contextManager.getActiveContext().getHandleIdentifier();
		if(id != null) {
			contextManager.deactivateContext(id);
			contextManager.activateContext(id);
		}
		//		view.getSite().getSelectionProvider().setSelection(Activator.getInstance());

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void init(IViewPart view) {
		this.view = view;
	}

}
