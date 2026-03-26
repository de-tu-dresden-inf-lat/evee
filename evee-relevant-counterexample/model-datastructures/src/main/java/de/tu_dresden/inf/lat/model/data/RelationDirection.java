package de.tu_dresden.inf.lat.model.data;

/**
 * @author Christian Alrabbaa
 *
 */
public enum RelationDirection {

	Forward(7), Backward(3), Bidirectional(5);

	private final int value;

	private RelationDirection(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
