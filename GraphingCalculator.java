import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;


public class GraphingCalculator implements ActionListener, Runnable {	
	//GUI objects
	
	private JFrame calculatorWindow         = new JFrame("Graphing Calculator"); 
	
	private JPanel northPanel               = new JPanel(); 
	private JPanel southPanel               = new JPanel(); 
	private JPanel centerPanel              = new JPanel(); 
	
	private JTextField enterExpressionField = new JTextField("");  
	private JTextField xIncrementField      = new JTextField("");
	private JTextField errorMessagesField   = new JTextField("Error messages will be displayed here."); 
	private JTextField valueForXField       = new JTextField(""); 
	
	private JTextArea logArea               = new JTextArea("Press the enter key to solve expressions \nClick \"Graph!\" to plot them."); 
	
	private JButton clearButton       		= new JButton("Clear Text Areas"); 
	private JButton recallButton            = new JButton("Recall last expression");
	private JButton graphButton             = new JButton("Graph!");
	
	private JLabel expressionPrompt         = new JLabel ("Type expression here  ===============================>"); 
	private JLabel forXLabel                = new JLabel ("       x = "); 
	private JLabel incrementLabel           = new JLabel ("Step value for x variable ="); 
	
	private JScrollPane logAreaScrollPane   = new JScrollPane(logArea); 
	
	//Other variables
	String lastMessage; 
	boolean errorDetected1;
	boolean errorDetected2; 
	
	// Constructor
	public GraphingCalculator() throws Exception{
			
		    
			System.out.print("Judd Heater and Joe Collins Lab 9.");
			//Build the calculator (not the graph) GUI
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			calculatorWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			northPanel.setLayout(new GridLayout(2,1)); //enterExpressionField and errorMessagesField 
			northPanel.add(errorMessagesField);
			northPanel.add(recallButton); 
			northPanel.add(expressionPrompt); 
			northPanel.add(enterExpressionField);
			calculatorWindow.getContentPane().add(northPanel, "North");
			
			southPanel.setLayout(new GridLayout(0,6)); //clearButton and valueForXField
			southPanel.add(forXLabel); 
			southPanel.add(valueForXField);
			southPanel.add(incrementLabel);
			southPanel.add(xIncrementField);
			southPanel.add(clearButton);
			southPanel.add(graphButton);
			calculatorWindow.getContentPane().add(southPanel, "South");
			
			centerPanel.setLayout(new GridLayout(1,1)); 
			centerPanel.add(logAreaScrollPane);
			calculatorWindow.getContentPane().add(centerPanel, "Center"); 
			
			
			calculatorWindow.setSize(1000,500); // width, height (in "pixels"!)
			calculatorWindow.setVisible(true); // show it!
			
			//Set attributes for GUI objects
			errorMessagesField.setFont(new Font("default",Font.BOLD,12)); 
			errorMessagesField.setBackground(Color.pink);
			errorMessagesField.setEditable(false);
			logArea.setFont(new Font("default", Font.BOLD,18));
			logArea.setEditable(false);
			
			
			
			//Activate the buttons
			clearButton.addActionListener(this); 
			enterExpressionField.addActionListener(this);
			valueForXField.addActionListener(this);
			recallButton.addActionListener(this); 
			graphButton.addActionListener(this); 
			////////////////////////////////////////////////////////////////////////////////////////////////
			//
			// Instruction to user
		   
			  // calculator loop
			  
	}
	
	// main method just calls constructor
	public static void main(String[] args) throws Exception {
		try 
		{
			GraphingCalculator calculatorgui = new GraphingCalculator(); //load the program object 
		} catch (Exception e) 
		{
			System.out.println("Could not load calculator!" + e);
		}  
		
	}  
 	
	// checks for parentheses errors
	private int checkParenthesesErrors(String expression, int whoIsCalling) {
		String testString = ")r^*/+-";
		int numOfOpenParentheses = 0;
		int numOfCloseParentheses = 0;
		int i;
		boolean implicitParentheses = false;
		boolean error = false;
		
		for (i = 0; i < expression.length(); i++) {
			if((expression.charAt(i) == '('))
				numOfOpenParentheses = numOfOpenParentheses+1;
			if((expression.charAt(i) == ')'))
				numOfCloseParentheses = numOfCloseParentheses+1;
		}
		for (i = 0; i < expression.length()-1; i++) {
			if ((expression.charAt(i) == ')')&&(expression.charAt(i+1) == '(')) {
				implicitParentheses = true;
				continue;
			}
			if 	((expression.charAt(i) == '(') && (i!=0)) {
				if ((expression.charAt(i-1) != '(') && (!testString.contains(String.valueOf(expression.charAt(i-1))))){
					System.out.println("There should be an operator before an open parentheses");
					errorMessagesField.setText("There should be an operator before an open parentheses.");
					error = true;
					continue;
				}
			}
			if ((expression.charAt(i) == ')') && (!testString.contains(String.valueOf(expression.charAt(i+1))))){
				System.out.println("There should be an operator after a close parentheses");
				errorMessagesField.setText("There should be an operator after a close parentheses.");
				error = true;
				continue;
			}
		}
		
		if ((expression.charAt(expression.length()-1) == '(')) {
			System.out.println("Cannot end an expression with an open parentheses.");
			errorMessagesField.setText("Cannot end an expression with an open parentheses.");
			return 1;
		}
		if ((expression.charAt(0) == ')')) {
			System.out.println("Cannot start an expression with a close parentheses.");
			errorMessagesField.setText("Cannot start an expression with a close parentheses.");
			return 1;
		}
		if (numOfOpenParentheses!=numOfCloseParentheses) {
			System.out.println("There are missmatched parentheses.");
			errorMessagesField.setText("There are missmatched parentheses.");
			return 1;
		}
		if (implicitParentheses) {
			System.out.println("Cannot have implicit multiplication using parentheses.");
			errorMessagesField.setText("Cannot have implicit multiplication using parentheses.");
			System.out.println("Please use * operator instead.");
			return 1;
		}
		if (error)
			return 1;
		return 0;
	}
	
	// handles the rest of the errors
	private int checkForOtherErrors(String expression, int whoIsCalling) {
		// find the operator
		char operator = ' ';
		boolean isError = false;
		int i;
		String illegalCharacters = "abcdefghjklmnoqstuvwyz!@#$%&_,<>?`~|[] {}'\\";
		for (i = 0; i < expression.length(); i++) {
			if (illegalCharacters.contains(String.valueOf(expression.charAt(i)))){
				errorMessagesField.setText("Illegal character: " + String.valueOf(expression.charAt(i)));
				isError = true;
			}
		}
		if (isError)
			return 1;
		for (i = 1; i < expression.length(); i++) { // allow leading unary!
			if((expression.charAt(i) == '^')
			 ||(expression.charAt(i) == 'r')
			 ||(expression.charAt(i) == '*')
			 ||(expression.charAt(i) == '/')
			 ||(expression.charAt(i) == '+')
			 ||(expression.charAt(i) == '-')){
				operator = expression.charAt(i);
				break;
			   }
		 }
		 if(whoIsCalling == 0){
		     if (i == expression.length()){
		    	System.out.println("Entered expression does not contain an operator.");
		    	errorMessagesField.setText("Entered expression does not contain an operator.");
		    	return 1;
		     }
		 }
	     if (i == expression.length()-1){
	 	    System.out.println("Expression should not end with an operator.");
	 	   errorMessagesField.setText("Expression should not end with an operator.");
	 	   return 1;
	     }
		return 0;
	}

	// unaryHandler changes '-'s to 'u's in order to differentiate minus operators
	// and negative numbers
	private String unaryHandler(String expression) {
		
		String testString = "(r^*/+-";
	       char[] newExpression = expression.toCharArray();
	       
	       for (int i = 0; i < expression.length(); i++) {
	            if((newExpression[i] == '-') && (i != 0)){
            	if(testString.contains(String.valueOf(newExpression[i-1]))){
            		
            		newExpression[i] = 'u';
            	}
            }
            else if((newExpression[i] == '-') && (i == 0)){
            	
            	newExpression[i] = 'u';
            }
        }
        return String.valueOf(newExpression);
	}
	
	// this method grabs the expresion in the parentheses and sends it to calc
	private String parenthesesHandler(String expression) {
		int i = 0;
		int start = 0;
		int end = 0;
		String anExpressionInParentheses;
		String anExpressionWithParentheses;
		
		while (expression.contains("(")) {
			//find start
			for (i = 0; i < expression.length(); i++) {
				if((expression.charAt(i) == '(')) {
					start = i;
					break;
				}
			}
			int pMatch = 0;
			//find end
			for (i = 0; i < expression.length(); i++) {
				if((expression.charAt(i) == '(')) {
					pMatch ++; 
				}
				if((expression.charAt(i) == ')')) {
					if (pMatch == 1) {
						end = i;
						break;
					}
					else 
						pMatch --;
				}
			}
			
			char[] tempExpression = expression.toCharArray();
			tempExpression[start] = ' ';
			tempExpression[end] = ' ';
			expression = String.valueOf(tempExpression);

			
			anExpressionInParentheses  = expression.substring(start+1,end);
			anExpressionWithParentheses = expression.substring(start,end);
			
			tempExpression = expression.toCharArray();
			
			expression = expression.replace(anExpressionWithParentheses,calculate(anExpressionInParentheses));
			
		}
		return expression;
	}
		
	// this is the "any expression" calculator method.
	// if the expression passed to this contains parentheses, it calls the parenthesesHandler method
	// it works by calling the oneAtATime method which identifies a simple expression like (number1 operator number2)
	// then the simpleExpression calculator to solve the simple expression
	// then it repeats the steps
	private String calculate (String expression) {
		
		String simpleExpression;
		// Are there any parentheses?!
		if (expression.contains("(") || expression.contains(")")){
			// then call the parenthesesHandler
			expression = parenthesesHandler(expression);
		}
		
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//POWERS AND ROOTS
		//if expression contains either root or power operators but not both...
		if (expression.contains("r") ^ expression.contains("^")) { 
            if (expression.contains("r")) {
            	// if there are only roots operators, call the one at a time method and calculate method to recursively replace the simple expressions
                while (expression.contains("r")) {
                	simpleExpression = oneAtATime(expression, 'r'); // get the simple x r y expression
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression); // calculate the solution to the simple expression
                    expression = expression.replace(simpleExpression, evaluatedValue); // replace expression in string with the solution
                }
                
            } 
            else if (expression.contains("^")) {
            	// same as roots but with power operators
                while (expression.contains("^")) {;
                	simpleExpression = oneAtATime(expression, '^'); // get simple expression
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression);  // calculate it
                    expression=expression.replaceFirst(Pattern.quote(simpleExpression), evaluatedValue);  //replace it
                }
            }
        } 
		else if (expression.contains("r") && expression.contains("^")) {  //root and exponent in expression
            //calculate in order from left to right
        	while (expression.contains("r") || expression.contains("^")) {
                int rootPosition = expression.indexOf("r");
                int powerPosition = expression.indexOf("^");
                //if there are still power operators, but no roots left
                if (rootPosition < 0) {    
                	while (expression.contains("^")) {
                    	simpleExpression = oneAtATime(expression, '^');// get simple expression
                        String evaluatedValue = simpleExpressionCalculator(simpleExpression);// calculate it
                        expression=expression.replaceFirst(Pattern.quote(simpleExpression), evaluatedValue);//replace it
                    }
                } 
                else if (powerPosition < 0) {  //if there are still power operators, but no roots left
                	while (expression.contains("r")) {
                    	simpleExpression = oneAtATime(expression, 'r');// get simple expression
                        String evaluatedValue = simpleExpressionCalculator(simpleExpression);// calculate it
                        expression=expression.replaceFirst(simpleExpression, evaluatedValue);//replace it
                    }
                } 
                else if (rootPosition < powerPosition) { //if root appears first
                	simpleExpression = oneAtATime(expression, 'r'); // get simple expression
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression); // calculate it
                    expression=expression.replaceFirst(simpleExpression, evaluatedValue); //replace it
                } 
                // at this point, the power operator must appear first
                else {
                	simpleExpression = oneAtATime(expression, '^'); // get simple expression
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression); // calculate it
                    expression=expression.replaceAll(Pattern.quote(simpleExpression), evaluatedValue); //replace it
                } 
            }
        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//MULTIPLICATION AND DIVISION
		// same exact algorithm as power and root operators!!!
        if (expression.contains("*") ^ expression.contains("/")) {
            if (expression.contains("*")) {
            	while (expression.contains("*")) {
                	simpleExpression = oneAtATime(expression, '*');
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                    expression=expression.replace(simpleExpression, evaluatedValue);
                }
            } 
            else if (expression.contains("/")) {
            	expression = expression.substring(0); //temporary index
            	while (expression.contains("/")) {
                	simpleExpression = oneAtATime(expression, '/');
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression); 
                    expression=expression.replace(simpleExpression, evaluatedValue); 

                }
            }
        } 
        else if (expression.contains("/") && expression.contains("*")) {
        	while (expression.contains("/") || expression.contains("*")) {
                int divIndex = expression.indexOf("/");
                int multIndex = expression.indexOf("*");
                if (divIndex < 0) {    //if there are still multiplication, but no division left
                	while (expression.contains("*")) {
                    	simpleExpression = oneAtATime(expression, '*');
                        String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                        expression=expression.replace(simpleExpression, evaluatedValue);
                    }
                } 
                else if (multIndex < 0) {  //if there is still division, but no multiplication left
                	while (expression.contains("/")) {
                    	simpleExpression = oneAtATime(expression, '/');
                        String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                        expression=expression.replace(simpleExpression, evaluatedValue);
                    }
                } 
                else if (divIndex < multIndex) {
                	simpleExpression = oneAtATime(expression, '/');
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                    expression=expression.replace(simpleExpression, evaluatedValue);
                } 
                else {
                	simpleExpression = oneAtATime(expression, '*');
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                    expression=expression.replace(simpleExpression, evaluatedValue);
                }
            }
        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//ADDITION AND SUBTRACTION
        
        // same as previous two sections 
        if (expression.contains("+") ^ expression.contains("-")) {
            if (expression.contains("+")) {
            	while (expression.contains("+")) {
                	simpleExpression = oneAtATime(expression, '+');
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                    expression=expression.replace(simpleExpression, evaluatedValue);
                }
            } 
            else if (expression.contains("-")) {
            	expression = expression.substring(0); //temporary index
            	while (expression.contains("-")) {
                    simpleExpression = oneAtATime(expression, '-');
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                    expression=expression.replace(simpleExpression, evaluatedValue);
                }
            }

        } 
        else if (expression.contains("+") &&expression.contains("-")) {
        	while (expression.contains("+") || expression.contains("-")) {
                int plusIndex = expression.indexOf("+");
                int subIndex = expression.indexOf("-");
                if (plusIndex < 0) {    //if there are still subtraction, but no addition left
                	while (expression.contains("-")) {
                        simpleExpression = oneAtATime(expression, '-');
                        String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                        expression=expression.replace(simpleExpression, evaluatedValue);
                	}
                } 
                else if (subIndex < 0) {  //if there is still addition, but no subtraction left
                	while (expression.contains("+")) {
                    	simpleExpression = oneAtATime(expression, '+');
                        String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                        expression=expression.replace(simpleExpression, evaluatedValue);
                    }
                } 
                else if (plusIndex < subIndex) {
                	simpleExpression = oneAtATime(expression, '+');
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                    expression=expression.replace(simpleExpression, evaluatedValue);
                } else {
                	simpleExpression = oneAtATime(expression, '-');
                    String evaluatedValue = simpleExpressionCalculator(simpleExpression);
                    expression = expression.replace(simpleExpression, evaluatedValue);
                }
            }
        }
        return expression;
	}
	
	// this method will be called several times by calculate to break up each operator
	private String oneAtATime(String expression, char operator) {
		// will be of the form: "first#" "operator" "second#"
		String simplerExpression;
		
		int posOfOperator = expression.indexOf(operator);
		
		//get the first operand
		String firstOperand = expression.substring(0, posOfOperator);
		while(   firstOperand.contains("r") == true || firstOperand.contains("^") == true || firstOperand.contains("*") == true || firstOperand.contains("/") == true || firstOperand.contains("+") == true || firstOperand.contains("-") == true ){
			if( firstOperand.lastIndexOf('r') != -1)
				firstOperand = firstOperand.substring(firstOperand.lastIndexOf('r')+1, firstOperand.length());

            if( firstOperand.lastIndexOf('^') != -1)
            	firstOperand = firstOperand.substring(firstOperand.lastIndexOf('^')+1, firstOperand.length());

            if( firstOperand.lastIndexOf('*') != -1)
            	firstOperand = firstOperand.substring(firstOperand.lastIndexOf('*')+1, firstOperand.length());

            if( firstOperand.lastIndexOf('/') != -1)
            	firstOperand = firstOperand.substring(firstOperand.lastIndexOf('/')+1, firstOperand.length());

            if( firstOperand.lastIndexOf('+') != -1)
            	firstOperand = firstOperand.substring(firstOperand.lastIndexOf('+')+1, firstOperand.length());

            if( firstOperand.lastIndexOf('-') != -1)
            	firstOperand = firstOperand.substring(firstOperand.lastIndexOf('-')+1, firstOperand.length());
	    }
		
		//get the second operand
		String secondOperand = expression.substring(posOfOperator+1, expression.length());
        while(secondOperand.contains("r") == true || secondOperand.contains("^") == true || secondOperand.contains("*") == true || secondOperand.contains("/") == true || secondOperand.contains("+") == true || secondOperand.contains("-") == true )
        {
            if( secondOperand.indexOf('r') != -1)
            	secondOperand = secondOperand.substring(0,secondOperand.indexOf('r'));

            if( secondOperand.indexOf('^') != -1)
            	secondOperand = secondOperand.substring(0,secondOperand.indexOf('^'));

            if( secondOperand.indexOf('*') != -1)
            	secondOperand = secondOperand.substring(0,secondOperand.indexOf('*'));

            if( secondOperand.indexOf('/') != -1)
            	secondOperand = secondOperand.substring(0,secondOperand.indexOf('/'));

            if( secondOperand.indexOf('+') != -1)
            	secondOperand = secondOperand.substring(0,secondOperand.indexOf('+'));
            
            if( secondOperand.indexOf('-') != -1)
            	secondOperand = secondOperand.substring(0,secondOperand.indexOf('-'));
        }
        
        
        simplerExpression = firstOperand + operator + secondOperand;
        simplerExpression.replaceAll("n", "-");
        
		return simplerExpression;
	}

	// most of this method was given by prof... it will be called many times by calculate method
	private String simpleExpressionCalculator (String expression) {
		//at this point we can change the "u"s to "-"s
		int i;
		char operator = ' ';
		//find operator
		for (i = 0; i < expression.length(); i++) { // allow leading unary!
	    
		if((expression.charAt(i) == '^')
		 ||(expression.charAt(i) == 'r')
		 ||(expression.charAt(i) == '*')
		 ||(expression.charAt(i) == '/')
		 ||(expression.charAt(i) == '+')
		 ||(expression.charAt(i) == '-')){
		    operator = expression.charAt(i);
			break;
		   }
	    }
		expression = expression.replace('u', '-');
		// find the operands
		String leftOperand  = expression.substring(0,i).trim();
		String rightOperand = expression.substring(i+1).trim();
		
		double leftNumber = 0;
		double rightNumber = 0;
		try {
	 	    leftNumber = Double.parseDouble(leftOperand);
		}
	 	catch(NumberFormatException nfe){
	 		System.out.println("Error breaking up the expression:" + expression
	 				+ "\nLeft operand.");
	 	    }
	 	
	 	try {
	 	    rightNumber = Double.parseDouble(rightOperand);
	 	    }
	 	catch(NumberFormatException nfe)
	 	    {
	 		System.out.println("Error breaking up the expression:" + expression
	 				+ "\nRight operand.");
	 	    }
		
		// evaluate the expression
		String value = expression;
		
		switch(operator) { // char or int 
			case '+' : value = Double.toString(leftNumber + rightNumber); break;
			case '-' : value = Double.toString(leftNumber - rightNumber); break;
			case '*' : value = Double.toString(leftNumber * rightNumber); break;
			case '/' : value = Double.toString(leftNumber / rightNumber); break;
			case '^' : value = Double.toString(Math.pow(leftNumber,rightNumber)); break;
			case 'r' : value = Double.toString(Math.pow(leftNumber,1/rightNumber)); break;
		}

		
		return value = value.replace('-', 'u');
	}
	
	// this is used to substitute in for the x variable
	private String xVariableSubstitution(String expression) {
		String xVariable = valueForXField.getText().trim(); 
		//get the x value here:
		// xVariable = -----
		expression = expression.replaceAll("x", xVariable);
		return expression;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	//GUI METHODS
	public void actionPerformed(ActionEvent ae)
	  {
		if (ae.getSource() == clearButton)  clearAreas();
		if(ae.getSource() == enterExpressionField) enterHasBeenPressed(); 
		if(ae.getSource() == valueForXField) enterHasBeenPressed(); 
		if(ae.getSource() == recallButton) recallLast(); 
		if(ae.getSource() == graphButton) graphButtonMethod(); 
		
	  }
	
	private void graphButtonMethod() {
		double xStep = -1;
		String newLine = System.lineSeparator();
		String expression = enterExpressionField.getText().trim();
		expression = expression.replaceAll(" ", "");
		lastMessage = enterExpressionField.getText(); //last thing entered in log area 
		if (!isXThere()) {
			errorMessagesField.setText("There must be an expression with 'x' in it");
			return;
			}
		if(!valueInTheXField()) {
			errorMessagesField.setText("Specify a starting value for x");
			return;
		}
		if (xIncrementField.getText().trim().length() == 0) {
			errorMessagesField.setText("Please specify a step value for x");
			return;
		}
		xStep =  Double.parseDouble(xIncrementField.getText());
		if (xStep<0) {
			errorMessagesField.setText("Step value for x must be a positive number");
			return;
		}
		if (isXThere()&&valueInTheXField()&&(xIncrementField.getText().trim().length() != 0)) {
			if (checkParenthesesErrors(expression, 1) == 1)
				return;
			if (checkForOtherErrors(expression, 1) == 1)
				return;
			errorMessagesField.setText("");
			logArea.append(newLine + "Graphing \"" + expression + "\"..."); 
			graphTheExpression(expression);
		}
	}
	
	private void graphTheExpression(String expression) {
		
		double[] x = new double[11];
		double[] y = new double[11];
		double startingValue = Double.parseDouble(valueForXField.getText());
		double incrementValue = Double.parseDouble(xIncrementField.getText());
		
		
		System.out.println("\nStarting value: " + startingValue);
		System.out.println("Increment by: " + incrementValue);
		System.out.println("Equation: " + expression);
		
		//calculate x point values
	   	 x[0] = startingValue;
	   	 for( int i=1 ; i<11 ; i++){
	   		 x[i] = x[i-1] + incrementValue;
	   	 }
	   	 //calculate y point values
	   	 for( int i=0 ; i<11 ; i++){
	   		 String xValueString = Double.toString(x[i]);
	   		 String yValueCalc = expression.replaceAll("x",xValueString);
	   		 y[i] = Double.parseDouble(calculate(yValueCalc));
	   	 }
	   	 System.out.println("\nx point values= " + Arrays.toString(x) + "\n");
	   	 System.out.println("y point values= " + Arrays.toString(y) + "\n");
	   	 System.out.println("Sending these arrays to drawPlot method.");
	   	 
	   	 drawPlot(expression, x, y);
	   	 return;
	}
	
	double[] X;
	double[] Y;
	
	private void drawPlot(String expression, double[] x, double[] y) {
		X = x;
		Y = y;
	    Thread t = new Thread(this); // create a Thread  
        t.start();
	}
	
	public void run() {
		System.out.println("HI");
		
		JFrame graphWindow = new JFrame("Plot of the Expression: " );
		JPanel plotPanel  = new JPanel();
		Graphics g;
		
		graphWindow.setSize(800,600); // width, height (in "pixels"!)
		graphWindow.getContentPane().add(plotPanel, "Center");
		graphWindow.setVisible(true); // show it!
		
		plotPanel.setBackground(Color.white);
		System.out.println("\nplotPanel width =  " + plotPanel.getWidth());
	    System.out.println("\nplotPanel height =  " + plotPanel.getHeight());
	    g = plotPanel.getGraphics();
	    g.setFont(new Font("default", Font.BOLD, 15));
		while (true) {
			int xMid = plotPanel.getWidth()/2;
			int yMid = plotPanel.getHeight()/2;
			int xMax = plotPanel.getWidth();
			int yMax = plotPanel.getHeight();
			
			for(int i = 0;i<11;i++) {
				g.fillRect((int)X[i], yMax-(int)Y[i], 2, 2);
			}
			g.setColor(Color.black);
			g.drawString("y-axis",xMid +10,15);
			g.drawString("x-axis",0,yMid);
			g.drawLine(0, yMid,  plotPanel.getWidth(),yMid);
		    g.fillRect(0, yMid, plotPanel.getWidth(), 5);//x,y,width,height  
		    g.fillRect(xMid, 0,5, plotPanel.getWidth());//x,y,width,height  
		}	
	}

	
	private void recallLast() 
	{
		enterExpressionField.setText(lastMessage);
		
	}

	private void clearAreas() //clears all areas except the log area 
	{
		valueForXField.setText("");
		enterExpressionField.setText("");
		errorMessagesField.setText("");
		xIncrementField.setText("");
		
	}

	private void expressionEntered()
	{
		if(isXThere()) //if x is present in the expression
		{
			if(!valueInTheXField()) //and there is no value in the valueForXField
			{
				System.out.println("You must set a value for x if it is in the expression!");
				errorDetected1 = true; 
			}
			
					
			
		}
		if(!isXThere()) //if x is not present in the expression 
		{
			if(valueInTheXField()) //and there is a value in the valueForXField
			{
				System.out.println("Do not set a value for x if it is not in the expression.");
				errorDetected2 = true; 
			}
			
		}
		
		
				
		
	}
	
	private void enterHasBeenPressed()
	{
		expressionEntered(); 
		String newLine = System.lineSeparator();
		if(!errorDetected1 && !errorDetected2) {
			lastMessage = enterExpressionField.getText(); //last thing entered in log area 
	        logArea.setCaretPosition(logArea.getDocument().getLength()); // scroll to bottom
	        //valueForXField.setText(" ");
			//enterExpressionField.setText(" "); 
	       
				
	        String enteredExpression = enterExpressionField.getText().trim().toLowerCase();
	        
	        String expression = enteredExpression.replaceAll(" ","");
				
				
				// call xVariableSubstitution to sub in variable
				if (expression.contains("x")) 
				expression = xVariableSubstitution(expression);
				
				// substitute values for pi and e
				expression = expression.replace("pi", String.valueOf(Math.PI));
				expression = expression.replace("e", String.valueOf(Math.E));
				
				boolean isError = false;
				
				// Call error checking methods!
				if (checkParenthesesErrors(expression, 0) == 1)
					isError = true;
				if (checkForOtherErrors(expression, 0) == 1)
					isError = true;
				//System.out.println("To be evaluated = " + expression);
				
				if (!isError) {
				// Call method to remove unary
				expression = unaryHandler(expression);
				
				String finalAnswer = calculate(expression);
				finalAnswer = finalAnswer.replace('u', '-');
			
			    // print answer
			 	System.out.println("\nANSWER:\n"+enteredExpression + " = " + finalAnswer);
			 	logArea.append(newLine + enterExpressionField.getText()+ " = " + finalAnswer); 
			 	clearAreas(); //clears everything
			}
				 
		}
		if(errorDetected1)
		{
			errorMessagesField.setText("You must set a value for x if it is in the expression!");
			//valueForXField.setText(" ");
			errorDetected1 = false; 
		}
		if(errorDetected2)
		{
			errorMessagesField.setText("Do not set a value for x if it is not in the expression."); 
			//valueForXField.setText(" ");
			errorDetected2 = false; 
		}
		
	}
	
	private boolean isXThere()
	{
		if(enterExpressionField.getText().trim().toLowerCase().contains("x") == true)
		{
			return true;
		}
			return false; 		
	}
	
	private boolean valueInTheXField()
	{
		if(valueForXField.getText().trim().length() == 0)
		{
			return false; 
		}
		return true; 
	}
}