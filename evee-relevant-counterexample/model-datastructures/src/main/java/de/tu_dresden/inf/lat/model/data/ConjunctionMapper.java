package de.tu_dresden.inf.lat.model.data;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

/**
 * @author Christian Alrabbaa
 *
 */
public class ConjunctionMapper {

	private final Map<OWLClass, OWLObjectIntersectionOf> cls2Conj = new HashMap<>();
	private final Map<OWLObjectIntersectionOf, OWLClass> conj2Cls = new HashMap<>();

	/**
	 * Constructor
	 */
	public ConjunctionMapper() {

	}

	/**
	 * @return Map of new artificial concept names to conjunctions they represent
	 */
	public Map<OWLClass, OWLObjectIntersectionOf> getClass2Conjunction() {
		return cls2Conj;
	}

	/**
	 * @return Map of conjunctions to their new artificial concept names
	 */
	public Map<OWLObjectIntersectionOf, OWLClass> getConjunction2Class() {
		return conj2Cls;
	}

	/**
	 * Map the arguments to each other
	 * 
	 * @param conceptName
	 * @param conjunction
	 */
	public void addEntry(OWLClass conceptName, OWLObjectIntersectionOf conjunction) {
		addClassConjunctionEntry(conceptName, conjunction);
		addConjunctionClassEntry(conjunction, conceptName);
	}

	/**
	 * Add a new key of a concept name if it is not already mapped to a conjunction
	 * 
	 * @param conjunction
	 * @param conceptName
	 */
	private void addClassConjunctionEntry(OWLClass conceptName, OWLObjectIntersectionOf conjunction) {
		if (!cls2Conj.containsKey(conceptName))
			cls2Conj.put(conceptName, conjunction);
	}

	/**
	 * Add a new key of a conjunction if it is not already mapped to a concept name
	 * 
	 * @param conjunction
	 * @param conceptName
	 */
	private void addConjunctionClassEntry(OWLObjectIntersectionOf conjunction, OWLClass conceptName) {
		if (!conj2Cls.containsKey(conjunction))
			conj2Cls.put(conjunction, conceptName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cls2Conj == null) ? 0 : cls2Conj.hashCode());
		result = prime * result + ((conj2Cls == null) ? 0 : conj2Cls.hashCode());
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
		ConjunctionMapper other = (ConjunctionMapper) obj;
		if (cls2Conj == null) {
			if (other.cls2Conj != null)
				return false;
		} else if (!cls2Conj.equals(other.cls2Conj))
			return false;
		if (conj2Cls == null) {
			if (other.conj2Cls != null)
				return false;
		} else if (!conj2Cls.equals(other.conj2Cls))
			return false;
		return true;
	}
}
