package com.ifreebudget.fm.search.newfilter;

public enum OperatorType {
	AND(1),
	OR(2);
	
	private int index;
	
	private OperatorType(int index) {
		this.index = index;
	}
	
	public int getType() {
		return index;
	}
}
