package de.tu_dresden.inf.lat.evee.proofs.json;

import com.fasterxml.jackson.databind.util.StdConverter;
import de.tu_dresden.inf.lat.evee.proofs.data.AbstractExtendedAxiom;
import de.tu_dresden.inf.lat.prettyPrinting.formatting.Formatter;
import de.tu_dresden.inf.lat.prettyPrinting.formatting.ParsableOWLFormatter;
import org.semanticweb.owlapi.model.OWLAxiom;

public class JsonGeneric2StringConverter extends StdConverter<Object, String> {

    private final Formatter<OWLAxiom> formatter = new ParsableOWLFormatter();

    @Override
    public String convert(Object object) {
        if (object instanceof OWLAxiom) {
            OWLAxiom owlAxiom = (OWLAxiom) object;
            String ret = formatter.format(owlAxiom);
            if (ret.isEmpty()) {
                throw new IllegalStateException("Could not format axiom: " + owlAxiom);
            }
            return ret;
        }else if(object instanceof AbstractExtendedAxiom){
            return ((AbstractExtendedAxiom) object).getJSONString();
        }
        else
            return object.toString();
    }
}
