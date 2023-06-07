package de.tu_dresden.inf.lat.counterExample;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.tu_dresden.inf.lat.counterExample.data.Loop;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.ElkModel;
import de.tu_dresden.inf.lat.model.data.Relation;

/**
 * @author Christian Alrabbaa
 *
 */
public class LoopTracker {
	private final ElkModel model;
	private final Map<Element, Set<Loop>> elementToLoops;

	public LoopTracker(ElkModel model) {
		this.model = model;
		this.elementToLoops = new HashMap<>();
		//fillMap();
	}

	public ElkModel getModel() {
		return model;
	}

	public Map<Element, Set<Loop>> getElementToLoops() {
		return elementToLoops;
	}

	public Set<Loop> getLoopsStartingWith(Element element, Relation relation) {
		if(elementToLoops.containsKey(element))
			return elementToLoops.get(element).stream().filter(x -> x.getFirstRelation().equals(relation))
				.collect(Collectors.toSet());
		else{
			Set<Loop> loops;
			List<Relation> loopRelations;
			Set<Relation> usedRelations;

				loops = new HashSet<>();
				usedRelations = new HashSet<>();

				for (Relation r : relation.getElement2().getRelations()) {
					if (r.isForward()) {
						loopRelations = new LinkedList<>();
						loopRelations.add(r);
						getLoop(relation.getElement2(), loopRelations, loops, usedRelations);
					}
				}
				this.elementToLoops.put(relation.getElement2(), loops);

				return loops;
		}

//		if (!elementToLoops.containsKey(element))
//			return Collections.emptySet();
//		return elementToLoops.get(element).stream().filter(x -> x.getFirstRelation().equals(relation))
//				.collect(Collectors.toSet());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elementToLoops == null) ? 0 : elementToLoops.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		LoopTracker other = (LoopTracker) obj;
		if (elementToLoops == null) {
			if (other.elementToLoops != null)
				return false;
		} else if (!elementToLoops.equals(other.elementToLoops))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}

	private void fillMap() {
		Set<Loop> loops;
		List<Relation> loopRelations;
		Set<Relation> usedRelations;
		for (Element e : model.getFinalizedModelElements()) {
			loops = new HashSet<>();
			usedRelations = new HashSet<>();

			for (Relation r : e.getRelations()) {
				if (r.isForward()) {
					loopRelations = new LinkedList<>();
					loopRelations.add(r);
					getLoop(e, loopRelations, loops, usedRelations);
				}
			}
			System.out.println("Loops starting with "+ e);
			loops.forEach(System.out::println);
			this.elementToLoops.put(e, loops);
		}
	}

	private void getLoop(Element e, List<Relation> currentPath, Set<Loop> loops, Set<Relation> usedRelations) {
		Element currentEnd = currentPath.get(currentPath.size() - 1).getElement2();

		if (currentEnd.equals(e))
			loops.add(new Loop(e, currentPath));
		else
			for (Relation r : currentEnd.getRelations()) {
//				if (usedRelations.contains(r)) {
//					if (r.isBackward())
//						continue;
//					List<Relation> newPath = new LinkedList<>(currentPath);
//					newPath.add(r);
//					System.out.println("adding here");
//					System.out.println(new Loop(currentEnd, newPath));
//					loops.add(new Loop(currentEnd, newPath));
//					continue;
//				}

				usedRelations.add(r);
				if (currentPath.contains(r))
					continue;
				if (r.isBackward())
					continue;
				List<Relation> newPath = new LinkedList<>(currentPath);
				newPath.add(r);

				getLoop(e, newPath, loops, usedRelations);
			}
	}

}
