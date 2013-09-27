package org.eclipselabs.guita.widgetsanalyzer.test;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class Teste extends Composite{

	Label image;
	Color white;

	public Teste(Composite parent, int style) {
		super(parent, style);
		FillLayout f = new FillLayout();
		f.marginHeight = 100;
		f.marginWidth = 100;
		setLayout(f);
		image = new Label(this, 0);
		image.setText("OLAAAAA");
		white = new Color(null, 255, 255, 255);
		image.setBackground(white);
	}

}
