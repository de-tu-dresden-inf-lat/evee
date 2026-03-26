package de.tu_dresden.inf.lat.counterExample;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import de.tu_dresden.inf.lat.model.data.Element;
import de.tu_dresden.inf.lat.model.data.Relation;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.*;

/**
 * @author Christian Alrabbaa
 */
public class RefinerMapper {
    private final Map<OWLObjectProperty, Map<Element, Set<Element>>> sim;
    private final Map<OWLObjectProperty, Map<Element, Set<Element>>> post;
    private final Map<OWLObjectProperty, Map<Element, Set<Element>>> pre;

    private final Map<OWLObjectProperty, Map<Element, Set<Element>>> remove;

    public Map<OWLObjectProperty, Map<Element, Set<Element>>> getSim() {
        return sim;
    }

    public Map<OWLObjectProperty, Map<Element, Set<Element>>> getPost() {
        return post;
    }

    public Map<OWLObjectProperty, Map<Element, Set<Element>>> getPre() {
        return pre;
    }

    public Map<OWLObjectProperty, Map<Element, Set<Element>>> getRemove() {
        return remove;
    }

    public Set<OWLObjectProperty> getRoleNames() {
        return roleNames;
    }

    private final Set<OWLObjectProperty> roleNames;

    public RefinerMapper(Set<Element> modelElements){
        sim = new HashMap<>();
        post = new HashMap<>();
        pre = new HashMap<>();
        remove = new HashMap<>();

        roleNames = new HashSet<>();

        init(modelElements);

    }

    private void init(Set<Element> modelElements) {
        fillRoleNamesSet(modelElements);

        for(OWLObjectProperty r: roleNames){
            sim.put(r, new HashMap<>());
            post.put(r, new HashMap<>());
            pre.put(r, new HashMap<>());
            remove.put(r, new HashMap<>());

            init_r(r, modelElements);
        }
    }

    public void computeSimulation(){

        Optional<Element> vOpt;
        Element v;

        for(OWLObjectProperty r: roleNames) {
            Set<Element> intersection;
            while (true) {
                vOpt = getNodeWithMoreToRemove(r);
                if(!vOpt.isPresent())
                    break;

                v = vOpt.get();
                for(Element u : getFrom(v, r, this.pre)){
                    for(Element w : getFrom(v, r, this.remove)){
                        if (this.sim.get(r).get(u).contains(w)){
                            this.sim.get(r).get(u).remove(w);
                            for(Element wPrime : getFrom(w,r,this.pre)){
                                intersection = Sets.newHashSet(this.post.get(r).get(wPrime));
                                intersection.retainAll(this.sim.get(r).get(u));
                                if(intersection.isEmpty())
                                    this.remove.get(r).get(u).add(wPrime);
                            }
                        }
                    }
                }

                this.remove.get(r).get(v).removeAll(this.remove.get(r).get(v));
            }
        }
    }

    private Optional<Element> getNodeWithMoreToRemove(OWLObjectProperty r) {
        return this.remove.get(r).keySet().stream().filter(x -> !this.remove.get(r).get(x).isEmpty()).findFirst();
    }

    private void fillRoleNamesSet(Set<Element> modelElements) {
        for (Element e: modelElements){
            for (Relation r: e.getRelations()){
                roleNames.add(r.getRoleName());
            }
        }
    }

    private void init_r(OWLObjectProperty r, Set<Element> modelElements) {

        for(Element e: modelElements){

            for(Relation relation: e.getRelations()){
                if(relation.getRoleName().equals(r)){
                    if (relation.isForward()){
                        addToMap(e, relation.getElement2(), post.get(r));
                        addToMap(relation.getElement2(),e, pre.get(r));
                    }
                }
            }
        }

        for(Element e: modelElements){
            if(getFrom(e, r, this.post).isEmpty())
                for(Element o : modelElements){
                    if (o.getTypes().containsAll(e.getTypes()))
                        addToMap(e, o, sim.get(r));
                }
            else
                for(Element o : modelElements){
                    if (o.getTypes().containsAll(e.getTypes()) && !getFrom(o, r, this.post).isEmpty())
                        addToMap(e, o, sim.get(r));
                }

        }

        //Fill remove map after initializing sim and prevsim
        Set<Element> preAll = new HashSet<>();
        pre.get(r).keySet().forEach(k->
            preAll.addAll(pre.get(r).get(k))
        );

        for(Element e: modelElements){
            if(!remove.get(r).containsKey(e))
                remove.get(r).put(e,new HashSet<>());

            Set<Element> preAllCopy = Sets.newHashSet(preAll);

            if(sim.get(r).containsKey(e))
                sim.get(r).get(e).forEach(o-> preAllCopy.removeAll(pre.get(r).getOrDefault(o, new HashSet<>())));

            preAllCopy.forEach(o -> addToMap(e, o, remove.get(r)));
        }

    }

    private Collection<Element> getFrom(Element o, OWLObjectProperty r, Map<OWLObjectProperty, Map<Element,
            Set<Element>>> m) {
        if(!m.get(r).containsKey(o))
            return new HashSet<>();
        return m.get(r).get(o);
    }

    public static <T,K> void addToMap(T key, K value, Map<T, Set<K>> map) {
        if(!map.containsKey(key))
            map.put(key,new HashSet<>(Collections.singletonList(value)));
        else
            map.get(key).add(value);
    }
}
