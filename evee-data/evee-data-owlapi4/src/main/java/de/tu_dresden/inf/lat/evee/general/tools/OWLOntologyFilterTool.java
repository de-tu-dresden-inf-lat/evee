package de.tu_dresden.inf.lat.evee.general.tools;

import de.tu_dresden.inf.lat.evee.general.interfaces.IOWLOntologyFilter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class OWLOntologyFilterTool {

    private OWLOntology ontology;
    private final IOWLOntologyFilter filter;
    private boolean ontologyContainsIgnoredElements;
    private final Logger logger = LoggerFactory.getLogger(OWLOntologyFilterTool.class);

    public OWLOntologyFilterTool(IOWLOntologyFilter filter){
        this.ontology = null;
        this.filter = null;
        this.ontologyContainsIgnoredElements = false;
    }

    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * @return A copy of the ontology which only contains supported axioms and non-logical axioms
     * (annotations and declaration axioms)
     */
    public OWLOntology filterOntology(){
        assert this.ontology != null;
        assert this.filter != null;
        this.ontologyContainsIgnoredElements = false;
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology filteredOntology = null;
        try {
            OWLOntology temporaryOntology = manager.createOntology();
            this.ontology.getAxioms().forEach(axiom -> {
                if ( (! isIrrelevantAxiomType(axiom)) &&
                        (! axiomIsSupported(
                                axiom, this.filter.getAxiomTypes(),
                                this.filter.getConceptTypes()))){
                    this.logger.debug("Axiom filtered: " + axiom);
                    this.ontologyContainsIgnoredElements = true;
                } else {
                    manager.addAxiom(temporaryOntology, axiom);
                }
            });
            filteredOntology = temporaryOntology;
        } catch (OWLOntologyCreationException e) {
            this.logger.error("Error when creating filtered Ontology: ", e);
        }
        return filteredOntology;
    }

    public boolean containsIgnoredElements(){
        return this.ontologyContainsIgnoredElements;
    }

    private boolean isIrrelevantAxiomType(OWLAxiom axiom){
        AxiomType<?> axiomType = axiom.getAxiomType();
        return (axiomType.equals(AxiomType.ANNOTATION_ASSERTION)) ||
                (axiomType.equals(AxiomType.DECLARATION));
    }

    private boolean axiomIsSupported(OWLAxiom axiom,
                                     List<AxiomType<? extends OWLAxiom>> supportedAxioms,
                                     List<ClassExpressionType> supportedConcepts) {
        if (! supportedAxioms.contains(axiom.getAxiomType())) {
            return false;
        }
        if (axiom.getNestedClassExpressions().stream()
                .anyMatch(expr -> ! supportedConcepts.contains(expr.getClassExpressionType()))) {
            return false;
        }
        return true;
    }

    public static class ELFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getAxiomTypes() {
            return Arrays.asList(AxiomType.EQUIVALENT_CLASSES, AxiomType.SUBCLASS_OF,
                    AxiomType.DECLARATION);
        }

        @Override
        public List<ClassExpressionType> getConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM);
        }
    }

    public static class ELHFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getAxiomTypes() {
            return Arrays.asList(AxiomType.EQUIVALENT_CLASSES, AxiomType.SUBCLASS_OF,
                    AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.DISJOINT_CLASSES,
                    AxiomType.SUB_OBJECT_PROPERTY, AxiomType.DECLARATION);
        }

        @Override
        public List<ClassExpressionType> getConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM);
        }
    }

    public static class ALCFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getAxiomTypes() {
            return Arrays.asList(AxiomType.EQUIVALENT_CLASSES, AxiomType.SUBCLASS_OF,
                    AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE,
                    AxiomType.DISJOINT_CLASSES, AxiomType.DISJOINT_UNION);
        }

        @Override
        public List<ClassExpressionType> getConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_UNION_OF, ClassExpressionType.OBJECT_COMPLEMENT_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM, ClassExpressionType.OBJECT_ALL_VALUES_FROM);
        }
    }

    public static class ALCHFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getAxiomTypes() {
            return Arrays.asList(AxiomType.EQUIVALENT_CLASSES, AxiomType.SUBCLASS_OF,
                    AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE,
                    AxiomType.DISJOINT_CLASSES, AxiomType.DISJOINT_UNION,
                    AxiomType.SUB_OBJECT_PROPERTY, AxiomType.DECLARATION);
        }

        @Override
        public List<ClassExpressionType> getConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_UNION_OF, ClassExpressionType.OBJECT_COMPLEMENT_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM, ClassExpressionType.OBJECT_ALL_VALUES_FROM);
        }
    }

    public static class ELBottomFilter implements IOWLOntologyFilter {

        public List<AxiomType<? extends OWLAxiom>> getAxiomTypes() {
            return Arrays.asList(AxiomType.EQUIVALENT_CLASSES, AxiomType.SUBCLASS_OF,
                    AxiomType.DECLARATION, AxiomType.DISJOINT_CLASSES);
        }

        @Override
        public List<ClassExpressionType> getConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM);
        }
    }


}
