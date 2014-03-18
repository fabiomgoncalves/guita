package teste;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

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

	after() returning(final Control control) : call(*.new(..)) && !within(EditTitle) {	

		LinkedList<MethodAbstract> methodAbstracts = new LinkedList<MethodAbstract>();
		Method[] controlMethodArray = control.getClass().getMethods();
		AdaptiveParanamer paranamer = new AdaptiveParanamer();

		for (Method method : controlMethodArray) {
			String methodName = method.getName();
			if (methodName.startsWith("set")) {					
				boolean yes = true;
				if (method.getParameterTypes().length == 0) {
					yes = false;
				} else {
					for (Class<?> type : method.getParameterTypes()) {
						if (!isWrapperType(type)) {
							System.out.println(methodName);
							System.out.println(type.toString());
							yes = false;
							break;
						}
					}
				}

				if (yes) {
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
		}

		Menu contextMenu = new Menu(control);
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
						Location location = new Location(map.get(control), 7777);
						Request request = new Request(location, result, methodAbstract.getMethodName());
						objectstream.writeObject(request);  
						objectstream.close();  
						outputstream.close();  
						socket.close();  
						//por a classe com linkedlist de objectos ja com os tipos certos
						//converter lista pra obkect[]
						//invocar o metodo
						Method met = control.getClass().getMethod(methodAbstract.getMethodName(), String.class);
						met.invoke(control, result.get(0));		
					} catch (IOException ee) {
						ee.printStackTrace();
					} catch (Exception eee) {

					}
				}
			});
		}
		control.setMenu(contextMenu);

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

	private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

	public static boolean isWrapperType(Class<?> clazz)
	{
		return WRAPPER_TYPES.contains(clazz);
	}

	private static Set<Class<?>> getWrapperTypes()
	{
		Set<Class<?>> ret = new HashSet<Class<?>>();
		ret.add(Boolean.class);
		ret.add(Character.class);
		ret.add(Short.class);
		ret.add(Integer.class);
		ret.add(Long.class);
		ret.add(Float.class);
		ret.add(Double.class);		
		ret.add(String.class);
		ret.add(boolean.class);
		ret.add(char.class);
		ret.add(short.class);
		ret.add(int.class);
		ret.add(long.class);
		ret.add(float.class);
		ret.add(double.class);
		return ret;
	}
}
