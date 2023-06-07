package de.tu_dresden.inf.lat.counterExample.data;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Mapper;
import de.tu_dresden.inf.lat.model.data.Relation;

/**
 * An object of this class contains all elements reachable from both
 * representatives of LHS and RHS
 * 
 * @author Christian Alrabbaa
 *
 */
public class CommonElements {

	private final Set<Element> elements;

	public CommonElements(Set<Element> modelElements, Mapper mapper, ModelType modelType) {
		elements = getReachableFromBoth(modelElements, mapper, modelType);
	}

	private Set<Element> getReachableFromBoth(Set<Element> coarseModel, Mapper mapper, ModelType modelType) {

		Element lhsRep = getElementFrom(mapper.getLHSRepresentativeElement(), coarseModel);

		Set<Element> reachableFromA = Sets.newHashSet(lhsRep);

		Set<Relation> explored = new HashSet<>();
		fillReachableElementsSet(lhsRep, reachableFromA, coarseModel, explored);

		if (modelType == ModelType.Alpha)
			return reachableFromA;

		Element rhsRep = getElementFrom(mapper.getRHSRepresentativeElement(), coarseModel);
		Set<Element> reachableFromB = Sets.newHashSet(rhsRep);

		explored = new HashSet<>();
		fillReachableElementsSet(rhsRep, reachableFromB, coarseModel, explored);

		Set<Element> reachableFromBoth = new HashSet<>(reachableFromA);
		reachableFromBoth.retainAll(reachableFromB);

		return reachableFromBoth;
	}

	private void fillReachableElementsSet(Element element, Set<Element> reachableElements, Set<Element> model,
			Set<Relation> processedRelations) {
		Element targetElement;

		for (Relation r : element.getRelations()) {
			if (!r.isForward())
				continue;
			if (processedRelations.contains(r))
				continue;
			processedRelations.add(r);

			targetElement = getElementFrom(r.getElement2(), model);
			reachableElements.add(targetElement);
			fillReachableElementsSet(targetElement, reachableElements, model, processedRelations);
		}
	}

	private Element getElementFrom(Element toFind, Set<Element> elements) {
		return elements.stream().filter(x -> x.equals(toFind)).collect(Collectors.toList()).get(0);
	}

	public Set<Element> getElements() {
		return elements;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elements == null) ? 0 : elements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommonElements other = (CommonElements) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}
}
