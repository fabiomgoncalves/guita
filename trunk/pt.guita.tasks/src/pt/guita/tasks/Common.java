package pt.guita.tasks;

import java.util.HashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class Common {

	public static void openMethod(final IMethod method, final int line) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IMarker marker = method.getResource().createMarker(IMarker.TEXT);
					HashMap<String, Object> map = new HashMap<String, Object>();
//					map.put(IMarker.CHAR_START, method.getSourceRange().getOffset());				
					map.put(IMarker.LINE_NUMBER, line-1);
					marker.setAttributes(map);
					IDE.openEditor(page, marker); 
					marker.delete();
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
