package beamline.dcr.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

public class DcrModel {

	public enum RELATION {
		CONDITION,
		RESPONSE,
		INCLUDE,
		EXCLUDE,
		SPAWN,
		MILESTONE,
		IMMCONDITION,
		IMMRESPONSE,
		DIRECTLOOP,
		SEQUENCE;
	}

	private Set<Triple<String, String, RELATION>> relations = new HashSet<Triple<String, String, RELATION>>();
	
	public void addRelations(Set<Triple<String, String, RELATION>> setOfRelations) {
		relations.addAll(setOfRelations);
	}
	public void addRelation(Triple<String, String, RELATION> relation) {
		relations.add(relation);
	}
	public Set<String> getActivities() {
		Set<String> activities = new HashSet<String>();
		for(Triple<String, String, RELATION> r : relations) {
			activities.add(r.getLeft());
			activities.add(r.getMiddle());
		}

		return activities;


	}
	public Set<Triple<String, String, RELATION>> getRelations() {
		return relations;
	}
	public boolean containsRelation(Triple<String, String, RELATION> relation){
		return relations.contains(relation);
	}
	public void removeRelation(String source, String target, RELATION relation){
		relations.remove(Triple.of(source, target, relation));
	}



}
