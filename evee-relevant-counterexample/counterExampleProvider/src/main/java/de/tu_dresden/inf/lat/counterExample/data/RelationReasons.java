package de.tu_dresden.inf.lat.counterExample.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.model.data.Relation;

/**
 * @author Christian Alrabbaa
 *
 *         For every edge that will be kept in the model, an object of this
 *         class should contain a list of relations that justify this
 */
public class RelationReasons {

	private final Map<Relation, Set<Relation>> lHSRelation2RHSRelations;

	public RelationReasons() {
		this.lHSRelation2RHSRelations = new HashMap<>();
	}

	public void updateMap(Relation relation, Set<Relation> values) {
		if (this.lHSRelation2RHSRelations.containsKey(relation))
			this.lHSRelation2RHSRelations.get(relation).addAll(values);
		else
			this.lHSRelation2RHSRelations.put(relation, Sets.newHashSet(values));
	}

	public Map<Relation, Set<Relation>> getLHSRelation2RHSRelations() {
		return lHSRelation2RHSRelations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lHSRelation2RHSRelations == null) ? 0 : lHSRelation2RHSRelations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelationReasons other = (RelationReasons) obj;
		if (lHSRelation2RHSRelations == null) {
			if (other.lHSRelation2RHSRelations != null)
				return false;
		} else if (!lHSRelation2RHSRelations.equals(other.lHSRelation2RHSRelations))
			return false;
		return true;
	}

}
