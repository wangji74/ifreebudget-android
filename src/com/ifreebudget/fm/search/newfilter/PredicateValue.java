package com.ifreebudget.fm.search.newfilter;

public class PredicateValue implements Filterable {
	private String value;
	private RelationType relation;
	
	public PredicateValue(RelationType relation, String value) {
		super();
		this.value = value;
		this.relation = relation;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public String getSubstitutionString() {
		return "?";
	}

	@Override
	public String getValue() {
		if(relation == RelationType.LIKE) {
			return "%" + value + "%";
		}
		return value;
	}
}
