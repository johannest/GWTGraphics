package org.vaadin.addon.gwtgraphics.testapp.client.shape;

import org.vaadin.addon.gwtgraphics.client.shape.Ellipse;
import org.vaadin.addon.gwtgraphics.testapp.client.CodeView;
import org.vaadin.addon.gwtgraphics.testapp.client.Metadata;
import org.vaadin.addon.gwtgraphics.testapp.client.ShapeEditor;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.TextBox;

public class EllipseEditor extends ShapeEditor {

	private TextBox radiusX;

	private TextBox radiusY;

	public EllipseEditor(Ellipse e, Metadata metadata, boolean newVo) {
		super(e, metadata, newVo);

		animatableEditor.setTarget(e);
		animatableEditor.addProperties(new String[] { "radiusx", "radiusy" });

		radiusX = addTextBoxRow("Radius X", 8);
		radiusY = addTextBoxRow("Radius Y", 8);

		if (e != null) {
			radiusX.setText("" + e.getRadiusX());
			radiusY.setText("" + e.getRadiusY());
		}
	}

	@Override
	public void onChange(ChangeEvent event) {
		Object sender = event.getSource();
		super.onChange(event);
		if (vo == null) {
			return;
		}

		Ellipse ellipse = (Ellipse) vo;
		CodeView code = getCodeView();
		if (sender == radiusX) {
			try {
				ellipse.setRadius(Integer.parseInt(radiusX.getText()), ellipse.getRadiusY());
				code.addMethodCall(ellipse, "setRadiusX", ellipse.getRadiusX());		// TODO: fix this
			} catch (NumberFormatException e) {
			}
			radiusX.setText("" + ellipse.getRadiusX());
		} else if (sender == radiusY) {
			try {
				ellipse.setRadius(ellipse.getRadiusX(), Integer.parseInt(radiusY.getText()));
				code.addMethodCall(ellipse, "setRadiusY", ellipse.getRadiusY());		// TODO: fix this
			} catch (NumberFormatException e) {
			}
			radiusY.setText("" + ellipse.getRadiusY());
		}
	}
}
