package de.tu_dresden.inf.lat.counterExample.data;

import java.util.List;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;

/**
 * @author Christian Alrabbaa
 *
 */
public class Loop {

	private final Element loopStart;
	private final Relation firstRelation;
	private final List<Relation> loopRelations;
	private final int loopSize;

	public Loop(Element loopStart, List<Relation> loopRelations) {
		assert !loopRelations.isEmpty() : "A loop should consist of at least one edge";

		this.loopStart = loopStart;
		this.loopRelations = loopRelations;
		this.firstRelation = this.loopRelations.get(0);
		this.loopSize = this.loopRelations.size();
	}

	public Element getLoopStart() {
		return loopStart;
	}

	public Relation getFirstRelation() {
		return firstRelation;
	}

	public List<Relation> getLoopRelations() {
		return loopRelations;
	}

	public int getLoopSize() {
		return loopSize;
	}

	@Override
	public String toString() {
		StringBuilder sB = new StringBuilder();
		for (Relation r : this.loopRelations)
			sB.append(r + " ");
		return sB.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstRelation == null) ? 0 : firstRelation.hashCode());
		result = prime * result + ((loopRelations == null) ? 0 : loopRelations.hashCode());
		result = prime * result + loopSize;
		result = prime * result + ((loopStart == null) ? 0 : loopStart.hashCode());
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
		Loop other = (Loop) obj;
		if (firstRelation == null) {
			if (other.firstRelation != null)
				return false;
		} else if (!firstRelation.equals(other.firstRelation))
			return false;
		if (loopRelations == null) {
			if (other.loopRelations != null)
				return false;
		} else if (!loopRelations.equals(other.loopRelations))
			return false;
		if (loopSize != other.loopSize)
			return false;
		if (loopStart == null) {
			if (other.loopStart != null)
				return false;
		} else if (!loopStart.equals(other.loopStart))
			return false;
		return true;
	}

}
