/*
 * Copyright 2011 Henri Kerola
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.addon.gwtgraphics.client.impl;

import java.util.List;

import org.vaadin.addon.gwtgraphics.client.Group;
import org.vaadin.addon.gwtgraphics.client.Image;
import org.vaadin.addon.gwtgraphics.client.Line;
import org.vaadin.addon.gwtgraphics.client.VectorObject;
import org.vaadin.addon.gwtgraphics.client.impl.util.NumberUtil;
import org.vaadin.addon.gwtgraphics.client.impl.util.VMLUtil;
import org.vaadin.addon.gwtgraphics.client.shape.Circle;
import org.vaadin.addon.gwtgraphics.client.shape.Ellipse;
import org.vaadin.addon.gwtgraphics.client.shape.Path;
import org.vaadin.addon.gwtgraphics.client.shape.Rectangle;
import org.vaadin.addon.gwtgraphics.client.shape.Text;
import org.vaadin.addon.gwtgraphics.client.shape.path.Arc;
import org.vaadin.addon.gwtgraphics.client.shape.path.ClosePath;
import org.vaadin.addon.gwtgraphics.client.shape.path.CurveTo;
import org.vaadin.addon.gwtgraphics.client.shape.path.LineTo;
import org.vaadin.addon.gwtgraphics.client.shape.path.MoveTo;
import org.vaadin.addon.gwtgraphics.client.shape.path.PathStep;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * This class contains the VML implementation module of GWT Graphics.
 *
 * @author Henri Kerola
 *
 */
public class VMLImpl extends SVGImpl {

	// TODO: get rid of all this crap

	@Override
	public String getRendererString() {
		return "VML";
	}

	@Override
	public String getStyleSuffix() {
		return "vml";
	}

	@Override
	public Element createDrawingArea(Element container, int width, int height) {
		addNamespaceAndStyle(VMLUtil.VML_NS_PREFIX,
				VMLUtil.VML_ELEMENT_CLASSNAME);

		container.getStyle().setProperty("position", "relative");
		container.getStyle().setProperty("overflow", "hidden");
		container.getStyle().setPropertyPx("width", width);
		container.getStyle().setPropertyPx("height", height);
		disableSelection(container);

		Element container2 = Document.get().createDivElement();
		container2.getStyle().setProperty("position", "absolute");
		container2.getStyle().setProperty("overflow", "hidden");
		container2.getStyle().setPropertyPx("width", width);
		container2.getStyle().setPropertyPx("height", height);
		container.appendChild(container2);

		Element root = VMLUtil.createVMLElement("group");
		setDefaultSize(root);
		container2.appendChild(root);
		return root;
	}

	private native void disableSelection(Element element) /*-{
		element.onselectstart = function() { return false };
	}-*/;

	private native void addNamespaceAndStyle(String ns, String classname) /*-{
		if (!$doc.namespaces[ns]) {
		$doc.namespaces.add(ns, "urn:schemas-microsoft-com:vml");
		// IE8's standards mode doesn't support * selector
		$doc.createStyleSheet().cssText = "." + classname + "{behavior:url(#default#VML); position: absolute; display:inline-block; }";
		}
	}-*/;

	@Override
	public Element createElement(Class<? extends VectorObject> type) {
		Element element = null;
		if (type == Rectangle.class) {
			element = VMLUtil.createVMLElement("roundrect");
			element.setAttribute("arcsize", "");
		} else if (type == Circle.class || type == Ellipse.class) {
			element = VMLUtil.createVMLElement("oval");
		} else if (type == Path.class) {
			element = VMLUtil.createVMLElement("shape");
			setDefaultSize(element);
		} else if (type == Text.class) {
			element = VMLUtil.createVMLElement("shape");
			setDefaultSize(element);

			Element path = VMLUtil.createVMLElement("path");
			path.setPropertyBoolean("textpathok", true);
			path.setPropertyString("v", "m 0,0 l 1,0");
			element.appendChild(path);

			Element textpath = VMLUtil.createVMLElement("textpath");
			textpath.setPropertyBoolean("on", true);
			// textpath.getStyle().setProperty("v-text-align", "left");
			element.appendChild(textpath);
		} else if (type == Image.class) {
			element = VMLUtil.createVMLElement("image");
		} else if (type == Line.class) {
			element = VMLUtil.createVMLElement("line");
		} else if (type == Group.class) {
			element = VMLUtil.createVMLElement("group");
			setDefaultSize(element);
		}
		return element;
	}

	@Override
	public int getX(Element element) {
		String tagName = VMLUtil.getTagName(element);
		if ( tagName.toLowerCase().equals("group") ){
			MatchResult r = getCoordOrigin(element);
			return r != null && r.getGroupCount() == 3 ? NumberUtil.parseIntValue(r.getGroup(1), 0) : 0;
		}else{
			return element.getPropertyInt("_x");
		}
	}

	@Override
	public void setX(Element element, int x, boolean attached) {
		setXY(element, x, true, attached);
	}

	@Override
	public int getY(Element element) {
		String tagName = VMLUtil.getTagName(element);
		if ( tagName.toLowerCase().equals("group") ){
			MatchResult r = getCoordOrigin(element);
			return r != null && r.getGroupCount() == 3 ? NumberUtil.parseIntValue(r.getGroup(2), 0) : 0;
		}else{
			return element.getPropertyInt("_y");
		}
	}

	@Override
	public void setY(Element element, int y, boolean attached) {
		setXY(element, y, false, attached);
	}

	private MatchResult getCoordOrigin(Element e){
		String coordorigin = e.getAttribute("coordorigin");
		return coordorigin != null ? RegExp.compile("(\\-?\\d+)\\s(\\-?\\d+)").exec(coordorigin) : null;
	}

	@Override
	public String getFillColor(Element element) {
		return element.getPropertyString("_fill-color");
	}

	@Override
	public void setFillColor(Element element, String color) {
		Element fill = VMLUtil.getOrCreateChildElementWithTagName(element,
				"fill");
		if (color == null) {
			fill.setPropertyString("color", "black");
			fill.setPropertyBoolean("on", false);
		} else {
			fill.setPropertyString("color", color);
			fill.setPropertyBoolean("on", true);
		}
		element.setPropertyString("_fill-color", color);
	}

	@Override
	public double getFillOpacity(Element element) {
		return element.getPropertyDouble("_fill-opacity");
	}

	@Override
	public void setFillOpacity(Element element, double opacity) {
		VMLUtil.getOrCreateChildElementWithTagName(element, "fill")
		.setPropertyString("opacity", "" + opacity);
		element.setPropertyDouble("_fill-opacity", opacity);
	}

	@Override
	public String getStrokeColor(Element element) {
		return element.getPropertyString("_stroke-color");
	}

	@Override
	public void setStrokeColor(Element element, String color) {
		Element stroke = VMLUtil.getOrCreateChildElementWithTagName(element,
				"stroke");
		stroke.setPropertyString("color", color);
		stroke.setPropertyBoolean("on", color != null ? true : false);
		element.setPropertyString("_stroke-color", color);
	}

	@Override
	public int getStrokeWidth(Element element) {
		return element.getPropertyInt("_stroke-width");
	}

	@Override
	public void setStrokeWidth(Element element, int width, boolean attached) {
		Element stroke = VMLUtil.getOrCreateChildElementWithTagName(element,
				"stroke");
		stroke.setPropertyString("weight", width + "px");
		stroke.setPropertyBoolean("on", width > 0 ? true : false);
		// store value for getter
		element.setPropertyInt("_stroke-width", width);
		if (isTextElement(element)) {
			fixTextPosition(element, attached);
		}
	}

	@Override
	public double getStrokeOpacity(Element element) {
		return element.getPropertyDouble("_stroke-opacity");
	}

	@Override
	public void setStrokeOpacity(Element element, double opacity) {
		VMLUtil.getOrCreateChildElementWithTagName(element, "stroke")
		.setPropertyString("opacity", "" + opacity);
		element.setPropertyDouble("_stroke-opacity", opacity);
	}

	@Override
	public int getWidth(Element element) {
		if (VMLUtil.getTagName(element).equals("group")) {
			// DrawingArea's root element
			element = element.getParentElement();
		}
		return NumberUtil.parseIntValue(
				element.getStyle().getProperty("width"), 0);
	}

	@Override
	public void setWidth(Element element, int width) {
		if (VMLUtil.getTagName(element).equals("group")) {
			// DrawingArea's root element
			element = element.getParentElement();
			element.getParentElement().getStyle().setPropertyPx("width", width);
		}
		element.getStyle().setPropertyPx("width", width);
	}

	@Override
	public int getHeight(Element element) {
		if (VMLUtil.getTagName(element).equals("group")) {
			// DrawingArea's root element
			element = element.getParentElement();
		}
		return NumberUtil.parseIntValue(element.getStyle()
				.getProperty("height"), 0);
	}

	@Override
	public void setHeight(Element element, int height) {
		if (VMLUtil.getTagName(element).equals("group")) {
			// DrawingArea's root element
			element = element.getParentElement();
			element.getParentElement().getStyle()
			.setPropertyPx("height", height);
		}
		element.getStyle().setPropertyPx("height", height);
	}

	@Override
	public int getCircleRadius(Element element) {
		return getEllipseRadiusX(element);
	}

	@Override
	public void setCircleRadius(Element element, int radius) {
		setEllipseRadiusX(element, radius);
		setEllipseRadiusY(element, radius);
	}

	@Override
	public int getEllipseRadiusX(Element element) {
		return getWidth(element) / 2;
	}

	@Override
	public void setEllipseRadiusX(Element element, int radiusX) {
		setWidth(element, 2 * radiusX);
		setX(element, getX(element), false);
	}

	@Override
	public int getEllipseRadiusY(Element element) {
		return getHeight(element) / 2;
	}

	@Override
	public void setEllipseRadiusY(Element element, int radiusY) {
		setHeight(element, 2 * radiusY);
		setY(element, getY(element), false);
	}

	@Override
	public void drawPath(Element element, List<PathStep> steps) {
		StringBuilder path = new StringBuilder();
		double x = -1;
		double y = -1;
		for (PathStep step : steps) {
			appendPathStep(path, step);

			if (step instanceof MoveTo) {
				MoveTo moveTo = (MoveTo) step;
				x = moveTo.getX() + (moveTo.isRelativeCoords() ? x : 0);
				y = moveTo.getY() + (moveTo.isRelativeCoords() ? y : 0);
			} else {
				// TODO close
			}
		}
		element.setAttribute("path", path.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void getPathStepString(Element element, PathStep step) {
		StringBuilder path = new StringBuilder();
		appendPathStep(path, step);
		element.getAttribute("path").concat(path.toString());
	}

	@Override
	protected void appendPathStep(StringBuilder path, PathStep step) {
		if (step instanceof Arc) {
			// TODO
		} else if (step instanceof CurveTo) {
			CurveTo curve = (CurveTo) step;
			path.append(curve.isRelativeCoords() ? " v" : " c");
			path.append(curve.getX1()).append(" ").append(curve.getY1());
			path.append(" ").append(curve.getX2()).append(" ")
			.append(curve.getY2());
			path.append(" ").append(curve.getX()).append(" ")
			.append(curve.getY());
		} else if (step instanceof LineTo) {
			LineTo lineTo = (LineTo) step;
			path.append(lineTo.isRelativeCoords() ? " r" : " l")
			.append(lineTo.getX()).append(" ").append(lineTo.getY());
		} else if (step instanceof MoveTo) {
			MoveTo moveTo = (MoveTo) step;
			path.append(moveTo.isRelativeCoords() ? " t" : " m")
			.append(moveTo.getX()).append(" ").append(moveTo.getY());
		}else if (step instanceof ClosePath) {
			path.append(" x e");
		}
	}

	private void setDefaultSize(Element element) {
		setSize(element, 1, 1);
	}

	private void setSize(Element element, int width, int height) {
		element.getStyle().setPropertyPx("width", width);
		element.getStyle().setPropertyPx("height", height);
		element.setPropertyString("coordorigin", "0 0");
		element.setPropertyString("coordsize", width + " " + height);
	}

	private void fixTextPosition(final Element element, final boolean attached) {
		if (!attached) {
			return;
		}
		element.getStyle().setProperty("visibility", "hidden");
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				setX(element, getX(element), attached);
				setY(element, getY(element), attached);
				element.getStyle().setProperty("visibility", "visible");
			}
		});
	}

	private void setXY(Element element, int xy, boolean x, boolean attached) {
		// Save value for getter
		element.setPropertyInt(x ? "_x" : "_y", xy);

		String tagName = VMLUtil.getTagName(element);
		if ( tagName.equals("group")){
			int other = x ? getY(element) : getX(element);

			StringBuilder sb = new StringBuilder();
			if ( x ){
				sb.append(-xy).append(" ").append(other);
			}else{
				sb.append(other).append(" ").append(-xy);
			}
			element.setAttribute("coordorigin", sb.toString());
		}else if (tagName.equals("line")) {
			if (x) {
				setLineFromTo(element, xy, null, true);
			} else {
				setLineFromTo(element, null, xy, true);
			}
		} else {
			if (isTextElement(element)) {
				int rot = getRotation(element);
				setRotation(element, 0, attached);
				if (x) {
					xy += (element.getOffsetWidth() / 2) - 1;
				} else {
					xy -= (element.getOffsetHeight() / 2) - 1;
				}
				setRotation(element, rot, attached);
			} else if (tagName.equals("oval")) {
				xy = xy
						- NumberUtil.parseIntValue(element.getStyle()
								.getProperty(x ? "width" : "height"), 0) / 2;
			}
			element.getStyle().setPropertyPx(x ? "left" : "top", xy);
		}
	}

	private void setLineFromTo(Element element, Integer x, Integer y,
			boolean from) {
		StringBuilder value = new StringBuilder();
		String xAttr = from ? "_x1" : "_x2";
		String yAttr = from ? "_y1" : "_y2";
		if (x != null) {
			value.append(x);
			element.setPropertyInt(xAttr, x);
		} else if (element.getPropertyString(xAttr) != null) {
			value.append(element.getPropertyInt(xAttr));
		} else {
			// x-coordinate not specified
			return;
		}
		value.append(" ");
		if (y != null) {
			value.append(y);
			element.setPropertyInt(yAttr, y);
		} else if (element.getPropertyString(yAttr) != null) {
			value.append(element.getPropertyInt(yAttr));
		} else {
			// y-coordinate not specified
			return;
		}
		element.setPropertyString(from ? "from" : "to", value.toString());
	}

	@Override
	public String getText(Element element) {
		return VMLUtil.getPropertyOfFirstChildElementWithTagName(element,
				"textpath", "string");
	}

	@Override
	public void setText(Element element, String text, boolean attached) {
		VMLUtil.getOrCreateChildElementWithTagName(element, "textpath")
		.setPropertyString("string", text);
		fixTextPosition(element, attached);
	}

	@Override
	public String getTextFontFamily(Element element) {
		return element.getPropertyString("_fontfamily");
	}

	@Override
	public void setTextFontFamily(Element element, String family,
			boolean attached) {
		element.setPropertyString("_fontfamily", family);
		setTextFont(element, attached);
	}

	@Override
	public int getTextFontSize(Element element) {
		return element.getPropertyInt("_fontsize");
	}

	@Override
	public void setTextFontSize(Element element, int size, boolean attached) {
		element.setPropertyInt("_fontsize", size);
		setTextFont(element, attached);
	}

	private void setTextFont(Element element, boolean attached) {
		VMLUtil.getOrCreateChildElementWithTagName(element, "textpath")
		.getStyle()
		.setProperty(
				"font",
				element.getPropertyInt("_fontsize") + "px "
						+ element.getPropertyString("_fontfamily"));
		fixTextPosition(element, attached);
	}

	private boolean isTextElement(Element element) {
		return VMLUtil.getTagName(element).equals("shape")
				&& element.getFirstChildElement() != null
				&& VMLUtil.getTagName(element.getFirstChildElement()).equals(
						"path");
	}

	@Override
	public String getImageHref(Element element) {
		return element.getPropertyString("src");
	}

	@Override
	public void setImageHref(Element element, String src) {
		element.setPropertyString("src", src);
	}

	@Override
	public int getRectangleRoundedCorners(Element element) {
		return element.getPropertyInt("_arcsize");
	}

	@Override
	public void setRectangleRoundedCorners(Element element, int radius) {
		String arcsize = "";
		if (radius > 0) {
			double l = Math.min(getWidth(element), getHeight(element));
			arcsize = "" + radius / l;
		}
		element.setAttribute("arcsize", arcsize);

		// Save int value for getter
		element.setPropertyInt("_arcsize", radius);
	}

	@Override
	public int getLineX2(Element element) {
		return element.getPropertyInt("_x2");
	}

	@Override
	public void setLineX2(Element element, int x2) {
		setLineFromTo(element, x2, null, false);
	}

	@Override
	public int getLineY2(Element element) {
		return element.getPropertyInt("_y2");
	}

	@Override
	public void setLineY2(Element element, int y2) {
		setLineFromTo(element, null, y2, false);

	}

	@Override
	public void add(Element root, Element element, boolean attached) {
		root.appendChild(element);
		applyFillAndStroke(element, attached);
		if (isTextElement(element)) {
			fixTextPosition(element, attached);
		}
	}

	@Override
	public void insert(Element root, Element element, int beforeIndex,
			boolean attached) {
		Element e = root.getChildNodes().getItem(beforeIndex).cast();
		root.insertBefore(element, e);
		applyFillAndStroke(element, attached);
		if (isTextElement(element)) {
			fixTextPosition(element, attached);
		}
	}

	private void applyFillAndStroke(Element element, boolean attached) {
		if (VMLUtil.hasChildElementWithTagName(element, "fill")) {
			setFillColor(element, getFillColor(element));
			setFillOpacity(element, getFillOpacity(element));
		}
		if (VMLUtil.hasChildElementWithTagName(element, "stroke")) {
			setStrokeColor(element, getStrokeColor(element));
			setStrokeOpacity(element, getStrokeOpacity(element));
			setStrokeWidth(element, getStrokeWidth(element), attached);
		}
	}

	@Override
	public void remove(Element root, Element element) {
		root.removeChild(element);
	}

	@Override
	public void bringToFront(Element root, Element element) {
		root.appendChild(element);
	}

	@Override
	public void clear(Element root) {
		while (root.hasChildNodes()) {
			root.removeChild(root.getLastChild());
		}
	}

	@Override
	public void setStyleName(Element element, String name) {
		element.setClassName(VMLUtil.VML_ELEMENT_CLASSNAME + " " + name + "-"
				+ getStyleSuffix());
	}

	@Override
	public void setRotation(Element element, int degree, boolean attached) {
		element.getStyle().setPropertyPx("rotation", degree);
	}

	@Override
	public int getRotation(Element element) {
		return NumberUtil.parseIntValue(
				element.getStyle().getProperty("rotation"), 0);
	}

	@Override
	public void onAttach(Element element, boolean attached) {
		if (isTextElement(element)) {
			fixTextPosition(element, attached);
		}
	}

}
