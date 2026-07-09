package de.tu_dresden.inf.lat.model.json;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;

import com.fasterxml.jackson.databind.util.StdConverter;

import de.tu_dresden.inf.lat.model.tools.ToOWLTools;

/**
 * @author Christian Alrabbaa
 *
 */
public class JsonCollectionOWLCLassExpression2SetStringConverter
		extends StdConverter<Collection<OWLClassExpression>, Collection<String>> {

	@Override
	public Collection<String> convert(Collection<OWLClassExpression> value) {
		Set<String> result = new HashSet<>();

		for (OWLClassExpression exp : value) {
			if (exp.equals(ToOWLTools.getInstance().getOWLTop())) {
				result.add("TOP");
			} else if (exp.equals(ToOWLTools.getInstance().getOWLBot())) {
				result.add("BOT");
			} else
				result.add(exp.toString());
		}

		return result;
	}

}
