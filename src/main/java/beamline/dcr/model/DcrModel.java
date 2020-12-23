package beamline.dcr.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

public class DcrModel {

	public enum RELATION {
		CONDITION, RESPONSE, INCLUDE, EXCLUDE, SPAWN, MILESTONE
	}
	
	private Set<Triple<String, String, RELATION>> relations = new HashSet<Triple<String, String, RELATION>>();
	
	public void addRelation(String source, String target, RELATION relation) {
		relations.add(Triple.of(source, target, relation));
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
}
