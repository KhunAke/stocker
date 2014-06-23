package com.javath.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextSpliter extends Instance {

	private BufferedReader reader;
	private boolean fixed;
	private int[] positions;
	private String delimiter = "\\s";
	
	public TextSpliter(InputStream input_stream, boolean fixed) {
		this(new BufferedReader(new InputStreamReader(input_stream)), fixed);
		//Integer[] a =  fields.toArray(new Integer[] {});
	}
	public TextSpliter(BufferedReader reader, boolean fixed) {
		this.reader = reader;
		setFixed(fixed);
		//Integer[] a =  fields.toArray(new Integer[] {});
	}
	
	public TextSpliter setFixed(boolean fixed) {
		this.fixed = fixed;
		return this;
	}
	public TextSpliter setPositions(int[] positions) {
		this.positions = positions;
		if (!fixed)
			throw new ObjectException("fixed mode");
		return this;
	}
	public TextSpliter setDelimiter(String delimiter) {
		this.delimiter = delimiter;
		return this;
	}
	
	public String[] readLine() throws IOException {
		String line = reader.readLine();
		String[] result = null;
		if (fixed) {
			result = new String[positions.length];
			int begin_index = 0;
			int end_index = 0;
			for (int index = 0; index < result.length; index++) {
				end_index = positions[index] - 1;
				result[index] = line.substring(begin_index, end_index).trim();
				begin_index = end_index;
			}
		} else {
			result = line.split(delimiter);
		}
		return result;
	}
	public boolean ready() throws IOException {
		return reader.ready();
	}
	public void reset() throws IOException {
		reader.reset();
	}

}
