package de.tu_dresden.inf.lat.model.json;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLClassExpression;

import com.fasterxml.jackson.databind.util.StdConverter;

import de.tu_dresden.inf.lat.prettyPrinting.owlapi.OWLExporter;
import de.tu_dresden.inf.lat.prettyPrinting.parsing.DLParser;
import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

/**
 * @author Christian Alrabbaa
 *
 */
public class JsonCollectionString2SetOWLClassExpressionConverter
		extends StdConverter<Collection<String>, Collection<OWLClassExpression>> {

	@Override
	public Set<OWLClassExpression> convert(Collection<String> value) {
		return value.stream().map(DLParser::parseConcept)
				.map(x -> (new OWLExporter(false)).toOwl(ToOWLTools.getInstance().getEmptyOntology(), x))
				.collect(Collectors.toSet());
	}
}
