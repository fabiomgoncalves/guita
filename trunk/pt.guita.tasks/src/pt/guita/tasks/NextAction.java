package pt.guita.tasks;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class NextAction implements IViewActionDelegate {
	private IViewPart view;
	
	@Override
	public void run(IAction action) {
		Recorder recorder = Activator.getInstance().getRecorder();
		recorder.next();		
		IMethod method = (IMethod) recorder.getFirstElement();
		if(method != null) {			
			Common.openMethod(method, recorder.getElementLine());
//			view.getSite().getSelectionProvider().setSelection(activator);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			Activator.getInstance().getRecorder().updateSelection(obj);
		}
	}

	@Override
	public void init(IViewPart view) {
		this.view = view;
	}

}
