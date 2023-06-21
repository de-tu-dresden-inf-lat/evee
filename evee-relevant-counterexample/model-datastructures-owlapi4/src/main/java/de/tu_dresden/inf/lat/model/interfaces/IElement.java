package de.tu_dresden.inf.lat.model.interfaces;

import java.util.Set;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.json.JsonCollectionOWLCLassExpression2SetStringConverter;
import de.tu_dresden.inf.lat.model.json.JsonCollectionString2SetOWLClassExpressionConverter;
import org.semanticweb.owlapi.model.OWLClassExpression;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(as = Element.class)
@JsonPropertyOrder({ "Element Name", "Element Classes", "Element Relations" })
public interface IElement extends IModelComponent {

	String getName();

	@JsonSerialize(converter = JsonCollectionOWLCLassExpression2SetStringConverter.class)
	@JsonDeserialize(converter = JsonCollectionString2SetOWLClassExpressionConverter.class)
	Set<OWLClassExpression> getTypes();

	Set<Relation> getRelations();
}
