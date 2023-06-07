package de.tu_dresden.inf.lat.model.data;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;

import de.tu_dresden.inf.lat.model.interfaces.IModel;

/**
 * @author Christian Alrabbaa
 *
 */
public class SimpleModel implements IModel {

	private final Set<Element> modelElements;

	@JsonCreator
	public SimpleModel(Set<Element> modelElements) {
		this.modelElements = modelElements;
	}

	@Override
	public Set<Element> getFinalizedModelElements() {
		return this.modelElements;
	}

}
