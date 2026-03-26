package de.tu_dresden.inf.lat.model.data;

/**
 * @author Christian Alrabbaa
 *
 * @param <TYPE1>
 * @param <TYPE2>
 */
public class EntryPair<TYPE1, TYPE2> {

	private final TYPE1 firstArg;
	private final TYPE2 secondArg;

	public EntryPair(TYPE1 arg1, TYPE2 arg2) {
		this.firstArg = arg1;
		this.secondArg = arg2;
	}

	public TYPE1 getFirstArg() {
		return firstArg;
	}

	public TYPE2 getSecondArg() {
		return secondArg;
	}

	@Override
	public String toString() {
		return "(" + firstArg + "," + secondArg + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstArg == null) ? 0 : firstArg.hashCode());
		result = prime * result + ((secondArg == null) ? 0 : secondArg.hashCode());
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
		@SuppressWarnings("unchecked")
		EntryPair<TYPE1, TYPE2> other = (EntryPair<TYPE1, TYPE2>) obj;
		if (firstArg == null) {
			if (other.firstArg != null)
				return false;
		} else if (!firstArg.equals(other.firstArg))
			return false;
		if (secondArg == null) {
			if (other.secondArg != null)
				return false;
		} else if (!secondArg.equals(other.secondArg))
			return false;
		return true;
	}

}
