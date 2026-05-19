package de.tu_dresden.inf.lat.model.data;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

/**
 * @author Christian Alrabbaa
 *
 */
public class RestrictionMapper {

	private final Map<OWLClass, OWLObjectSomeValuesFrom> cls2qer = new HashMap<>();
	private final Map<OWLObjectSomeValuesFrom, OWLClass> qer2cls = new HashMap<>();

	/**
	 * Constructor
	 */
	public RestrictionMapper() {

	}

	/**
	 * @return Map of a new artificial concept name to the restriction it represents
	 */
	public Map<OWLClass, OWLObjectSomeValuesFrom> getClass2Restriction() {
		return cls2qer;
	}

	/**
	 * @return Map of a restriction to its new artificial concept name
	 */
	public Map<OWLObjectSomeValuesFrom, OWLClass> getRestriction2Class() {
		return qer2cls;
	}

	/**
	 * Map the arguments to each other
	 * 
	 * @param conceptName
	 * @param restriction
	 */
	public void addEntry(OWLClass conceptName, OWLObjectSomeValuesFrom restriction) {
		addClassRestrictionEntry(conceptName, restriction);
		addRestrictionClassEntry(restriction, conceptName);
	}

	/**
	 * Add a new key of a concept name if it is not already mapped to a restriction
	 * 
	 * @param restriction
	 * @param conceptName
	 */
	private void addClassRestrictionEntry(OWLClass conceptName, OWLObjectSomeValuesFrom restriction) {
		if (!cls2qer.containsKey(conceptName))
			cls2qer.put(conceptName, restriction);
	}

	/**
	 * Add a new key of a restriction if it is not already mapped to a concept name
	 * 
	 * @param restriction
	 * @param conceptName
	 */
	private void addRestrictionClassEntry(OWLObjectSomeValuesFrom restriction, OWLClass conceptName) {
		if (!qer2cls.containsKey(restriction))
			qer2cls.put(restriction, conceptName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cls2qer == null) ? 0 : cls2qer.hashCode());
		result = prime * result + ((qer2cls == null) ? 0 : qer2cls.hashCode());
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
		RestrictionMapper other = (RestrictionMapper) obj;
		if (cls2qer == null) {
			if (other.cls2qer != null)
				return false;
		} else if (!cls2qer.equals(other.cls2qer))
			return false;
		if (qer2cls == null) {
			if (other.qer2cls != null)
				return false;
		} else if (!qer2cls.equals(other.qer2cls))
			return false;
		return true;
	}
}
