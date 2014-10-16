package com.fleety.base.ext;

import java.util.LinkedList;
import java.util.ArrayList;
/**
 * 公式计算器类
 * 1.用于判别一个字符串是否是一个正确的普通数值计算的表达式（包含数字0-9、加减乘除、括号）
 * 2.用于得到一个正确表达式的计算结果
 * @author zhh *
 */
public class ExtFormulaCalculator {

	public static double getFormulaResult(String formula) throws Exception {
		return doAnalysis(formula);
	}
	public static boolean isRightFormula(String formula){
		try {
			doAnalysis(formula);
		} catch (Exception e) {
//			e.printStackTrace();
			return false;
		}
		return true;
	}
	private static double doAnalysis(String formula) throws Exception {

		LinkedList stack = new LinkedList();

		int curPos = 0;
		String beforePart = "";
		String afterPart = "";
		String calculator = "";
		while ((formula.indexOf('(') >= 0 || formula.indexOf(')') >= 0)) {
			curPos = 0;
			char []CArray=formula.toCharArray();
			char s;
			for (int i=0;i<CArray.length;i++) {
				s=CArray[i];
				if (s == '(') {
					stack.add(new Integer(curPos));
				} else if (s == ')') {
					if (stack.size() > 0) {
						beforePart = formula.substring(0, ((Integer)stack.getLast()).intValue());
						afterPart = formula.substring(curPos + 1);
						calculator = formula.substring(((Integer)stack.getLast()).intValue() + 1,
								curPos);
						formula = beforePart + doCalculation(calculator)
								+ afterPart;
						stack.clear();
						break;
					} else {
						throw new Exception("有未关闭的右括号！请检查:" + formula);
					}
				}
				curPos++;
			}
			if (stack.size() > 0) {
				throw new Exception("有未关闭的左括号！请检查:" + formula);
			}
		}
		return doCalculation(formula);
	}

	private static double doCalculation(String formula) throws Exception{
		ArrayList values = new ArrayList();
		ArrayList operators = new ArrayList();
		int curPos = 0;
		int prePos = 0;
		char []CArray=formula.toCharArray();
		char s;
		for (int i=0;i<CArray.length;i++) {
			s=CArray[i];
			if (s == '+' || s == '-' || s == '*' || s == '/') {
				values.add(new Double((formula.substring(prePos, curPos)
						.trim())));
				operators.add("" + s);
				prePos = curPos + 1;
			}
			curPos++;
		}
		values.add(new Double(formula.substring(prePos).trim()));
		char op;		
		for (curPos = 0; curPos <operators.size(); ) {
			op = ((String)operators.get(curPos)).charAt(0);
			switch (op) {
			case '*':
				values.add(curPos, new Double(((Double)values.get(curPos)).doubleValue() * ((Double)values.get(curPos + 1)).doubleValue()));
				values.remove(curPos + 1);
				values.remove(curPos + 1);
				operators.remove(curPos);
				break;
			case '/':
				values.add(curPos, new Double(((Double)values.get(curPos)).doubleValue() / ((Double)values.get(curPos + 1)).doubleValue()));
				values.remove(curPos + 1);
				values.remove(curPos + 1);
				operators.remove(curPos);
				break;
			default:
				curPos++;
			}	
		}
		for (curPos = 0; curPos <operators.size();) {
			op = ((String)operators.get(curPos)).charAt(0);
			switch (op) {
			case '+':
				values.add(curPos, new Double(((Double)values.get(curPos)).doubleValue() + ((Double)values.get(curPos + 1)).doubleValue()));
				values.remove(curPos + 1);
				values.remove(curPos + 1);
				operators.remove(curPos);
				break;
			case '-':
				values.add(curPos, new Double(((Double)values.get(curPos)).doubleValue() - ((Double)values.get(curPos + 1)).doubleValue()));
				values.remove(curPos + 1);
				values.remove(curPos + 1);
				operators.remove(curPos);
				break;
			default:
				curPos++;
			}
		}
		return ((Double)values.get(0)).doubleValue();
	}

	public static void main(String args[]) {
		try {
			System.out.println(ExtFormulaCalculator.getFormulaResult("100-(25*1+6*4/2)+24"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
