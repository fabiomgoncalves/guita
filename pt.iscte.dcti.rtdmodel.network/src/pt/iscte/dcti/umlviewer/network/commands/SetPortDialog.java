package pt.iscte.dcti.umlviewer.network.commands;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SetPortDialog extends TitleAreaDialog {

  private Text text;

  private int portNumber;

  public SetPortDialog(Shell parentShell, int port) {
    super(parentShell);
    portNumber = port;
  }

  @Override
  public void create() {
    super.create();
    setTitle("Set socket port");
    setMessage("The port number has to be the same as in the instrumentation aspect (AspectJ)", IMessageProvider.INFORMATION);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    Composite container = new Composite(area, SWT.NONE);
    container.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout layout = new GridLayout(2, false);
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    container.setLayout(layout);

    createFirstName(container);
//    createLastName(container);

    return area;
  }

  private void createFirstName(Composite container) {
    Label lbtFirstName = new Label(container, SWT.NONE);
    lbtFirstName.setText("Port number");

    GridData dataFirstName = new GridData();
    dataFirstName.grabExcessHorizontalSpace = true;
    dataFirstName.horizontalAlignment = GridData.FILL;

    text = new Text(container, SWT.BORDER);
    text.setText(Integer.toString(portNumber));
    text.setLayoutData(dataFirstName);
    text.addListener(SWT.Verify,new Listener() {  
    	  @Override  
    	  public void handleEvent(Event event) {  
    	      String port = ((Text)event.widget).getText();  
    	      try{  
    	          int portNum = Integer.valueOf(port);  
    	          if(portNum <0 || portNum > 65535){  
    	                event.doit = false;  
    	         }  
    	      }  
    	      catch(Exception ex){  
    	         event.doit = false;  
    	      }                 
    	   }  
    	});  
  }

  @Override
  protected boolean isResizable() {
    return true;
  }

  // save content of the Text fields because they get disposed
  // as soon as the Dialog closes
  private void saveInput() {
    portNumber = Integer.parseInt(text.getText());
  }

  @Override
  protected void okPressed() {
    saveInput();
    super.okPressed();
  }

  public int getPortNumber() {
    return portNumber;
  }

} 