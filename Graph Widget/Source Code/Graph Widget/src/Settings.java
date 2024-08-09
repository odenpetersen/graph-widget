/**
Settings.java
This file defines the Settings class. This is intended to store the state of the
	settings currently selected by the user, and to communicate this between windows
	e.g. through the mainSettings and currentSettings instances
**/

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class Settings {
	boolean showGridlines = false;	//Not yet implemented
	boolean showAxes = true;
	boolean labelAxes = true;	//Not yet implemented
	boolean labelIntercepts = true;	//Not yet implemented
	boolean labelIntersections = false;	//Not yet implemented
	
	//If true, prints out debug statements to console, otherwise prints nothing
	//Also ensures the unit tests are run
	static public boolean debugMode = true;
	
	Color backgroundColor = new Color(Display.getCurrent(), 255, 255, 255);
	Color penColor = new Color(Display.getCurrent(), 0, 0, 0);
	
	double xZoom = 50;
	double yZoom = 50;
	int xPan = 0;
	int yPan = 0;
	int panSpeed = 10;
	double zoomRatio = 1;	//xZoom over yZoom
	double zoomSpeed = 1.1;
	double accuracyThreshold = 3;
	double inequalityTransparency = 0.5;	//Not yet implemented
	String currentEquations = "";
}
