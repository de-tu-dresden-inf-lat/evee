package de.tu_dresden.inf.lat.model.data;

import org.semanticweb.owlapi.model.OWLClass;

import de.tu_dresden.inf.lat.model.interfaces.IConcept;

/**
 * @author Christian Alrabbaa
 *
 */
public class ConceptName implements IConcept {

	private final OWLClass name;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public ConceptName(OWLClass name) {
		this.name = name;
	}

	/**
	 * @return Concept name
	 */
	public OWLClass getName() {
		return name;
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
		ConceptName other = (ConceptName) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
