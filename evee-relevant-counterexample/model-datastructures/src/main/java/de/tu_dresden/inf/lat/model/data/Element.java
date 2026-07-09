package de.tu_dresden.inf.lat.model.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.tu_dresden.inf.lat.model.interfaces.IElement;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

/**
 * @author Christian Alrabbaa
 *
 */
public class Element implements IElement {

	@JsonProperty("Element Name")
	private final String name;
	@JsonProperty("Element Classes")
	private final Set<OWLClassExpression> types = new HashSet<>();
	@JsonProperty("Element Relations")
	private final Set<Relation> relations = new HashSet<>();

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	@JsonCreator
	public Element(@JsonProperty("Element Name") String name) {
		this.name = name;
	}

	/**
	 * @return all types of this element
	 */
	@Override
	public Set<OWLClassExpression> getTypes() {
		return types;
	}

	/**
	 * @return only the original types without the artificial ones
	 * 
	 * @param mapper
	 */
	@JsonIgnore
	public Set<OWLClassExpression> getOriginalTypes(Mapper mapper) {
		Set<OWLClassExpression> result = new HashSet<>();

		for (OWLClassExpression type : this.types) {
			if (mapper.getConjunctionMapper().getClass2Conjunction().keySet().contains(type))
				continue;
			if (mapper.getRestrictionMapper().getClass2Restriction().keySet().contains(type))
				continue;
			result.add(type);
		}

		return result;
	}

	/**
	 * add a type to the set of all types of this element
	 * 
	 * @param type
	 */
	@JsonIgnore
	public void addType(OWLClassExpression type) {
		this.types.add(type);
	}

	/**
	 * add types to the set of all types of this element
	 * 
	 * @param types
	 */
	@JsonIgnore
	public void addTypes(Collection<OWLClassExpression> types) {
		types.stream().forEach(this::addType);
	}

	/**
	 * add types to the set of all types of this element
	 * 
	 * @param types
	 */
	@JsonIgnore
	public void addTypes(Stream<? extends OWLClassExpression> types) {
		types.forEach(this::addType);
	}

	/**
	 * @return all relations of this element
	 */
	@Override
	public Set<Relation> getRelations() {
		return Collections.unmodifiableSet(relations);
	}

	/**
	 * add a relation to the set of all relations of this element
	 * 
	 * @param relation
	 */
	@JsonIgnore
	public void addRelation(Relation relation) {
		this.relations.add(relation);
	}

	/**
	 * add a set of relations to the set of all relations of this element
	 * 
	 * @param relations
	 */
	@JsonIgnore
	public void addRelations(Set<Relation> relations) {
		this.relations.addAll(relations);
	}

	/**
	 * @return Name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Remove artificial concept names introduced in the normalisation
	 * 
	 * @param mapper
	 */
	@JsonIgnore
	public void removeTypes(Mapper mapper) {
		mapper.getConjunctionMapper().getClass2Conjunction().keySet().forEach(key -> {
			if (types.contains(key))
				types.remove(key);
		});

		mapper.getRestrictionMapper().getClass2Restriction().keySet().forEach(key -> {
			if (types.contains(key)) {
				types.remove(key);
				// types.add(mapper.getClass2Restriction().get(key));
			}
		});

		if (!mapper.getOriginalLHS().equals(mapper.getAliasLHS()))
			if (types.contains(mapper.getAliasLHS()))
				types.remove(mapper.getAliasLHS());
	}

	/**
	 * Remove the provided artificial concept name from the set of types
	 * 
	 * @param cls
	 */
	@JsonIgnore
	public void removeType(OWLClassExpression cls) {
		types.remove(cls);
	}

	/**
	 * Remove the provided relation from the set of relations
	 * 
	 * @param rel
	 */
	@JsonIgnore
	public void removeRelation(Relation rel) {
		relations.remove(rel);
	}

	@Override
	public String toString() {
		String typesStr = types.stream().filter(x -> x instanceof OWLClass)
				.map(x -> ToOWLTools.getInstance().getShortIRIString(x.toString())).collect(Collectors.toSet())
				.toString();
		return name + " is in the following relation(s) " + relations + ", and it is an element of " + typesStr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Element other = (Element) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
