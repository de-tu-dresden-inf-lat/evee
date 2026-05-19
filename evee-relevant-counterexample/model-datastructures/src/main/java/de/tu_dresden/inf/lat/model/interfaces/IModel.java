package de.tu_dresden.inf.lat.model.interfaces;

import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.SimpleModel;

/**
 * @author Christian Alrabbaa
 *
 */
@JsonDeserialize(as = SimpleModel.class)
public interface IModel {

	public Set<Element> getFinalizedModelElements();

}
