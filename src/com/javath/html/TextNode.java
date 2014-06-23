package com.javath.html;

import java.util.ArrayList;
import java.util.Stack;


import org.w3c.dom.Node;

import com.javath.util.Instance;

public class TextNode extends Instance {
	
	private char left_quote;
	private char right_quote;
	private char delimiter;

	private String[][] string_array;	
	private Stack<String[]> stack = new Stack<String[]>();
	
	public TextNode() {
		this.setQuote("{}");
		this.setDelimiter('|');
	}
	public TextNode(Node node) {
		this();
		this.convert(node);
	}
	
	public void setQuote(String quote) {
		left_quote = quote.charAt(0);
		right_quote = quote.charAt(1);
	}
	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}
	
	public synchronized void convert(Node node) {
		stack.clear();
		stack.push(this.convert(0, node));
		this.reOrder();
	}
	private String[] convert(int depth, Node node) {
		Node child = node.getFirstChild();
		if (node.getNodeName().equals("#text")) {
			String text = node.getNodeValue();
			if (text == null)
				return new String[] {String.valueOf(depth) ,""};
			else
				return new String[] {String.valueOf(depth) , text.trim()};
		}
		if (node.getNodeName().equals("BR"))
			return new String[] {String.valueOf(depth) ,String.format("%s%s%s",left_quote, "BR", right_quote)};
		
		ArrayList<String> array = new ArrayList<String>();
		array.add(String.valueOf(depth));
		if (child == null)
			array.add("");
		while (child != null) { 
			String[]  childArray = convert(depth + 1, child);
			//printStringArray(childArray);
			if (childArray.length == 2) {
				array.add(childArray[1]);
			} else {
				//printStringArray(childArray);
				stack.push(childArray);
				array.add(String.format("%s%d%s",left_quote, depth + 1, right_quote));
			}
			child = child.getNextSibling();
		}
		String[] string = array.toArray(new String[] {});
		return string;
	}
	private void reOrder() {
		string_array = new String[stack.size()][];
		int current = stack.size() - 1;
		for (int index = current; index > -1; index--)
			string_array[index] = stack.pop();
		while (current > 0) {
			boolean loop = true;
			int source = current;
			while (loop) {
				int target = source - 1;
				if (target == -1)
					break;
				if (Integer.valueOf(string_array[source][0]) < 
						Integer.valueOf(string_array[target][0]))  {
					String[] temp = string_array[target];
					string_array[target] = string_array[source];
					string_array[source] = temp;
					source -= 1;
				} else
					loop = false;
			}
			if (Integer.valueOf(string_array[current][0]) >= 
					Integer.valueOf(string_array[current - 1][0]))
				current -= 1;
		}
		//System.out.println("Array");
		//for (int index = 0; index < stringArray.length; index++)
		//	printStringArray(stringArray[index]);
	}
	
	public void printStringArray(String[] object) {
		if (object.length == 0)
			System.out.print(left_quote);
		else
			System.out.print(left_quote + object[0].toString());
		for (int index = 1; index < object.length - 1; index++)
			System.out.print(delimiter + object[index].toString());
		if (object.length <= 1)
			System.out.println(right_quote);
		else
			System.out.println(delimiter + object[object.length - 1].toString() + right_quote);
	}
	public void printStringArray(int row) {
		printStringArray(getStringArray(row));
	}
	public void print() {
		for (int index = 0; index < string_array.length; index++)
			printStringArray(index);
	}
	
	public int length() {
		return string_array.length;
	}
	public String[] getStringArray(int row) {
		return string_array[row];
	}
	public String getString(int row, int column) {
		return string_array[row][column];
	}
	
}
