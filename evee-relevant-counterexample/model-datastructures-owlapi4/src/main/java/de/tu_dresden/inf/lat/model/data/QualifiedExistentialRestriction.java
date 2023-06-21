package de.tu_dresden.inf.lat.model.data;

import de.tu_dresden.inf.lat.model.interfaces.IConcept;

/**
 * @author Christian Alrabbaa
 *
 */
public class QualifiedExistentialRestriction implements IConcept {

	private final RoleName roleName;
	private final IConcept concept;

	/**
	 * Constructor
	 * 
	 * @param roleName
	 * @param concept
	 */
	public QualifiedExistentialRestriction(RoleName roleName, IConcept concept) {
		this.roleName = roleName;
		this.concept = concept;
	}

	/**
	 * @return Role name
	 */
	public RoleName getRoleName() {
		return roleName;
	}

	/**
	 * @return Concept
	 */
	public IConcept getConcept() {
		return concept;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((concept == null) ? 0 : concept.hashCode());
		result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
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
		QualifiedExistentialRestriction other = (QualifiedExistentialRestriction) obj;
		if (concept == null) {
			if (other.concept != null)
				return false;
		} else if (!concept.equals(other.concept))
			return false;
		if (roleName == null) {
			if (other.roleName != null)
				return false;
		} else if (!roleName.equals(other.roleName))
			return false;
		return true;
	}

}
