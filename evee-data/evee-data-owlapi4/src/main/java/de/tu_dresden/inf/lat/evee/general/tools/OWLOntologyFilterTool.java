package de.tu_dresden.inf.lat.evee.general.tools;

import de.tu_dresden.inf.lat.evee.general.interfaces.IOWLOntologyFilter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class OWLOntologyFilterTool {

    private OWLOntology ontology;
    private final IOWLOntologyFilter filter;
    private boolean ontologyContainsIgnoredElements;
    private final Logger logger = LoggerFactory.getLogger(OWLOntologyFilterTool.class);

    public OWLOntologyFilterTool(IOWLOntologyFilter filter){
        this.ontology = null;
        this.filter = filter;
        this.ontologyContainsIgnoredElements = false;
    }

    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * @return A copy of the ontology which only contains supported axioms
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
                ;
                if ( (axiom.isOfType(this.filter.getSupportedFromABoxTBoxRBox()) &&
                        (! axiomIsSupported(
                                axiom, this.filter.getSupportedAxiomTypes(),
                                this.filter.getSupportedConceptTypes())))){
                    this.logger.debug("Axiom not supported: " + axiom);
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

    public boolean ontologyContainsIgnoredElements(){
        return this.ontologyContainsIgnoredElements;
    }

    private boolean axiomIsSupported(OWLAxiom axiom,
                                     List<AxiomType<? extends OWLAxiom>> supportedAxioms,
                                     List<ClassExpressionType> supportedConcepts) {
        if (! supportedAxioms.contains(axiom.getAxiomType())) {
            this.logger.debug("Axiom type not supported: {}", axiom.getAxiomType());
            return false;
        }
        AtomicBoolean supported = new AtomicBoolean(true);
        axiom.getNestedClassExpressions().forEach(expr -> {
            if (! supportedConcepts.contains(expr.getClassExpressionType())){
                this.logger.debug("Expression type not supported: {}", expr.getClassExpressionType());
                supported.set(false);
            }
        });
        return supported.get();
    }

    public static class ELFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getSupportedAxiomTypes() {
            return Arrays.asList(AxiomType.EQUIVALENT_CLASSES, AxiomType.SUBCLASS_OF,
                    AxiomType.DECLARATION);
        }

        @Override
        public List<ClassExpressionType> getSupportedConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM);
        }

        @Override
        public Set<AxiomType<?>> getSupportedFromABoxTBoxRBox() {
            return AxiomType.TBoxAndRBoxAxiomTypes;
        }
    }

    public static class ELBottomFilter implements IOWLOntologyFilter {

        public List<AxiomType<? extends OWLAxiom>> getSupportedAxiomTypes() {
            return Arrays.asList(AxiomType.EQUIVALENT_CLASSES, AxiomType.SUBCLASS_OF,
                    AxiomType.DECLARATION, AxiomType.DISJOINT_CLASSES);
        }

        @Override
        public List<ClassExpressionType> getSupportedConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM);
        }

        @Override
        public Set<AxiomType<?>> getSupportedFromABoxTBoxRBox() {
            return AxiomType.TBoxAndRBoxAxiomTypes;
        }
    }

    public static class ELHFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getSupportedAxiomTypes() {
            return Arrays.asList(AxiomType.EQUIVALENT_CLASSES, AxiomType.SUBCLASS_OF,
                    AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.DISJOINT_CLASSES,
                    AxiomType.SUB_OBJECT_PROPERTY);
        }

        @Override
        public List<ClassExpressionType> getSupportedConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM);
        }

        @Override
        public Set<AxiomType<?>> getSupportedFromABoxTBoxRBox() {
            return AxiomType.TBoxAndRBoxAxiomTypes;
        }
    }

    public static class ALCTBoxRBoxFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getSupportedAxiomTypes() {
            return Arrays.asList(AxiomType.SUBCLASS_OF, AxiomType.EQUIVALENT_CLASSES,
                    AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE,
                    AxiomType.DISJOINT_CLASSES, AxiomType.DISJOINT_UNION);
        }

        @Override
        public List<ClassExpressionType> getSupportedConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_UNION_OF, ClassExpressionType.OBJECT_COMPLEMENT_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM, ClassExpressionType.OBJECT_ALL_VALUES_FROM);
        }

        @Override
        public Set<AxiomType<?>> getSupportedFromABoxTBoxRBox() {
            return AxiomType.TBoxAndRBoxAxiomTypes;
        }
    }

    public static class ALCHFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getSupportedAxiomTypes() {
            return Arrays.asList(AxiomType.SUBCLASS_OF, AxiomType.EQUIVALENT_CLASSES,
                    AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE,
                    AxiomType.DISJOINT_CLASSES, AxiomType.DISJOINT_UNION,
                    AxiomType.SUB_OBJECT_PROPERTY);
        }

        @Override
        public List<ClassExpressionType> getSupportedConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_UNION_OF, ClassExpressionType.OBJECT_COMPLEMENT_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM, ClassExpressionType.OBJECT_ALL_VALUES_FROM);
        }

        @Override
        public Set<AxiomType<?>> getSupportedFromABoxTBoxRBox() {
            return AxiomType.TBoxAndRBoxAxiomTypes;
        }
    }

    public static class ALCOIFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getSupportedAxiomTypes() {
            return Arrays.asList(AxiomType.SUBCLASS_OF, AxiomType.EQUIVALENT_CLASSES,
                    AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE,
                    AxiomType.DISJOINT_CLASSES, AxiomType.DISJOINT_UNION,
                    AxiomType.SUB_OBJECT_PROPERTY, AxiomType.INVERSE_OBJECT_PROPERTIES);
        }

        @Override
        public List<ClassExpressionType> getSupportedConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_UNION_OF, ClassExpressionType.OBJECT_COMPLEMENT_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM, ClassExpressionType.OBJECT_ALL_VALUES_FROM, ClassExpressionType.OBJECT_HAS_VALUE, ClassExpressionType.OBJECT_ONE_OF);
        }

        @Override
        public Set<AxiomType<?>> getSupportedFromABoxTBoxRBox() {
            return AxiomType.TBoxAndRBoxAxiomTypes;
        }
    }

    public static class SHIFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getSupportedAxiomTypes() {
            return Arrays.asList(AxiomType.SUBCLASS_OF, AxiomType.EQUIVALENT_CLASSES,
                    AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE,
                    AxiomType.DISJOINT_CLASSES, AxiomType.DISJOINT_UNION,
                    AxiomType.SUB_OBJECT_PROPERTY, AxiomType.INVERSE_OBJECT_PROPERTIES, AxiomType.TRANSITIVE_OBJECT_PROPERTY);
        }

        @Override
        public List<ClassExpressionType> getSupportedConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_UNION_OF, ClassExpressionType.OBJECT_COMPLEMENT_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM, ClassExpressionType.OBJECT_ALL_VALUES_FROM);
        }

        @Override
        public Set<AxiomType<?>> getSupportedFromABoxTBoxRBox() {
            return AxiomType.TBoxAndRBoxAxiomTypes;
        }
    }

    public static class ALCKBFilter implements IOWLOntologyFilter {

        @Override
        public List<AxiomType<? extends OWLAxiom>> getSupportedAxiomTypes() {
            return Arrays.asList(AxiomType.SUBCLASS_OF, AxiomType.EQUIVALENT_CLASSES,
                    AxiomType.OBJECT_PROPERTY_DOMAIN, AxiomType.OBJECT_PROPERTY_RANGE,
                    AxiomType.DISJOINT_CLASSES, AxiomType.DISJOINT_UNION);
        }

        @Override
        public List<ClassExpressionType> getSupportedConceptTypes() {
            return Arrays.asList(ClassExpressionType.OWL_CLASS, ClassExpressionType.OBJECT_INTERSECTION_OF,
                    ClassExpressionType.OBJECT_UNION_OF, ClassExpressionType.OBJECT_COMPLEMENT_OF,
                    ClassExpressionType.OBJECT_SOME_VALUES_FROM, ClassExpressionType.OBJECT_ALL_VALUES_FROM);
        }

        @Override
        public Set<AxiomType<?>> getSupportedFromABoxTBoxRBox() {
            Set<AxiomType<?>> result = new HashSet<>(AxiomType.TBoxAndRBoxAxiomTypes);
            result.addAll(AxiomType.ABoxAxiomTypes);
            return result;
        }
    }

}
