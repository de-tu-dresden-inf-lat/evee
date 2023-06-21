package de.tu_dresden.inf.lat.model.data;

import org.semanticweb.owlapi.model.OWLObjectProperty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.tu_dresden.inf.lat.model.interfaces.IRelation;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

public class Relation implements IRelation {

	private static final ToOWLTools tools = ToOWLTools.getInstance();
	@JsonProperty("Role Name")
	private final OWLObjectProperty roleName;
	@JsonProperty("First Element Name")
	private final Element element1;
	@JsonProperty("Second Element Name")
	private final Element element2;
	@JsonProperty("Edge Direction")
	private final RelationDirection direction;

	/**
	 * Constructor
	 * 
	 * @param roleName
	 * @param element1
	 * @param element2
	 * @param direction
	 */
	@JsonCreator
	public Relation(@JsonProperty("Role Name") OWLObjectProperty roleName,
			@JsonProperty("First Element Name") Element element1, @JsonProperty("Second Element Name") Element element2,
			@JsonProperty("Edge Direction") RelationDirection direction) {

		this.roleName = roleName;
		this.element1 = element1;
		this.element2 = element2;
		this.direction = direction;
	}

	/**
	 * 
	 * @return OWLObjectProperty
	 *
	 */
	@Override
	public OWLObjectProperty getRoleName() {
		return roleName;
	}

	@Override
	public Element getElement1() {
		return element1;
	}

	@Override
	public Element getElement2() {
		return element2;
	}

	@Override
	public RelationDirection getDirection() {
		return this.direction;
	}

	@JsonIgnore
	public boolean isForward() {
		return this.direction == RelationDirection.Forward || this.direction == RelationDirection.Bidirectional;
	}

	@JsonIgnore
	public boolean isBackward() {
		return this.direction == RelationDirection.Backward || this.direction == RelationDirection.Bidirectional;
	}

	@Override
	public String toString() {

		String roleNameStr = tools.getShortIRIString(roleName.toString());

		if (this.direction == RelationDirection.Bidirectional)
			return element1.getName() + " <-" + roleNameStr + "-> " + element2.getName();

		if (this.direction == RelationDirection.Backward)
			return element2.getName() + " <-" + roleNameStr + "- " + element1.getName();

		return element1.getName() + " -" + roleNameStr + "-> " + element2.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((element1 == null) ? 0 : element1.hashCode());
		result = prime * result + ((element2 == null) ? 0 : element2.hashCode());
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
		Relation other = (Relation) obj;
		if (direction != other.direction)
			return false;
		if (element1 == null) {
			if (other.element1 != null)
				return false;
		} else if (!element1.equals(other.element1))
			return false;
		if (element2 == null) {
			if (other.element2 != null)
				return false;
		} else if (!element2.equals(other.element2))
			return false;
		if (roleName == null) {
			if (other.roleName != null)
				return false;
		} else if (!roleName.equals(other.roleName))
			return false;
		return true;
	}

}
