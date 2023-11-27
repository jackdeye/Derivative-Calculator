import java.util.Scanner;
import java.util.ArrayList;
import java.util.Stack;
/* Created by Jack Deye, 6/11/2023
 * Prompts users to input a mathematical expression, then returns its derivative in a relatively simplified form
 *   
 */
public class Derivative_Explorer {

/*
	Method Directory
	Name:       | Input:    |  output
	simplify    | postfix   | postfix
	derivative  | postfix   | postfix
	getDerivRPN | prefix    | prefix
	breakL      | prefix    | prefix
	breakR      | prefix    | prefix
	getRPN      | string    | postfix
 */
	@SuppressWarnings("unchecked")
	public static ArrayList<String> oneStepSimplify(ArrayList<String> s){
		
		ArrayList<String> sDup = (ArrayList<String>) s.clone();
		ArrayList<String> sDupL, 
						  sDupR,
						  answer = new ArrayList<>(),
						  zero = new ArrayList<>();
		zero.add("0");
		String operator = sDup.get(sDup.size()-1);	
		for(int i = sDup.size()-1; i>=0; i--) {
			if(!isOperator(sDup.get(i))) {
				continue;
			} else {
				operator = sDup.get(i);
				//ik this looks bad but if the rpn is [{A},{B},operator]
				//sDupL will be {A}, and sDupR will be {B}
				sDupL = reverse(breakL(reverse((new ArrayList<String>((sDup).subList(0,i+1))))));
				sDupR = reverse(breakR(reverse((new ArrayList<String>((sDup).subList(0,i+1))))));	
			}
			
			if(sDupR.size()==1&&isNumeric(sDupR.get(0))&&
			   isNumeric(sDupL.get(sDupL.size()-1))){
				
				String eval = operate(sDupL.get(sDupL.size()-1),sDupR.get(0),operator);
				sDupL.remove(sDupL.size()-1);
				for(String str : sDupL) {
					answer.add(str);
				}
				answer.add(eval);
				for(int j = i+1; j<sDup.size();j++) {
					answer.add(sDup.get(j));
				}
				return answer;
			}
			
			if(operator.equals("+")) {
				if(sDupL.get(sDupL.size()-1).equals("0")||sDupL.get(sDupL.size()-1).equals("0.0")) {
					sDupL.remove(sDupL.size()-1);
					for(String str : sDupL) {
						answer.add(str);
					}
					for(String str : sDupR) {
						answer.add(str);
					}
					for(int j = i+1; j<sDup.size();j++) {
						answer.add(sDup.get(j));
					}
					return answer;
				}
				if(sDupR.size()==1&&sDupR.get(0).equals("0")||sDupR.size()==1&&sDupR.get(0).equals("0.0")) {
					for(String str : sDupL) {
						answer.add(str);
					}
					for(int j = i+1; j<sDup.size();j++) {
						answer.add(sDup.get(j));
					}
					return answer;
				}
				
			}
			if(operator.equals("-")) {
				if(sDupR.size()==1&&sDupR.get(0).equals("0")||sDupR.size()==1&&sDupR.get(0).equals("0.0")) {
					for(String str : sDupL) {
						answer.add(str);
					}
					for(int j = i+1; j<sDup.size();j++) {
						answer.add(sDup.get(j));
					}
					return answer;
				}
			}
			if(operator.equals("*")) {
				if(sDupL.get(sDupL.size()-1).equals("0")||sDupL.get(sDupL.size()-1).equals("0.0")) {
					for(String str : sDupL) {
						answer.add(str);
					}
					for(int j = i+1; j<sDup.size();j++) {
						answer.add(sDup.get(j));
					}
					return answer;
				}
				
				//The reason why the following exists is because the example 2*X+(X-1)*0 -> [2,X,*,X,1,-,0,*,+] 
				// when the loop gets to * as the operator, sDupR is "0", 
				//and sDupL is [2,X,*,X,1,-], however we only want the [2,X,*]
				//so the first for loop figures out where what we are multiplying by zero ends,
				//and where the new part of the function starts
				if(sDupR.size()==1 && sDupR.get(0).equals("0")||
				   sDupR.size()==1 && sDupR.get(0).equals("0.0")) {
					int cutIndex = -1;
					int count = 0;
					for(int j = sDupL.size()-1; j>=0; j--){
						if(count<0) {
							cutIndex=j;
							break;
						}
						if(isOperator(sDupL.get(j))){
							count++;
						} else {
							count--;
						}
					}
					
					for(int j = 0; j<=cutIndex; j++) {
						answer.add(sDup.get(j));
					}
					
					answer.add("0");
					
					for(int j = i+1; j<sDup.size();j++) {
						answer.add(sDup.get(j));
					}
					return answer;
				}
			}
		}
		return s;
	}	@SuppressWarnings("unchecked")
	public static ArrayList<String> totalSimplify(ArrayList<String> s){
		ArrayList<String> prev = (ArrayList<String>) s.clone();
		ArrayList<String> change = new ArrayList<>(oneStepSimplify(prev));
		
		while(!(change.equals(prev)||change.size()==1)) {
			prev=change;
			//To view every step of the simplification, uncomment below
			//System.out.println(getInfix(prev));
			change = oneStepSimplify(change);
		}
		return change;
	}
	public static boolean isNumeric(String str){
	    for (char c : str.toCharArray()){
	        if (!(Character.isDigit(c)||c=='.')) return false;
	    }
	    return true;
	}
	public static ArrayList<String> derivative(ArrayList<String> list, String res){
		return reverse(getDerivRPN(reverse(list),res));
	}
	
	//must take in prefix
	@SuppressWarnings("unchecked")	
	public static ArrayList<String> getDerivRPN(ArrayList<String> list, String res){
		
		ArrayList<String> temp = new ArrayList<>();
		if(list.size()==0) return temp;
		
		if(list.size()==1) {
			if(Character.isDigit(list.get(0).charAt(0))) {
				temp.add("0");
				return temp;
			}
			if(Character.isLetter(list.get(0).charAt(0))) {
				temp.add("1");
				return temp;
			}
		}
		
		String operator = list.get(0);
		ArrayList<String> list2 = (ArrayList<String>) list.clone();
		ArrayList<String> list1d = (ArrayList<String>) list.clone();
		ArrayList<String> list2d = (ArrayList<String>) list.clone();
		
		//derivative sum rule
		if(operator.equals("+")||operator.equals("-")) {
			temp.add(operator);
			temp.addAll(getDerivRPN(breakR(list1d),res));
			temp.addAll(getDerivRPN(breakL(list2d),res));
			return temp;
		}
		//derivative product rule
		if(operator.equals("*")) {
			temp.add("+");
			temp.add("*");
			temp.addAll(getDerivRPN(breakR(list1d),res));
			temp.addAll(breakL(list2));
			temp.add("*");
			temp.addAll(getDerivRPN(breakL(list2d),res));
			temp.addAll(breakR(list));
			return temp;
		}
		if(operator.equals("^")) {
			//d/dx(x^a)=a*x^(a-1)*d/dx
			
			if(Character.isDigit(list.get(1).charAt(0))){
			temp.add("*");
			temp.add("*");
			temp.add("^");
			temp.add((Integer.valueOf(list.get(1))-1)+"");
			temp.addAll(breakL(list2));
			temp.add(list.get(1));
			temp.addAll(getDerivRPN(breakL(list1d),res));
			return temp;
			}
		}
		
		/*
		f/g
		[/, g, f]
		(g*df-f*gd)/(g^2)
		[/, ^, 2, g, -, *, gd, f, *, df, g] 
		 */
		
		if(operator.equals("/")) {
			temp.add("/");
			temp.add("^");
			temp.add("2");
			temp.addAll(breakR(list2));
			temp.add("-");
			temp.add("*");
			
			temp.addAll(getDerivRPN(breakR(list2d),res));
			temp.addAll(breakL(list));
			
			temp.add("*");
			
			temp.addAll(getDerivRPN(breakL(list1d),res));
			temp.addAll(breakR(list2));
			return temp;
		}
		return temp;
	}
	public static ArrayList<String> reverse(ArrayList<String> list) {
	    for(int i = 0, j = list.size() - 1; i < j; i++) {
	        list.add(i, list.remove(j));
	    }
	    return list;
	}
	public static int numOperators(ArrayList<String> s) {
		int count = 0;
		for(String str: s) {
			if(isOperator(str)) count++;
		}
		return count;
	}
	public static void checkOperators(String str) throws Exception{
		if( str.charAt(str.length()-1)=='+'||
			str.charAt(str.length()-1)=='-'||
			str.charAt(str.length()-1)=='*'||
			str.charAt(str.length()-1)=='/'||
			str.charAt(str.length()-1)=='^') {
			throw new Exception("Cannot end with an oporator");
			}
	}	//must take expression in postfix 
	@SuppressWarnings("unchecked")	
	public static ArrayList<String> breakR(ArrayList<String> seq){
		ArrayList<String> seqDupe = (ArrayList<String>) seq.clone();
		ArrayList<String> temp = new ArrayList<>();
		if(seqDupe.size()==0) return null;
		if(!isOperator(seqDupe.get(0))){
			return temp;
		} else 
			seqDupe.remove(0);
			int count = 0;
			while(count>=0) {
				if(isOperator(seqDupe.get(0))) {
					count++;
					temp.add(seqDupe.remove(0));
				} else {
					count--;
					temp.add(seqDupe.remove(0));	
				}
			}
		return temp;
	}
	// must take expression postfix
	@SuppressWarnings("unchecked")
	public static ArrayList<String> breakL(ArrayList<String> seq){
		ArrayList<String> temp2 = (ArrayList<String>) seq.clone();
		ArrayList<String> temp = new ArrayList<>();
		if(temp2.size()==0) return null;
		if(!isOperator(temp2.get(0))){
			return temp;
		} else { 
			int left = breakR(temp2).size();
			for(int i = 0; i<left+1;i++) {
				temp2.remove(0);
			}
			return temp2;
		}
	}
	public static boolean isNumber(String str) {
		if(isOperator(str)||
			str.charAt(str.length()-1)=='('||
			str.charAt(str.length()-1)==')') {
				return false;
		}
		return true;
	}
	//PEMDAS
	public static int getPrecedence(String str) {
		switch(str.charAt(str.length()-1)) {
			case '^': return 4; 
			case '*': return 3; 
			case '/': return 3;
			case '+': return 2;
			case '-': return 2;
		}
		return 0;
	}
	//The associative property is different for exponents and it is easier to make a method
	public static boolean getAss(String op) {
		if((op.charAt(op.length()-1)=='^')) return true;
		else return false;
	}
	public static String operate(String prev, String s2, String op) {
		double num1 = Double.valueOf(prev);
		double num2 = Double.valueOf(s2);
		Double val = 0.0;
		
		switch(op.charAt(op.length()-1)) {
			case '^': val= Math.pow(num1,num2); break;
			case '*': val= num1*num2; break;
			case '/': val= num1/num2; break;
			case '+': val= num1+num2; break;
			case '-': val= num1-num2; break;
		}
		return ""+val;
	}
	public static ArrayList<String> getRPN(String s){
		//gets rid of spaces
		for(int i = 0; i<s.length();i++) {
			if(s.substring(i, i+1).equals(" ")) {
				s = s.substring(0, i) + s.substring(i+1, s.length());
				i--;
			}	
		}	
		//checks that it does not end with an operator
		try { checkOperators(s);}
		catch (Exception e) { System.out.println("Can not put operators at the end");}	
		//creates an arraylist of components
		ArrayList<String> seq = new ArrayList<>();
		int prev = 0; 
		for(int i=0; i<s.length(); i++) {
			if(s.charAt(i)=='-'||s.charAt(i)=='+'||
			   s.charAt(i)=='*'||s.charAt(i)=='/'||
			   s.charAt(i)=='('||s.charAt(i)==')'||
			   s.charAt(i)=='^') {
				
				if(prev!=i) seq.add(s.substring(prev,i));
				seq.add(s.substring(i,i+1));
				prev = i+1;
			} 
		}
		
		if(!isParens(seq.get(seq.size()-1))) seq.add(s.substring(prev));
		if(isOperator(seq.get(seq.size()-1))) throw new Error("Can not end with an operator"); 
		
		for(int i = 0;i<seq.size();i++) {
			if(seq.get(i)==null) {
				seq.remove(i);
				i--;
			}
		}
		ArrayList<String> straight = new ArrayList<>();
		Stack<String> down = new Stack<String>();
		//first half
		while(true) {
			if(seq.size()==0) break;
			String str = seq.get(0);
			//right Parentheses rule;
			if(isRightParens(str)) {
				boolean found = false;
				while(!found) {
					if(!down.isEmpty()&&isLeftParens(down.peek())) {
						down.pop();
						seq.remove(0);
						break;
					}
					straight.add(down.pop());
				}
				continue;
			}
			//just a number rule
			if(isNumber(str)) {
				straight.add(str);
				seq.remove(0);
				continue;
			}
			//Left parentheses rule
			if(isLeftParens(str)) {
				down.push(str);
				seq.remove(0);
				continue;
			}
			//Simple operator rule
			if(isOperator(str)&&down.isEmpty()) {
				down.push(str);
				seq.remove(0);
				continue;
			}
			if(isOperator(str)&&!isOperator(down.peek())) {
				down.push(str);
				seq.remove(0);
				continue;
			}
			//Precedence Operator rule
			//Wiki:there is an operator o2 at the top of the operator stack which is not a left parenthesis, (cant be bc isOperator doesnt return true for Parens)
			if(isOperator(str)&&isOperator(down.peek())) {
		 
            //and (o2 has greater precedence than o1 or (o1 and o2 have the same precedence and o1 is left-associative))
				String o1 = str;
				String o2 = down.peek();
				int prec1 = getPrecedence(o1);
				int prec2 = getPrecedence(o2);
				while(!down.isEmpty()&&!isLeftParens(o2)&&((prec2>prec1)||(prec1==prec2&&!getAss(o1)))) {
					straight.add(down.pop());
					if(!down.isEmpty()) {
						o2 = down.peek();
						prec2 = getPrecedence(o2);
					}
				}
				down.push(str);
				seq.remove(0);
				continue;
			}	
		}
		
		while(!down.isEmpty()) {
			straight.add(down.pop());
		}
		return straight;
	}
	public static String evaluate(String s) {
		ArrayList<String> straight = getRPN(s);
		Stack<String> eval = new Stack<>();
		while(straight.size()>0) {
			if(!isOperator(straight.get(0))) {
				eval.push(straight.remove(0));
				continue;
			}else {
				String s1 = eval.pop();
				String s2 = eval.pop();
				eval.push(operate(s2,s1,straight.get(0)));
				straight.remove(0);
			}			
		}
		return eval.peek();
	}	
	public static String plugIn(String exp, String val, Double d) {
		int index;
		for(int i = 0; i<exp.length(); i++) {
			index = exp.indexOf(val);
			if(index!=-1)exp = exp.substring(0,index)+d+exp.substring(index+val.length());	
		}
		return exp;
	}	
	public static String dumbDerivative(String exp, String var) {
		int index = var.indexOf("=");
		String variable = var.substring(0,index);
		Double val = Double.valueOf(var.substring(index+1));
		Double top = Double.valueOf(evaluate(plugIn(exp,variable,val+0.0000001)))-Double.valueOf(evaluate(plugIn(exp,variable,val)));
		
		return top/0.0000001+"";
	}
	public static boolean isOperator(String str) {
		if(str.charAt(str.length()-1)=='+'|| str.charAt(str.length()-1)=='-'||
		   str.charAt(str.length()-1)=='*'|| str.charAt(str.length()-1)=='/'||
		   str.charAt(str.length()-1)=='^') {
				return true;
		}
		return false;
	}
	public static boolean isLeftParens(String str) {
		if(str.charAt(str.length()-1)=='(') {
				return true;
		}
		return false;
	}
	public static boolean isRightParens(String str) {
		if(str.charAt(str.length()-1)==')') {
				return true;
		}
		return false;
	}
	public static boolean isParens(String str) {
		if(str.charAt(str.length()-1)=='('||str.charAt(str.length()-1)==')') {
				return true;
		}
		return false;
	}
	//from postfix to infix
	public static String getInfix(ArrayList<String> exp) {
	    if(exp.size()==1) return exp.get(0);
		Stack<String> s = new Stack<String>();
	    for (int i = 0; i < exp.size(); i++){
	        // Push operands
	        if (!isOperator(exp.get(i))) {
	        	s.push(exp.get(i) + "");
	        } else {
	            String op1 = s.peek();
	            s.pop();
	            String op2 = s.peek();
	            s.pop();
	            s.push("(" + op2 + exp.get(i) +
	                    op1 + ")");
	        }
	    }
	    String str = s.peek();
	    return str.substring(1,str.length()-1);
	}	
	public static void main(String[] args) {
		System.out.println("Derivative Calculator V1.0");
		System.out.println("Notes:");
		System.out.println("1. Currently only supports + - * / ^ operators as well as parenthesis");
		System.out.println("2. Use * for all multiplication");
		System.out.println("3. Exponents must be of the form f(x)^a");
		System.out.println("4. Type \"finish\" in expression slot to end");
		
		Scanner n = new Scanner(System.in);
		String expression;
		String respect;
		String answer =null;
		double val;
		while(true) {
			System.out.print("Please input the expression: ");
			expression = n.nextLine();
			if(expression.toLowerCase().equals("finish")) break;
			
			if(expression.toLowerCase().equals("evaluate")) {
				System.out.println("What is the variable: ");
				respect = n.nextLine();
				System.out.print("Evaluate at "+respect+"=? ");
				val = Double.valueOf(n.nextLine());
				System.out.println(plugIn(answer, respect, val)+"="+evaluate(plugIn(answer, respect, val)));
				continue;
			} else {
				System.out.print("With respect to: ");
				respect = n.nextLine();
			}
			answer = getInfix(totalSimplify(derivative(getRPN(expression),respect)));
			System.out.println(answer);
			System.out.println();
			//System.out.println(reverse(getRPN(expression)));
			//System.out.println(getInfix(derivative(getRPN(expression),respect)));
			//System.out.println(getInfix(totalSimplify(getRPN(expression))));
		}
		n.close();
	}
}