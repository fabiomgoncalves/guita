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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.SWT;
import org.eclipselabs.guita.rtmod.data.Location;
import org.eclipselabs.guita.rtmod.data.Request;

import com.thoughtworks.paranamer.AdaptiveParanamer;


public aspect EditTitle {
	private static final Set<Class<?>> ALLOWED_TYPES = getAllowedTypes();
	private Map<Map<Control, String>, SourceLocation> map = new HashMap<>();	

	private static Set<Class<?>> getAllowedTypes() {
		Set<Class<?>> allowedTypes = new HashSet<Class<?>>();

		allowedTypes.add(Boolean.class);
		allowedTypes.add(Character.class);
		allowedTypes.add(Short.class);
		allowedTypes.add(Integer.class);
		allowedTypes.add(Long.class);
		allowedTypes.add(Float.class);
		allowedTypes.add(Double.class);		
		allowedTypes.add(String.class);

		allowedTypes.add(boolean.class);
		allowedTypes.add(char.class);
		allowedTypes.add(short.class);
		allowedTypes.add(int.class);
		allowedTypes.add(long.class);
		allowedTypes.add(float.class);
		allowedTypes.add(double.class);

		return allowedTypes;
	}
	
	public static boolean isAllowedType(Class<?> _class) {
		return ALLOWED_TYPES.contains(_class);
	}
	
	private boolean isInstanceOf(Class<?> child, Class<?> parent) {
		return parent.isAssignableFrom(child);
	}
	
	after() returning(final Control control) : call(*.new(..)) && within(Window) {	
		Method[] controlMethodArray = control.getClass().getMethods();
		LinkedList<MethodContainer> methodList = new LinkedList<MethodContainer>();
		AdaptiveParanamer paranamer = new AdaptiveParanamer();

		for (Method method : controlMethodArray) {
			String methodName = method.getName();
			if (methodName.startsWith("set")) {
				Class<?>[] methodParameterTypes = method.getParameterTypes();

				boolean allowedMethod = true;

				if (methodParameterTypes.length == 0) {
					allowedMethod = false;
				}

				for (int i = 0, j = methodParameterTypes.length; i < j && allowedMethod; i++) {
					if (!isAllowedType(methodParameterTypes[i])) {
						allowedMethod = false;
					}
				}

				if (allowedMethod) {		
					String[] parameterNames = paranamer.lookupParameterNames(method, false);
					MethodContainer addMethod = new MethodContainer(methodName);

					for (int i = 0, j = methodParameterTypes.length; i < j; i++) {
						String parameterType = methodParameterTypes[i].getName();

						if (parameterType.indexOf(".") != -1) {
							parameterType = parameterType.substring(parameterType.lastIndexOf(".") + 1);
						}

						String parameterName = parameterType;
						if (parameterNames.length > 0) {
							parameterName = parameterNames[i];
						}
						addMethod.addParameter(parameterName, parameterType);
					}

					methodList.add(addMethod);
				}
			}
		}

		Menu contextMenu = new Menu(control);
		for (final MethodContainer method : methodList) {
			MenuItem menuItem = new MenuItem(contextMenu, SWT.NONE);
			menuItem.setText(method.getMethodExpression());		
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					MethodDialog dialog = new MethodDialog(method);					
					Object[] parameters = dialog.open();		
					HashMap<Control, String> slm = new HashMap<>();	
					slm.put(control, method.getName());
					System.out.println();
					try {
						Socket socket = new Socket("127.0.0.1", 7777);
						OutputStream outputstream = (OutputStream) socket.getOutputStream();  
						ObjectOutputStream objectstream = new ObjectOutputStream(outputstream);
						Location location = new Location(map.get(slm), 7777);
						Request request = new Request(location, parameters, method.getName());
						objectstream.writeObject(request);  
						objectstream.close();  
						outputstream.close();  
						socket.close();  
						Class<?>[] _c = new Class<?>[parameters.length];
						for (int i = 0, j = parameters.length; i < j; i++) {
							_c[i] = parameters[i].getClass();
						}
						Method met = control.getClass().getMethod(method.getName(), _c);
						met.invoke(control, parameters);		
					} catch (IOException ee) {
						ee.printStackTrace();
					} catch (Exception eee) {

					}
				}
			});

		}
		control.setMenu(contextMenu);
	}

	after(): call(void *.set*(..)) && within(Window) {		
		if (isInstanceOf(thisJoinPoint.getTarget().getClass(), Control.class)) {
			SourceLocation source = thisJoinPoint.getSourceLocation();
			HashMap<Control, String> slm = new HashMap<>();	
			slm.put((Control) thisJoinPoint.getTarget(), thisJoinPoint.getSignature().getName());
			map.put(slm, source);
		}
	}
}
