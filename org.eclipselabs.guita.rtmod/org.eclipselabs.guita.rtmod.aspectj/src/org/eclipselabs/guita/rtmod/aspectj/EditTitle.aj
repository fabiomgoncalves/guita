package teste;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.aspectj.lang.reflect.SourceLocation;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipselabs.guita.rtmod.data.Location;
import org.eclipselabs.guita.rtmod.data.Request;

import com.thoughtworks.paranamer.AdaptiveParanamer;


public aspect EditTitle {
	private Map<Control, SourceLocation> map = new HashMap<>();	
	private Shell shell = null;

	after() returning(final Control b) : call(*.new(..)) {	
		if (isInstanceOf(b.getClass(), Shell.class)) {
			shell = (Shell) b;
		}

		LinkedList<MethodAbstract> methodAbstracts = new LinkedList<MethodAbstract>();
		Method[] controlMethodArray = b.getClass().getMethods();
		AdaptiveParanamer paranamer = new AdaptiveParanamer();

		for (Method method : controlMethodArray) {
			String methodName = method.getName();
			if (methodName.startsWith("set")) {				
				LinkedList<String> parameterTypesList = new LinkedList<String>();
				Class<?>[] methodParameterTypes = method.getParameterTypes();
				for (Class<?> parameterType : methodParameterTypes) {
					String parameterTypeName = parameterType.getName();

					if (parameterTypeName.indexOf(".") != -1) {
						parameterTypeName = parameterTypeName.substring(parameterTypeName.lastIndexOf(".") + 1);
					}
					parameterTypesList.add(parameterTypeName);
				}

				LinkedList<String> parameterNamesList = new LinkedList<String>();
				String[] parameterNamesArray = paranamer.lookupParameterNames(method, false);
				for (String parameterName : parameterNamesArray) {
					parameterNamesList.add(parameterName);
				}

				methodAbstracts.add(new MethodAbstract(methodName, parameterTypesList, parameterNamesList));
			}
		}

		Menu contextMenu = new Menu(b);
		for (final MethodAbstract methodAbstract : methodAbstracts) {
			MenuItem menuItem = new MenuItem(contextMenu, SWT.NONE);
			menuItem.setText(methodAbstract.getMethodName() + "(" + methodAbstract.getFormatedParameters() + ")");
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					MethodParametersDialog dialog = new MethodParametersDialog(methodAbstract);
					LinkedList<String> result = dialog.open();
					try {
						Socket socket = new Socket("127.0.0.1", 7777);
						OutputStream outputstream = (OutputStream) socket.getOutputStream();  
						ObjectOutputStream objectstream = new ObjectOutputStream(outputstream);
						Location location = new Location(map.get(b), 7777);
						Request request = new Request(location, result, methodAbstract.getMethodName());
						objectstream.writeObject(request);  
						objectstream.close();  
						outputstream.close();  
						socket.close();  
						//por a classe com linkedlist de objectos ja com os tipos certos
						//converter lista pra obkect[]
						//invocar o metodo
						Method met = b.getClass().getMethod(methodAbstract.getMethodName(), String.class);
						met.invoke(b, result.get(0));		
					} catch (IOException ee) {
						ee.printStackTrace();
					} catch (Exception eee) {
						
					}
				}
			});
		}
		b.setMenu(contextMenu);

//		b.addMouseListener(new MouseListener() {
//			@Override
//			public void mouseDoubleClick(MouseEvent arg0) {
//			}
//
//			@Override
//			public void mouseDown(MouseEvent arg0) {	
//			}
//
//			@Override
//			public void mouseUp(MouseEvent arg0) {
//				try {
//					Socket socket = new Socket("127.0.0.1", 7777);
//					OutputStream outputstream = (OutputStream) socket.getOutputStream();  
//					ObjectOutputStream objectstream = new ObjectOutputStream(outputstream);
//					//b.setText("Novo Texto");
//					System.out.println(map.get(b));
//					Location location = new Location(map.get(b), 7777);
//					Request request = new Request(location, "Novo Texto", "setText");
//					objectstream.writeObject(request);  
//					objectstream.close();  
//					outputstream.close();  
//					socket.close();  
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}			
//		});
	}
	//
	//	after(String s) : call(void Button.set*(*)) && args(s) && !within(EditTitle) {
	//		SourceLocation source = thisJoinPoint.getSourceLocation();
	//		map.put((Button) thisJoinPoint.getTarget(), source);
	//	}

	after(String s) : call(void *.set*(String)) && args(s) && !within(EditTitle) {		
		if (isInstanceOf(thisJoinPoint.getTarget().getClass(), Control.class)) {
			SourceLocation source = thisJoinPoint.getSourceLocation();
			map.put((Control) thisJoinPoint.getTarget(), source);
		}
	}

	private boolean isInstanceOf(Class<?> child, Class<?> parent) {
		return parent.isAssignableFrom(child);
	}
}
