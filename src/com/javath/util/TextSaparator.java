package com.javath.util;

import java.util.ArrayList;

public class TextSaparator extends Instance {

	private boolean fixed;
	private ArrayList<Integer> fixed_Saparator;
	private String EOL;
	
	public TextSaparator(boolean fixed) {
		setFixed(fixed);
		//Integer[] a =  fields.toArray(new Integer[] {});
		EOL = Assign.Line_Separator;
	}
	
	public TextSaparator setFixed(boolean fixed) {
		this.fixed = fixed;
		if (fixed) {
			fixed_Saparator = new ArrayList<Integer>();
		} else {
			fixed_Saparator = null;
		}
		return this;
	}
	public TextSaparator setFixedSaparator(int[] positions) {
		if (fixed) {
			clearFixedSaparator();
			for (int index = 0; index < positions.length; index++)
				addFixedSaparator(positions[index]);
		}
		return this;
	}
	public TextSaparator clearFixedSaparator() {
		fixed_Saparator.clear();
		return this;
	}
	public TextSaparator addFixedSaparator(int position) {
		if (fixed)
			if (fixed_Saparator.isEmpty())
				if (fixed_Saparator.get(fixed_Saparator.size()) < position)
					fixed_Saparator.add(position);
				else
					throw new ObjectException("");
			else
				fixed_Saparator.add(position);
		else
			throw new ObjectException("");
		return this;
	}
	
	public TextSaparator setEOL(String Line_Separator) {
		EOL = Line_Separator;
		return this;
	}
	
}
