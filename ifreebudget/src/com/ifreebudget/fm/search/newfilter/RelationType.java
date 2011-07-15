package com.ifreebudget.fm.search.newfilter;

public enum RelationType {
	EQUALS("="),
	NOTEQUALS("!="),
	LESSER_THAN("<"),
	GREATER_THAN(">"),
	IN("IN"),
	LIKE("like");
	
	private String index;
	
	private RelationType(String index) {
		this.index = index;
	}
	
	public String getType() {
		return index;
	}
}
