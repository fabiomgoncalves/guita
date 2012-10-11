package org.eclipselabs.guita.uitracer.view;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipselabs.guita.uitrace.common.Defaults;
import org.eclipselabs.guita.uitrace.common.Location;
import org.eclipselabs.guita.uitrace.common.Trace;


public class TraceItem extends CTabItem {

	private Trace trace;
	private final TraceView view;
	private Composite area;	
	private Scale timeline;
	private Canvas canvas;
//	private Label timelineLabel;
	private Button backwardButton;
	private Button forwardButton;
	
	private Text debug;
	
	private static final int PADDING = 5;
	
	public TraceItem(CTabFolder parent, final Trace trace, final Image image, final TraceView view) {
		super(parent, SWT.CLOSE);
		this.trace = trace;
		this.view = view;
		
//		setImage(image);
		setText(trace.getWidgetName());
		
		area = new Composite(parent, SWT.NONE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.spacing = 10;
		layout.marginTop = 10;
		area.setLayout(layout);
		
		addButtons();
		loadTimeline();
		
		loadImage(image);
				
//		loadQuestions();
//		String s = "";
//		for(int i = 0; i < trace.size(); i++)
//			s += trace.at(i) + "\n";
//		new Label(area, SWT.BORDER).setText(s);
		
//		debug = new Text(area,SWT.BORDER);
		
		setControl(area);
		
	}

	private void loadQuestions() {
		Link link = new Link(area, SWT.NONE);
		link.setText("<a>Which object did this?</a>");
		link.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				
				Socket clientSocket = null;
				try {
					clientSocket = new Socket("localhost", Defaults.APPLICATION_PORT);	
					OutputStream os = clientSocket.getOutputStream();  
					ObjectOutputStream oos = new ObjectOutputStream(os);
					oos.writeObject(trace.at(timeline.getSelection()));
					oos.close();
					os.close();
					clientSocket.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	private void loadImage(final Image image) {
		if(canvas != null)
			canvas.dispose();
		
		ImageData data = image.getImageData();
		
		
		canvas = new Canvas(area, SWT.BORDER);
//		canvas.setBackground(new Color(Display.getDefault(), 255,255,255));
		int width = data.width + PADDING*2;
		int height = data.height + PADDING*2;
		
//		data = Blur.blur(data, 1);
		data.alpha = 128;
		
		final Image blur = new Image(area.getDisplay(), data);
		canvas.setLayoutData(new RowData(width, height));
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(blur, PADDING-1, PADDING-1);
			}
		});
	}
	
	private void loadTimeline() {
//		if(timelineLabel != null)
//			timelineLabel.dispose();

		if(timeline != null)
			timeline.dispose();
		
//		timelineLabel = new Label(area, SWT.NONE);
//		timelineLabel.setText("Timeline:");
		
		timeline = new Scale (area, SWT.BORDER);
		timeline.setMinimum(0);
		int max = Math.max(1, trace.size()-1);
		timeline.setMaximum (max);
		timeline.setPageIncrement (1);
		timeline.setSelection(0);
		timeline.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				navigateToSelection();
			}
		});						
	}
	
	private void addButtons() {
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayout(new RowLayout(SWT.VERTICAL));
		
		backwardButton = new Button(comp, SWT.PUSH);
		backwardButton.setText("<");
		backwardButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(timeline.getSelection() > 0) {
					timeline.setSelection(timeline.getSelection() - 1);
					navigateToSelection();
				}
			}
		});
		
		forwardButton = new Button(comp, SWT.PUSH);
		forwardButton.setText(">");
		forwardButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(timeline.getSelection() < timeline.getMaximum()) {
					timeline.setSelection(timeline.getSelection() + 1);
					navigateToSelection();
				}
			}
		});
	}
	
	
	public void navigateToSelection() {
		Location loc = trace.at(
				trace.size() == 1 ? 0 :
				timeline.getSelection());
		
//		debug.setText(loc.toString() + " : " + loc.lineNumber());
//		debug.pack();
		area.layout();
		
		view.navigate(loc);
		
	}
	
	public void reload(Trace trace, Image image) {
		if(!this.trace.sameAs(trace))
			throw new RuntimeException();
		
		this.trace = trace;
		loadTimeline();
		loadImage(image);
		area.layout();
	}
	
	
	
	public boolean hasTrace(Trace trace) {
		return this.trace.sameAs(trace);
	}

//	public void setFileNotFound() {
//		shotLabel.setText("File not found");	
//	}
	

}
