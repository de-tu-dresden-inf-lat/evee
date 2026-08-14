package de.tu_dresden.inf.lat.evee.concreteDomains.preprocess;

import de.tu_dresden.inf.lat.evee.general.tools.OWLTools;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author stefborg
 *
 */
public class OWLObjectMap {

	private Map<OWLObject, OWLObject> map = new HashMap<>();
	protected Collection<OWLObject> domain = new LinkedList<>();
	protected Collection<OWLObject> range = new LinkedList<>();
	protected Collection<OWLClassExpression> domainSubconcepts = new LinkedList<>();
	protected Collection<OWLClassExpression> rangeSubconcepts = new LinkedList<>();
	protected Collection<OWLObjectProperty> domainSubroles = new LinkedList<>();
	protected Collection<OWLObjectProperty> rangeSubroles = new LinkedList<>();

	public boolean contains(OWLObject e1) {
		return domain.contains(e1);
	}

	public boolean containsSubset(OWLClassExpression expr) {
		if (expr instanceof OWLObjectUnionOf) {
			return getDomain().stream().filter(o -> o instanceof OWLObjectUnionOf).anyMatch(
					o -> subsetOf(((OWLObjectUnionOf) o).getOperands(), ((OWLObjectUnionOf) expr).getOperands()));
		}
		if (expr instanceof OWLObjectIntersectionOf) {
			return getDomain().stream().filter(o -> o instanceof OWLObjectIntersectionOf)
					.anyMatch(o -> subsetOf(((OWLObjectIntersectionOf) o).getOperands(),
							((OWLObjectIntersectionOf) expr).getOperands()));
		}
		return false;
	}

	private boolean subsetOf(Set<OWLClassExpression> s1, Set<OWLClassExpression> s2) {
		return s2.containsAll(s1);
	}

	public boolean add(OWLObject o1, OWLObject o2) {
		if ((o1 == null) || (o2 == null)) {
			throw new IllegalArgumentException("Cannot map 'null': " + o1 + " -> " + o2);
		}
		if ((o1 instanceof OWLClassExpression) && (o2 instanceof OWLClassExpression)) {
			return addClasses((OWLClassExpression) o1, (OWLClassExpression) o2);
		}
		if ((o1 instanceof OWLObjectPropertyExpression) && (o2 instanceof OWLObjectPropertyExpression)) {
			return addProperties((OWLObjectPropertyExpression) o1, (OWLObjectPropertyExpression) o2);
		}
		if ((o1 instanceof OWLIndividual) && (o2 instanceof OWLIndividual)) {
			return addIndividuals((OWLIndividual) o1, (OWLIndividual) o2);
		}
		throw new IllegalArgumentException("Cannot map between different types of OWL objects:\n" + o1 + "\n" + o2);
	}

	public boolean addClasses(OWLClassExpression c1, OWLClassExpression c2) {
		// Make sure that c1 does not share a subexpression with any element of the
		// domain of this map, and similarly for c2. Also, top and bottom should never
		// be mapped.
//		if (!c1.getNestedClassExpressions().stream().anyMatch(domainSubconcepts::contains)
//				&& !c2.getNestedClassExpressions().stream().anyMatch(rangeSubconcepts::contains)
//				&& !c1.getObjectPropertiesInSignature().stream().anyMatch(domainSubroles::contains)
//				&& !c2.getObjectPropertiesInSignature().stream().anyMatch(rangeSubroles::contains)
//				&& !areTopBottom(c1, c2)) {

//		if (!domain.contains(c1) && !range.contains(c2) && !Utils.areTopBottom(c1, c2)) {

		if(!map.containsKey(c1)){
			map.put(c1, c2);
			domain.add(c1);
			range.add(c2);
			c1.getNestedClassExpressions().forEach(domainSubconcepts::add);
			c2.getNestedClassExpressions().forEach(rangeSubconcepts::add);
			c1.getObjectPropertiesInSignature().forEach(domainSubroles::add);
			c2.getObjectPropertiesInSignature().forEach(rangeSubroles::add);
			return true;
		}
		return false;
	}

	public boolean addProperties(OWLObjectPropertyExpression p1, OWLObjectPropertyExpression p2) {
		if (!p1.getObjectPropertiesInSignature().stream().anyMatch(domainSubroles::contains)
				&& !p2.getObjectPropertiesInSignature().stream().anyMatch(rangeSubroles::contains)
				&& !areTopBottom(p1, p2)) {
//		if (!domain.contains(p1) && !range.contains(p2) && !Utils.areTopBottom(p1, p2)) {
			map.put(p1, p2);
			domain.add(p1);
			range.add(p2);
			p1.getObjectPropertiesInSignature().forEach(domainSubroles::add);
			p2.getObjectPropertiesInSignature().forEach(rangeSubroles::add);
			return true;
		}
		return false;
	}

	public boolean addIndividuals(OWLIndividual i1, OWLIndividual i2) {
		if (!domain.contains(i1) && !range.contains(i2)) {
			map.put(i1, i2);
			domain.add(i1);
			range.add(i2);
			return true;
		}
		return false;
	}

	public OWLObject remove(OWLObject o) {
		OWLObject o2 = map.remove(o);
		if (o2 != null) {
			domain.remove(o);
			range.remove(o2);
		}
		return o2;
	}

	public OWLObject apply(OWLObject o) {
		return map.get(o);
	}

	public OWLObject applySubset(OWLObject o) {
		if (o instanceof OWLObjectUnionOf) {
			return OWLTools.odf.getOWLObjectUnionOf(applyNAry((OWLObjectUnionOf) o));
		}
		if (o instanceof OWLObjectIntersectionOf) {
			return OWLTools.odf.getOWLObjectIntersectionOf(applyNAry((OWLObjectIntersectionOf) o));
		}
		return null;
	}

	private <T extends OWLNaryBooleanClassExpression> Set<OWLClassExpression> applyNAry(T o) {
		Set<OWLClassExpression> operands = o.getOperands();
		for (Entry<OWLObject, OWLObject> e : map.entrySet()) {
			if (o.getClass().isInstance(e.getKey())) {
				Set<OWLClassExpression> sub = o.getClass().cast(e.getKey()).getOperands();
				if (operands.containsAll(sub)) {
					operands.removeAll(sub);
					operands.add((OWLClassExpression) e.getValue());
				}
			}
		}
		return operands;
	}

	public OWLClassExpression applyClass(OWLClassExpression c) {
		return (OWLClassExpression) map.get(c);
	}

	public OWLObjectPropertyExpression applyProperty(OWLObjectPropertyExpression p) {
		return (OWLObjectPropertyExpression) map.get(p);
	}

	public OWLIndividual applyIndividual(OWLIndividual i) {
		return (OWLIndividual) map.get(i);
	}

	public Collection<OWLObject> getDomain() {
		return domain;
	}

	public Collection<OWLObject> getRange() {
		return range;
	}

	@Override
	public String toString() {
		return map.toString();
	}

	public static boolean areTopBottom(OWLObject o1, OWLObject o2) {
		return o1.isTopEntity() || o1.isBottomEntity() || o2.isTopEntity() || o2.isBottomEntity();
	}

}
