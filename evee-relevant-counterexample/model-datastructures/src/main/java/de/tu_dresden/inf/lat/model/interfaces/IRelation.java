package de.tu_dresden.inf.lat.model.interfaces;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import de.tu_dresden.inf.lat.model.data.RelationDirection;
import de.tu_dresden.inf.lat.model.json.JsonElement2StringConverter;
import de.tu_dresden.inf.lat.model.json.JsonOWLObjectProperty2StringConverter;
import de.tu_dresden.inf.lat.model.json.JsonString2ElementConverter;
import de.tu_dresden.inf.lat.model.json.JsonString2OWLObjectPropertyConverter;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(as = Relation.class)
@JsonPropertyOrder({ "Role Name", "First Element Name", "Second Element Name", "Edge Direction" })
public interface IRelation extends IModelComponent {

	@JsonSerialize(converter = JsonElement2StringConverter.class)
	@JsonDeserialize(converter = JsonString2ElementConverter.class)
    Element getElement1();

	@JsonSerialize(converter = JsonElement2StringConverter.class)
	@JsonDeserialize(converter = JsonString2ElementConverter.class)
	Element getElement2();

	@JsonSerialize(converter = JsonOWLObjectProperty2StringConverter.class)
	@JsonDeserialize(converter = JsonString2OWLObjectPropertyConverter.class)
	OWLObjectProperty getRoleName();

	@JsonIgnore
    RelationDirection getDirection();
}