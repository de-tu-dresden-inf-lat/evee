package de.tu_dresden.inf.lat.model.data;

import org.semanticweb.owlapi.model.OWLObjectProperty;

import de.tu_dresden.inf.lat.model.interfaces.IRole;

/**
 * @author Christian Alrabbaa
 *
 */
public class RoleName implements IRole {

	private final OWLObjectProperty name;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public RoleName(OWLObjectProperty name) {
		this.name = name;
	}

	/**
	 * @return name
	 */
	public OWLObjectProperty getName() {
		return name;
	}

	@Override
	public String toString() {
		return name.toString();
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
		RoleName other = (RoleName) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
