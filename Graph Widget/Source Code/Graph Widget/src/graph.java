/**
graph.java
This is the main component of Graph Widget. It creates the main window with the
	equation box and canvas, and contains code to process user-entered equations
	and represent them graphically on the canvas. It references the
	settingsWindow.java application window, and the Settings.java class.
**/

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

public class graph {
	
	//List of characters which cannot be used in the equations
	// given by the user, but may be used in the tokenised equation
	private char reservedChars[] = {'[',']','{','}'};
	//List of operators; must be organised in reverse order of operations
	private char operatorList[] = {'-','+','*','/','^'};
	//List of function names; must be organised longest to shortest
	private String functionNames[] = {"sin","cos","tan","abs","ln"};
	//List of variable (and constant) names
	private String variableNames[] = {"pi","e","x","y"};
	
	protected Shell shlGraphWidget;
	private Text txtEquationBox;
	
	settingsWindow settingsWindowInstance = new settingsWindow();
	//The copy of the settings used in the main window.
	//This is sent to the settings window, when it is opened,
	//and then copied back when the settings window is closed.
	Settings mainSettings = new Settings();

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			graph window = new graph();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlGraphWidget.open();
		shlGraphWidget.layout();
		while (!shlGraphWidget.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		//All this code is generated automatically by Eclipse through its GUI design page,
		//with the exception of the listeners, which execute commands upon interaction with the GUI.
		shlGraphWidget = new Shell();
		shlGraphWidget.setSize(596, 249);
		GridLayout gl_shlGraphWidget = new GridLayout();
		gl_shlGraphWidget.numColumns = 7;
		shlGraphWidget.setLayout(gl_shlGraphWidget);
		shlGraphWidget.setText("Graph Widget");
		
		txtEquationBox = new Text(shlGraphWidget, SWT.MULTI);
		GridData gridData_txtEquationBox = new GridData();
		gridData_txtEquationBox.widthHint = 107;
		gridData_txtEquationBox.heightHint = 186;
		gridData_txtEquationBox.horizontalAlignment = GridData.FILL;
		gridData_txtEquationBox.verticalAlignment = GridData.FILL;
		gridData_txtEquationBox.grabExcessHorizontalSpace = true;
		gridData_txtEquationBox.grabExcessVerticalSpace = true;
		gridData_txtEquationBox.horizontalSpan = 1;
		txtEquationBox.setLayoutData(gridData_txtEquationBox);
		
		Canvas canvas = new Canvas(shlGraphWidget, SWT.BORDER);
		canvas.setBackground(mainSettings.backgroundColor);
		GridData gridData_canvas = new GridData();
		gridData_canvas.horizontalSpan = 6;
		gridData_canvas.widthHint = 234;
		gridData_canvas.horizontalAlignment = GridData.FILL;
		gridData_canvas.verticalAlignment = GridData.FILL;
		gridData_canvas.grabExcessVerticalSpace = true;
		gridData_canvas.grabExcessHorizontalSpace = true;
		canvas.setLayoutData(gridData_canvas);
		
		//This lets the user zoom and pan using the arrow keys
		canvas.addKeyListener(new KeyAdapter()
		{	
			public void keyPressed(KeyEvent e)
			{
				switch (e.keyCode) {
					//Arrow keys to pan
					case SWT.ARROW_UP:
						mainSettings.yPan=mainSettings.yPan+mainSettings.panSpeed;
						canvas.redraw();
						break;
					case SWT.ARROW_DOWN:
						mainSettings.yPan=mainSettings.yPan-mainSettings.panSpeed;;
						canvas.redraw();
						break;
					case SWT.ARROW_LEFT:
						mainSettings.xPan=mainSettings.xPan+mainSettings.panSpeed;;
						canvas.redraw();
						break;
					case SWT.ARROW_RIGHT:
						mainSettings.xPan=mainSettings.xPan-mainSettings.panSpeed;;
						canvas.redraw();
						break;
					case 61:	//+ button
						//Zoom in
						mainSettings.xZoom = mainSettings.xZoom * Math.pow(mainSettings.zoomSpeed,mainSettings.zoomRatio);
						mainSettings.yZoom = mainSettings.yZoom * mainSettings.zoomSpeed;
						canvas.redraw();
						break;
					case 45:	//- button
						//Zoom out
						mainSettings.xZoom = mainSettings.xZoom / Math.pow(mainSettings.zoomSpeed,mainSettings.zoomRatio);
						mainSettings.yZoom = mainSettings.yZoom / mainSettings.zoomSpeed;
						if (mainSettings.debugMode) {
							System.out.println("xZoom is:"+mainSettings.xZoom+" yZoom is:"+mainSettings.yZoom);
						}
						canvas.redraw();
						break;
					case 93:	//[ button
						//Decrease the sensitivity by decreasing the threshold
						mainSettings.accuracyThreshold *= 1.1;
						canvas.redraw();
						break;
					case 91:	//] button
						//Increase the sensitivity by decreasing the threshold
						mainSettings.accuracyThreshold /= 1.1;
						canvas.redraw();
						break;
				}
			}
		});
		
		//Button to redraw the graph
		Button btnGraph = new Button(shlGraphWidget, SWT.NONE);
		btnGraph.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnGraph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//Store the equations in the text box
				mainSettings.currentEquations = txtEquationBox.getText();
				//Remove the spaces from the equations e.g. "y = x" -> "y=x"
				mainSettings.currentEquations = mainSettings.currentEquations.replace(" ", "");
				//Redraw the graph
				canvas.redraw();
			}
		});
		btnGraph.setBounds(10, 255, 65, 28);
		btnGraph.setText("Graph");
		
		//Open settings
		Button btnSettings = new Button(shlGraphWidget, SWT.NONE);
		btnSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mainSettings.currentEquations = txtEquationBox.getText();
				//Open the settings window
				settingsWindowInstance.open(mainSettings);
				//When it is closed, update the settings to match and redraw the graph with the new settings
				mainSettings = settingsWindowInstance.currentSettings;
				//Set the background colour
				canvas.setBackground(mainSettings.backgroundColor);
				//Remove empty lines from the equations list
				mainSettings.currentEquations = mainSettings.currentEquations.replace("\n\n", "\n");
				//Redraw the graph
				canvas.redraw();
				//Print the equations used back into the text box
				txtEquationBox.setText(mainSettings.currentEquations);
			}
		});
		btnSettings.setBounds(143, 255, 76, 28);
		btnSettings.setText("Settings");
		
		Button btnHelp = new Button(shlGraphWidget, SWT.NONE);
		btnHelp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("https://sites.google.com/view/graph-widget/");
			}
		});
		btnHelp.setText("Help");
		
		//Zoom into the graph
		Button btnZoomIn = new Button(shlGraphWidget, SWT.NONE);
		btnZoomIn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mainSettings.xZoom = mainSettings.xZoom * Math.pow(mainSettings.zoomSpeed,mainSettings.zoomRatio);
				mainSettings.yZoom = mainSettings.yZoom * mainSettings.zoomSpeed;
				canvas.redraw();
			}
		});
		btnZoomIn.setBounds(313, 255, 40, 28);
		btnZoomIn.setText("+");
		
		//Zoom out of the graph
		Button btnZoomOut = new Button(shlGraphWidget, SWT.NONE);
		btnZoomOut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mainSettings.xZoom = mainSettings.xZoom / Math.pow(mainSettings.zoomSpeed,mainSettings.zoomRatio);
				mainSettings.yZoom = mainSettings.yZoom / mainSettings.zoomSpeed;
				if (mainSettings.debugMode) {
					System.out.println("xZoom is:"+mainSettings.xZoom+" yZoom is:"+mainSettings.yZoom);
				}
				canvas.redraw();
			}
		});
		btnZoomOut.setText("-");
		btnZoomOut.setBounds(359, 255, 37, 28);
		
		//Reset the zoom and pan
		Button btnCenterView = new Button(shlGraphWidget, SWT.NONE);
		btnCenterView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mainSettings.xZoom = 50;
				mainSettings.yZoom = 50;
				mainSettings.xPan = 0;
				mainSettings.yPan = 0;
				canvas.redraw();
			}
		});
		btnCenterView.setText("Center view");
		btnCenterView.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		//Reset the sensitivity
		Button btnResetSensitivity = new Button(shlGraphWidget, SWT.NONE);
		btnResetSensitivity.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mainSettings.accuracyThreshold = 3;
				canvas.redraw();
			}
		});
		btnResetSensitivity.setText("Reset Sensitivity");
		
		//This runs when the canvas is redrawn, for example, when the "graph" button is pressed.
		canvas.addPaintListener(new PaintListener() {
		      public void paintControl(PaintEvent onGraph) {
		    	//This object stores the height and width of the canvas.
		    	Rectangle rect = ((Canvas) onGraph.widget).getBounds();
		    	
		    	//Set the colours for the background and foreground
		    	onGraph.gc.setForeground(mainSettings.penColor);
		    	onGraph.gc.setBackground(mainSettings.backgroundColor);
		    	
		    	//Draws the x and y axes, if the settings say they should be drawn.
		    	if (mainSettings.showAxes) {
			    	onGraph.gc.drawLine(mainSettings.xPan+rect.width/2, 0, mainSettings.xPan+rect.width/2, rect.height);
				    onGraph.gc.drawLine(0, mainSettings.yPan+rect.height/2, rect.width, mainSettings.yPan+rect.height/2);
			    }
				
		    	//Gets the equations from the text box and splits them at each newline into an array.
				String equations[] = mainSettings.currentEquations.split("\\r?\\n");
				
				//Runs all the unit tests on the methods found in the graph and node classes.
				if (mainSettings.debugMode) {
					testGraphMethods();
				}
				
				String tokenisedEquation;	//Stores the tokenised form of the current equation.
				Node parsedEquation;	//Stores the parsed tree representation of the current equation.
				
				boolean containsInvalidEquation=false;	//True if there is at lease one invalid equation in the list, in which case a warning is displayed.
				
				//Loops through all the equations and points and checks if each point satisfies each equation.
				for (int equationIndex = 0; equationIndex < Array.getLength(equations);equationIndex++) {
					//Make sure the line isn't a comment; if it isn't, try to graph it
					if (!equations[equationIndex].startsWith("//")) {
						String currentEquation = equations[equationIndex];
						
						//Turn all functions of x into equations in x & y
						//This allows expressions like sin(x) to be interpreted as y=sin(x)
						if (!(currentEquation.contains("="))){
							currentEquation="y="+currentEquation;
						}
						
						//Check if the equation is valid and if so, graphs it on the canvas
						if (isValidExpression(currentEquation)) {
							//Tokenises the equation
							tokenisedEquation = tokenise(currentEquation);
							try {
								//Turns the equation into an expression tree
								parsedEquation = parse(tokenisedEquation);
								//Set the background
								onGraph.gc.setBackground(mainSettings.backgroundColor);
								
								//Loop through every pixel of the canvas and colour the pixel in if it matches the equation
								for (int xCoord = 0; xCoord < canvas.getBounds().width; xCoord++) {
									for (int yCoord = 0; yCoord < canvas.getBounds().height; yCoord++) {
										//The coordinates need to be adjusted based on zoom and pan
										double adjustedX = (xCoord-rect.width/2-mainSettings.xPan)/mainSettings.xZoom;
										double adjustedY = (-yCoord+rect.height/2+mainSettings.yPan)/mainSettings.yZoom;
										
										//The coordinates are checked against the equation and then plotted if they match
										try {
											if (substitute(adjustedX,adjustedY,parsedEquation).boolValue) {
												onGraph.gc.drawPoint(xCoord, yCoord);
											}
										} catch (Exception e){
											if (mainSettings.debugMode) {
												System.out.println("Substitution failure on x="+xCoord+", y="+yCoord+", "+currentEquation);
											}
										}
									}
								}
							} catch (Exception e) {
								containsInvalidEquation = true;
								if (mainSettings.debugMode) {
									System.out.println("Parse failure on "+currentEquation);
								}
							}
						} else {
							containsInvalidEquation = true;
						}
					}
				}
				//If there is an invalid equation, display a warning
				if (containsInvalidEquation && !mainSettings.currentEquations.isBlank()) {
					MessageBox warningText = new MessageBox(shlGraphWidget);
					warningText.setMessage("One or more of your equations/expressions is invalid. Please see the help document for more information.");
					warningText.open();
				}
		      }
		});
	}
	
	public boolean isValidExpression(String equationString) {
		boolean returnValue = true;
		
		//Check if the string is empty
		if (equationString.length()==0) {
			returnValue = false;
		}
		
		//Check for illegal characters by comparing each character of string to each element of reservedChars
		boolean isLegalChar;
		for (int charIndex = 0; charIndex < equationString.length(); charIndex++) {
			isLegalChar = true;
			for (int illegalCharIndex = 0; illegalCharIndex < Array.getLength(reservedChars); illegalCharIndex++) {
				if (reservedChars[illegalCharIndex]==equationString.charAt(charIndex)) {
					isLegalChar = false;
				}
			}
			if (!isLegalChar) {
				returnValue = false;
			}
		}
		
		//Check that brackets are paired by comparing the number of '(' and ')' brackets
		int bracketCheckSum = 0;
		for (int charIndex = 0;charIndex < equationString.length();charIndex++) {
			switch(equationString.charAt(charIndex)) {
			  case '(':
			    bracketCheckSum++;
			    break;
			  case ')':
			    bracketCheckSum--;
			    break;
			}
			//If a ')' is found without a pair, e.g. in "())", then brackets are not paired.
			if (bracketCheckSum < 0) {
				returnValue = false;
			}
		}
		
		//If the number of brackets of each type are not equal, then brackets are not paired.
		if (!(bracketCheckSum == 0)){
			returnValue = false;
		}
		
		if (mainSettings.debugMode) {
			System.out.println("isValidExpression returns " + Boolean.toString(returnValue) + " on "+ equationString);
		}
		return returnValue;
	}
	private String tokenise(String equationString) {
		if (mainSettings.debugMode) {
			System.out.println("Entered tokenise");
		}
		
		//Make the equation lowercase for ease of comparison with tokens
		String newString = equationString.toLowerCase();
		
		//Loop through the string and compare portions of it with each of the function and variable names
		String subString;	//Stores a section of the equation string
		char previousChar = ' ';	//Stores the character before the current one
		int charIndex = 0;	//Stores the index of the current character
		boolean exitLoop = false;	//Will cause the loops through the token names to be terminated if set to true
		while (charIndex < newString.length()) {	//Note: the length of newString will likely change during the loop
			if (!Character.isLetter(previousChar)) {	//Check if the previous character is a letter
				//Compare a part of the string at and after the current character with the name of each function
				exitLoop = false;
				int functionIndex = 0;
				do {
					subString = newString.substring(charIndex,Math.min(charIndex+functionNames[functionIndex].length(),newString.length()));
					if (subString.equals(functionNames[functionIndex])) {
						newString = newString.substring(0,charIndex) + "{" + functionNames[functionIndex] + "}" + newString.substring(charIndex+functionNames[functionIndex].length());
						charIndex = charIndex + subString.length() + 1;
						exitLoop = true;
					}
					functionIndex++;
				} while (!exitLoop && functionIndex < functionNames.length);
				
				//Compare a part of the string at and after the current character with the name of each variable (and constants e.g. pi, e)
				exitLoop = false;
				int variableIndex = 0;
				do {
					subString = newString.substring(charIndex,Math.min(charIndex+variableNames[variableIndex].length(),newString.length()));
					if (subString.equals(variableNames[variableIndex])) {
						newString = newString.substring(0,charIndex) + "[" + variableNames[variableIndex] + "]" + newString.substring(charIndex+variableNames[variableIndex].length());
						charIndex = charIndex + subString.length() + 1;
						exitLoop = true;
					}
					variableIndex++;
				} while (!exitLoop && variableIndex < variableNames.length);
			}
			charIndex++;
		}
		
		//Regular expression to insert multiplication symbols between brackets and numbers where appropriate, e.g.:
		// "...)[..." -> "...)*[..."	"...2(..." -> "...2*(..."
		newString = newString.replaceAll("([\\)\\][0-9]])([\\(\\[\\{])", "$1*$2");
		
		if (mainSettings.debugMode) {
			System.out.println("tokenise returns " + newString + " on "+ equationString);
		}
		return newString;
	}
	
	//If the parse function is given a string, put it in a node and then call the parse function with a node.
	private Node parse(String tokenisedExpression) {
		Node nodeToParse = new Node(tokenisedExpression);
		return parse(nodeToParse);
	}
	
	//Parses a node containing a tokenised expression into an expression tree, which can then be easily evaluated by the substitute function
	private Node parse(Node tokenisedNode){
		
		//The output of the parse function; it is "current" because the parse function is recursive.
		Node currentNode = tokenisedNode;
		
		//If an expression begins with a '-', such as in "-2", make it start with "0-", e.g. "0-2".
		if (currentNode.contents.charAt(0)=='-') {
			currentNode.contents = '0'+currentNode.contents;
		}
		
		//Search for an '=' character in the string. Returns -1 if not found.
		int indexInString = -1;
		for (int currentIndex=0;currentIndex<currentNode.contents.length();currentIndex++) {
			if (currentNode.contents.charAt(currentIndex)=='=') {
				indexInString = currentIndex;
			}
		}
		
		//If an '=' character is found, split the string there and create two new child nodes to represent the LHS and RHS of the equation.
		//These are to be parsed.
		if (!(indexInString == -1)) {
			Node lhs = new Node(currentNode.contents.substring(0, indexInString));
			currentNode.newChild(parse(lhs));
			Node rhs = new Node(currentNode.contents.substring(indexInString+1));
			currentNode.newChild(parse(rhs));
			currentNode.contents = "=";
		} else {	//If the '=' character is not found (the more common outcome), continue with the next part of the parse function.
			//Firstly, remove any brackets that enclose the entire string, e.g. "(2)" -> "2". This is done in a similar manner to how
			// the isValidExpression function checks that brackets are paired.
			boolean exitLoop = false;
			int bracketCheckSum;
			int charIndex;
			while (currentNode.contents.startsWith("(") && !exitLoop) {
				charIndex = 1;
				bracketCheckSum = 1;
				while ((charIndex <= currentNode.contents.length()) && !(bracketCheckSum==0)) {
					switch (currentNode.contents.charAt(charIndex)) {
						case ')':
							bracketCheckSum--;
							break;
						case '(':
							bracketCheckSum++;
							break;
					}
					charIndex++;
				}
				if (charIndex == currentNode.contents.length()) {
					currentNode.contents = currentNode.contents.substring(1,currentNode.contents.length()-1);
				} else {
					exitLoop = true;
				}
			}
			
			//Check if the string is enclosed by a function token, e.g. "{sin}(2)",
			// and then create an expression tree with the function token as the root node.
			//Firstly, the string must begin with '{' and end with ')'.
			boolean exitParse = false;
			if (currentNode.contents.startsWith("{") && currentNode.contents.endsWith(")")) {
				
				charIndex = 0;
				while (!(currentNode.contents.charAt(charIndex)=='}')) {
					charIndex++;
				}
				
				//Secondly, it must not be of the form "{...}(...)...(...)"; that is,
				// the last bracket must be paired with the bracket after the function token.
				// An example of a string not satisfying this is "{sin}(2)*(1+1)".
				int braceIndex = charIndex;
				charIndex++;
				bracketCheckSum = 1;
				while (!(bracketCheckSum==0)) {
					charIndex++;
					switch (currentNode.contents.charAt(charIndex)) {
						case '(':
							bracketCheckSum++;
							break;
						case ')':
							bracketCheckSum--;
							break;
					}
				}
				
				//Create the tree with the function token as the root node.
				if (charIndex == currentNode.contents.length()-1) {
					currentNode.newChild(parse(currentNode.contents.substring(braceIndex+1)));
					currentNode.contents = currentNode.contents.substring(0,braceIndex+1);
					exitParse = true;
				}
			}
			
			//If no tokens have been dealt with so far, check for infix-style operators such as +, -, /, *.
			if (exitParse==false) {
				int operatorIndex = 0;
				bracketCheckSum = 0;
				//The operators should be checked in reverse order of operations. This should be reflected in the operatorList array.
				while (!exitParse && (operatorIndex < operatorList.length)) {
					charIndex = currentNode.contents.length()-1;
					while ((charIndex >= 0) && !exitParse) {
						switch (currentNode.contents.charAt(charIndex)) {
							case '(':
								bracketCheckSum++;
								break;
							case ')':
								bracketCheckSum--;
								break;
						}
						//The operator must not be enclosed by any brackets. If it is to be parsed, it becomes the root node
						// of a tree with two children representing each of its arguments, e.g. "1+2" would have root "+" and children "1" and "2".
						// The children should also be parsed.
						if ((bracketCheckSum == 0) && (currentNode.contents.charAt(charIndex) == operatorList[operatorIndex])) {
							Node leftArg = parse(currentNode.contents.substring(0,charIndex));
							if (mainSettings.debugMode) {
								System.out.println("Parsing "+leftArg.contents);
							}
							currentNode.newChild(leftArg);
							Node rightArg = parse(currentNode.contents.substring(charIndex+1));
							if (mainSettings.debugMode) {
								System.out.println("Parsing "+rightArg.contents);
							}
							currentNode.newChild(rightArg);
							
							currentNode.contents = Character.toString(currentNode.contents.charAt(charIndex));
							exitParse = true;
						}
						charIndex--;
					}
					operatorIndex++;
				}
			}
		}
		
		//If no tokens are detected, the input and output are the same.
		// For example, parsing a node containing "2" should just return a node containing "2".
		if (mainSettings.debugMode) {
			System.out.println("parse returns " + currentNode.contents + " as parent on " + tokenisedNode.contents);
		}
		return currentNode;
	}
	
	//Substitute coordinates into an expression tree. If the tree has a '=' as its root, then the output will have a boolean value of "true",
	// otherwise it will have a numerical value representing the value of the expression at the two coordinates.
	private ReturnValues substitute(double xCoord, double yCoord, Node expressionTree) {
		//This output has both a boolean and numerical value, as both are acceptable outputs of this function.
		ReturnValues output = new ReturnValues();
		if (expressionTree.contents == "=") {
			double lhs = substitute(xCoord,yCoord,expressionTree.children.get(0)).numValue;
			double rhs = substitute(xCoord,yCoord,expressionTree.children.get(1)).numValue;
			if (Math.abs(lhs-rhs)<2*mainSettings.accuracyThreshold/(mainSettings.xZoom+mainSettings.yZoom)) {
				output.boolValue = true;
			} else {
				output.boolValue = false;
			}
		} else {
			double args[] = new double[expressionTree.getNumChildren()];
			for (int childrenIndex = 0; childrenIndex < Array.getLength(args); childrenIndex++) {
				args[childrenIndex] = substitute(xCoord,yCoord,expressionTree.children.get(childrenIndex)).numValue;
			}
			switch (expressionTree.contents) {
				case "[x]":
					output.numValue = xCoord;
					break;
				case "[y]":
					output.numValue = yCoord;
					break;
				/*case "[t]":
					output.numValue = 
					break;*/
				case "[e]":
					output.numValue = Math.E;
					break;
				case "[pi]":
					output.numValue = Math.PI;
					break;
				case "{sin}":
					output.numValue = Math.sin(args[0]);
					break;
				case "{cos}":
					output.numValue = Math.cos(args[0]);
					break;
				case "{tan}":
					output.numValue = Math.tan(args[0]);
					break;
				case "{abs}":
					output.numValue = Math.abs(args[0]);
					break;
				case "{ln}":
					output.numValue = Math.log(args[0]);
					break;
				case "+":
					output.numValue = args[0]+args[1];
					break;
				case "-":
					output.numValue = args[0]-args[1];
					break;
				case "/":
					output.numValue = args[0]/args[1];
					break;
				case "*":
					output.numValue = args[0]*args[1];
					break;
				case "^":
					output.numValue = Math.pow(args[0],args[1]);
					break;
				default:
					output.numValue = Double.valueOf(expressionTree.contents);
					break;
				}
		}
		return output;
	}
	private void testGraphMethods() {
		//TEST isValidExpression
		assert isValidExpression("x=y");
		assert isValidExpression("y=sin(x)");
		assert isValidExpression("x^2");
		assert !isValidExpression("x=(y");
		assert !isValidExpression("x=)(y");
		assert !isValidExpression("y=[x]");
		assert !isValidExpression("y={x}");
		
		//TEST tokenise
		assert tokenise("x=y")=="[x]=[y]";
		assert tokenise("y=sin(x)")=="[y]={sin}([x])";
		assert tokenise("x^2")=="[x]^2";
		
		//TEST parse
		assert parse("[y]=[x]").contents=="=";
		assert parse("[y]+[x]").contents=="+";
		
		//TEST substitute
		assert substitute(12,12,parse("x=y")).boolValue;
		assert substitute(0,0,parse("1=1")).boolValue;
		assert substitute(0,0,parse("1+1=2")).boolValue;
		assert substitute(5,7,parse("x+y")).numValue==12;
		assert !substitute(0,0,parse("2+2=5")).boolValue;
		assert !substitute(200,-200,parse("x=y")).boolValue;
		
		System.out.println("All tests passed!");
	}
}

class Node {
	String contents;
	ArrayList<Node> children = new ArrayList<Node>();
	
	//Make a node containing newContents
	public Node(String newContents){
		contents = newContents;
	}
	
	//Add a child to the node from a string
	public void newChild(String newContents) {
		Node childNode = new Node(newContents);
		children.add(childNode);
	}
	
	//Add a child to the node from a node
	public void newChild(Node childNode) {
		children.add(childNode);
	}
	
	//Get the number of children of the node
	public int getNumChildren() {
		return children.size();
	}
}

class ReturnValues {
	boolean isVoid = false;
	double numValue;
	boolean boolValue;
}