package org.eclipselabs.guita.rtmod.aspectj;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.LinkedList;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.reflect.SourceLocation;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.SWT;
import org.eclipselabs.guita.rtmod.data.Location;
import org.eclipselabs.guita.rtmod.data.Request;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.thoughtworks.paranamer.AdaptiveParanamer;

public aspect EditControl {
	private Table <Control, String, SourceLocation> controlMethodsLocation;

	public EditControl() {
		this.controlMethodsLocation = HashBasedTable.create();
	}

	public static boolean isAllowedType(Class<?> clazz) {
		return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class;
	}

	after() returning(final Control control) : call(*.new(..)) && within(Window) {	
		Method[] controlMethodArray = control.getClass().getMethods();
		LinkedList<MethodContainer> methodList = Lists.newLinkedList();
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

				@Override
				public void widgetSelected(SelectionEvent e) {
					MethodDialog dialog = new MethodDialog(method);					
					Object[] parameters = dialog.open();

					try {
						Socket socket = new Socket("127.0.0.1", 7777);
						OutputStream outputStream = socket.getOutputStream();  
						ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);	

						Location location = new Location(controlMethodsLocation.get(control, method.getName()), 7777);
						Request request = new Request(location, parameters, method.getName());						
						objectStream.writeObject(request);  

						objectStream.close();  
						outputStream.close();  
						socket.close();  

						MethodUtils.invokeMethod(control, method.getName(), parameters);
					} catch (Exception exception) {
						System.out.println(method.getName());
						exception.printStackTrace();
					}
				}
			});

		}

		control.setMenu(contextMenu);
	}

	after(): call(void *.set*(..)) && within(Window) && target(Control)  {		
		SourceLocation source = thisJoinPoint.getSourceLocation();
		controlMethodsLocation.put((Control) thisJoinPoint.getTarget(), thisJoinPoint.getSignature().getName(), source);
	}
}
