package de.tu_dresden.inf.lat.evee.concreteDomains.data.names;


public final class CDAnnotationNames {
    
     // private constructor to prevent instantiation
    private CDAnnotationNames() {}

    /** the namespace of the annotation properties and of the auxiliary classes in proofs */
    public static final String NAMESPACE = "http://tu-dresden.de/inf/lat/evee/cd#";

    public static final String CONSTRAINT_ANNOTATION_PROP = "http://tu-dresden.de/inf/lat/evee/cd#constraint";
    public static final String CONCRETE_DOMAIN_ANNOTATION_PROP = "http://tu-dresden.de/inf/lat/evee/cd#concreteDomain";

    /** the IRI of the schema ontology, which declares and describes these names; importing it is optional */
    public static final String SCHEMA_ONTOLOGY = "https://de-tu-dresden-inf-lat.github.io/evee/cd.owl";
}
