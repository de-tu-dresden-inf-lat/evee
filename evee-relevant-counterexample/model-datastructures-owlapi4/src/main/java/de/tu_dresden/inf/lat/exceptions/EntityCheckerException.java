package de.tu_dresden.inf.lat.exceptions;

/**
 * @author Christian Alrabbaa
 *
 */
public class EntityCheckerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EntityCheckerException() {
		super("Axiom can not be recognised!\nCheck the format and make sure that the names used in the axiom occur in the ontology");
	}

}
