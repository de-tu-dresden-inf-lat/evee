package de.tu_dresden.inf.lat.evee.concreteDomains.owlApi;

import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.CDAnnotationNames;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * The schema ontology of PACER, which declares and describes the annotation properties cd:concreteDomain and
 * cd:constraint and the concrete domains cd:QDiff, cd:QMult and cd:QLinear. Ontologies may import it, but need not.
 * It is published at its IRI, and this library contains a copy.
 */
public final class CDSchema {

    private CDSchema() {
    }

    /** the IRI of the schema ontology, where it is published */
    public static final IRI ONTOLOGY_IRI = IRI.create(CDAnnotationNames.SCHEMA_ONTOLOGY);

    /** resolves the schema ontology to the copy in this library, so that importing it needs no network */
    public static final OWLOntologyIRIMapper IRI_MAPPER = new IRIMapper();

    /**
     * The schema ontology in the manager: the one it has already, or the copy in this library, which is loaded into it.
     * Then an import of the schema ontology needs no network, also where the IRI mappers of the manager cannot be
     * changed, as in Protégé.
     */
    public static OWLOntology loadInto(OWLOntologyManager manager) throws OWLOntologyCreationException {
        OWLOntology schema = manager.getOntology(ONTOLOGY_IRI);
        if (schema != null)
            return schema;
        try (InputStream copy = CDSchema.class.getResourceAsStream("cd.owl")) {
            return manager.loadOntologyFromOntologyDocument(copy);
        } catch (IOException e) {
            throw new OWLOntologyCreationException("Cannot read the copy of the schema ontology", e);
        }
    }

    private static final class IRIMapper implements OWLOntologyIRIMapper {

        @Override
        public IRI getDocumentIRI(IRI ontologyIRI) {
            return ONTOLOGY_IRI.equals(ontologyIRI) ? IRI.create(CDSchema.class.getResource("cd.owl")) : null;
        }
    }
}
