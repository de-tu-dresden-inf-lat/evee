package de.tu_dresden.inf.lat.model.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.tu_dresden.inf.lat.prettyPrinting.formatting.SimpleOWLFormatter;
import de.tu_dresden.inf.lat.model.tools.ObjectGenerator;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

/**
 * @author Christian Alrabbaa
 *
 */
public class Mapper {

	private final RestrictionMapper resMapper;
	private final ConjunctionMapper conjMapper;
	private final OWLClassExpression originalLHS, originalRHS;
	private final OWLClass aliasLHS, aliasRHS;
	private final Map<OWLClass, Element> cls2element;

	/**
	 * A map of complex concepts to concept names. Used to generate a model in
	 * ELKModel
	 * 
	 * @param originalLHS
	 * @param originalRHS
	 */
	@JsonCreator
	public Mapper(OWLClassExpression originalLHS, OWLClassExpression originalRHS) {

		resMapper = new RestrictionMapper();
		conjMapper = new ConjunctionMapper();

		this.originalLHS = originalLHS;
		this.originalRHS = originalRHS;

		if (!(originalLHS instanceof OWLClass)) {
			aliasLHS = ObjectGenerator.getInstance().getNextConceptName();
			if (originalLHS instanceof OWLObjectSomeValuesFrom)
				resMapper.addEntry(aliasLHS, (OWLObjectSomeValuesFrom) originalLHS);
			else if (originalLHS instanceof OWLObjectIntersectionOf)
				conjMapper.addEntry(aliasLHS, (OWLObjectIntersectionOf) originalLHS);
		} else
			aliasLHS = (OWLClass) originalLHS;

		if (!(originalRHS instanceof OWLClass)) {
			aliasRHS = ObjectGenerator.getInstance().getNextConceptName();
			if (originalRHS instanceof OWLObjectSomeValuesFrom)
				resMapper.addEntry(aliasRHS, (OWLObjectSomeValuesFrom) originalRHS);
			else if (originalRHS instanceof OWLObjectIntersectionOf)
				conjMapper.addEntry(aliasRHS, (OWLObjectIntersectionOf) originalRHS);
		} else
			aliasRHS = (OWLClass) originalRHS;

		cls2element = new HashMap<>();
	}

	@JsonIgnore
	public RestrictionMapper getRestrictionMapper() {
		return resMapper;
	}

	@JsonIgnore
	public ConjunctionMapper getConjunctionMapper() {
		return conjMapper;
	}

	@JsonIgnore
	public OWLClassExpression getOriginalLHS() {
		return originalLHS;
	}

	@JsonIgnore
	public OWLClassExpression getOriginalRHS() {
		return originalRHS;
	}

	@JsonIgnore
	public OWLClass getAliasLHS() {
		return aliasLHS;
	}

	@JsonIgnore
	public OWLClass getAliasRHS() {
		return aliasRHS;
	}

	@JsonIgnore
	public Map<OWLClass, Element> getClassRepresentatives() {
		return cls2element;
	}

	@JsonIgnore
	public Element getLHSRepresentativeElement() {
		return cls2element.get(aliasLHS);
	}

	@JsonIgnore
	public Element getRHSRepresentativeElement() {
		return cls2element.get(aliasRHS);
	}

	@JsonIgnore
	public void addNewRepresentative(OWLClass className, Element representative) {

		if (!cls2element.keySet().contains(className))
			cls2element.put(className, representative);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aliasLHS == null) ? 0 : aliasLHS.hashCode());
		result = prime * result + ((aliasRHS == null) ? 0 : aliasRHS.hashCode());
		result = prime * result + ((cls2element == null) ? 0 : cls2element.hashCode());
		result = prime * result + ((conjMapper == null) ? 0 : conjMapper.hashCode());
		result = prime * result + ((originalLHS == null) ? 0 : originalLHS.hashCode());
		result = prime * result + ((originalRHS == null) ? 0 : originalRHS.hashCode());
		result = prime * result + ((resMapper == null) ? 0 : resMapper.hashCode());
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
		Mapper other = (Mapper) obj;
		if (aliasLHS == null) {
			if (other.aliasLHS != null)
				return false;
		} else if (!aliasLHS.equals(other.aliasLHS))
			return false;
		if (aliasRHS == null) {
			if (other.aliasRHS != null)
				return false;
		} else if (!aliasRHS.equals(other.aliasRHS))
			return false;
		if (cls2element == null) {
			if (other.cls2element != null)
				return false;
		} else if (!cls2element.equals(other.cls2element))
			return false;
		if (conjMapper == null) {
			if (other.conjMapper != null)
				return false;
		} else if (!conjMapper.equals(other.conjMapper))
			return false;
		if (originalLHS == null) {
			if (other.originalLHS != null)
				return false;
		} else if (!originalLHS.equals(other.originalLHS))
			return false;
		if (originalRHS == null) {
			if (other.originalRHS != null)
				return false;
		} else if (!originalRHS.equals(other.originalRHS))
			return false;
		if (resMapper == null) {
			if (other.resMapper != null)
				return false;
		} else if (!resMapper.equals(other.resMapper))
			return false;
		return true;
	}

	// TODO Make concept types static collection like in proofs and add them to the
	// description of this function and others
	/**
	 * Return the representative element of the provided concept in the current
	 * mapper //TODO Currently only !!! are supported
	 * 
	 * @param concept
	 * @return
	 */
	@JsonIgnore
	public Element getRepresentativeOf(OWLClassExpression concept) {

		if (concept instanceof OWLClass)
			return this.getClassRepresentatives().get(concept);

		if (concept instanceof OWLObjectSomeValuesFrom)
			return this.getClassRepresentatives().get(this.getRestrictionMapper().getRestriction2Class().get(concept));

		if (!(concept instanceof OWLObjectIntersectionOf))
			assert false : "unexpected type of objects " + concept;

		return this.getClassRepresentatives().get(this.getConjunctionMapper().getConjunction2Class().get(concept));
	}

	/**
	 * Return a map of class expression to the name of its representative element.
	 * This method is used by the JSON writer
	 * 
	 * @return
	 */
	@JsonProperty("Concept2Representative")
	public Map<String, String> getClsExp2ElementName() {
		Map<String, String> res = new HashMap<>();
		for (Entry<OWLClass, Element> ent : this.cls2element.entrySet()) {
			res.put(SimpleOWLFormatter.format(getConceptFromAlias(ent.getKey())), ent.getValue().getName());
		}

		return res;
	}

	@JsonIgnore
	private OWLClassExpression getRestrictionFromAlias(OWLClassExpression alias) {
		if (this.getRestrictionMapper().getClass2Restriction().containsKey(alias))
			return this.getRestrictionMapper().getClass2Restriction().get(alias);
		return alias;
	}

	@JsonIgnore
	private OWLClassExpression getConjunctionFromAlias(OWLClassExpression alias) {
		if (this.getConjunctionMapper().getClass2Conjunction().containsKey(alias))
			return this.getConjunctionMapper().getClass2Conjunction().get(alias);
		return alias;
	}

	@JsonIgnore
	private OWLClassExpression getConceptFromAlias(OWLClassExpression alias) {
		OWLClassExpression res = getRestrictionFromAlias(alias);

		if (!res.equals(alias)) {
			OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) this.getRestrictionMapper()
					.getClass2Restriction().get(alias);
			OWLClassExpression filler = restriction.getFiller();

			return ToOWLTools.getInstance().getOWLExistentialRestriction(restriction.getProperty(),
					getConceptFromAlias(filler));
		}

		res = getConjunctionFromAlias(alias);
		if (!res.equals(alias)) {
			Set<OWLClassExpression> conjuncts = this.getConjunctionMapper().getClass2Conjunction().get(alias)
					.asConjunctSet();
			Set<OWLClassExpression> newConjuncts = new HashSet<>();
			for (OWLClassExpression conjunct : conjuncts) {
				newConjuncts.add(getConceptFromAlias(conjunct));
			}
			return ToOWLTools.getInstance().getOWLConjunction(newConjuncts);
		}

		return res;
	}
}