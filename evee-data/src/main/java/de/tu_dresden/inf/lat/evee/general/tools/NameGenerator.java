package de.tu_dresden.inf.lat.evee.general.tools;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import org.semanticweb.owlapi.model.OWLEntity;

public abstract class NameGenerator<T extends OWLEntity> implements Iterator<T> {

	private int a;
	private char A;
	private int upperBound;
	private Function<String, T> constructor;
	private String prefix;
	private boolean readable;
	private Set<OWLEntity> sig;

	public NameGenerator(char A, int upperBound, Function<String, T> constructor, String prefix, boolean readable,
			Set<OWLEntity> sig) {
		this.a = 0;
		this.A = A;
		this.upperBound = upperBound;
		this.constructor = constructor;
		this.prefix = prefix;
		this.readable = readable;
		this.sig = (sig != null) ? sig : new HashSet<>();
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public T next() {
		T next;
		do {
			if (readable && (a >= upperBound)) {
				// start with A0, A1 etc. once we reach Z
				readable = false;
				a = 0;
			}
			String name = readable ? ("" + ((char) (A + a))) : (A + Integer.valueOf(a).toString());
			a++;
			next = constructor.apply(prefix + name);
		} while (sig.contains(next));
		return next;
	}

}
