package org.eclipselabs.guita.codetrace.swt.test;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewElement extends Composite{

	private Label label;
	private Text text;

	public NewElement(Composite parent, int style) {
		super(parent, style);
		FillLayout f = new FillLayout();
		f.marginHeight = 20;
		f.marginWidth = 20;
		setLayout(f);
		
		label = new Label(this, 0);
		label.setText("Insert Text Here:");
		Color white = new Color(null, 255, 255, 255);
		label.setBackground(white);
		
		text = new Text (this, 0);
		text.setSize(20, 20);
	}

}