package pt.iscte.dcti.rtdmodel.network;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipselabs.jmodeldiagram.model.JOperation;
import org.eclipselabs.jmodeldiagram.model.JType;
import org.osgi.framework.BundleContext;

import pt.iscte.dcti.umlviewer.service.ClickHandler;
import pt.iscte.dcti.umlviewer.service.ServiceHelper;

public class Activator extends AbstractUIPlugin {
	private static final int DEFAULT_PORT = 8081;

	private static Activator instance;
	
	public static Activator getInstance() {
		return instance;
	}
	
	private int socketPort = DEFAULT_PORT;
	
	private ClickHandler handler = new ClickHandler() {
		
		@Override
		public void classClicked(JType type) {
			System.out.println("Click on " + type);
		}
		
		@Override
		public void methodClicked(JOperation operation) {
			System.out.println("Click on " + operation + " " + operation.getProperty("LINE"));
			
		}
	};
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		ServiceHelper.getService().addClickHandler(handler);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		instance = null;
	}

	public void setSocketPort(int port) {
		socketPort = port;
	}
	
	public int getSocketPort() {
		return socketPort;
	}
}
