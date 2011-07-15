package com.ifreebudget.fm.search.newfilter;

import java.util.ArrayList;
import java.util.List;

public class Predicate {
	public static final String SPACE = " ";
	private String key;
	private List<Filterable> values;
	private RelationType rel;
	private String type;
	
	public static Predicate create(String key, Filterable value, RelationType rel, String type) {
		List<Filterable> vals = new ArrayList<Filterable>();
		vals.add(value);
		
		Predicate p = new Predicate();
		p.key = key;
		p.values = vals;
		p.rel = rel;
		p.type = type;
		
		return p;
	}
	
	public static Predicate create(String key, String value, RelationType rel, String type) {
		List<Filterable> vals = new ArrayList<Filterable>();
		vals.add(new PredicateValue(rel, value));

		Predicate p = new Predicate();
		p.key = key;
		p.values = vals;
		p.rel = rel;
		p.type = type;
		
		return p;
	}

	public static Predicate create(String key, List<Filterable> values,
			RelationType rel, String type) {
		Predicate p = new Predicate();
		p.key = key;
		p.values = values;
		p.rel = rel;
		p.type = type;
		
		return p;
	}

	public String getKey() {
		return key;
	}

	public List<Filterable> getValues() {
		return values;
	}

	public RelationType getRel() {
		return rel;
	}
	
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();

		ret.append(key);
		ret.append(SPACE);
		ret.append(rel.getType());
		ret.append(SPACE);

		int sz = values.size();
		if (sz > 0) {
			ret.append(" ( ");
			for (int i = 0; i < sz; i++) {
				ret.append(values.get(i).getSubstitutionString());
				if (i < sz - 1) {
					ret.append(", ");
				}
			}
			ret.append(" ) ");
		}
		return ret.toString();
	}	
}
