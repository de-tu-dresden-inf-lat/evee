package de.tu_dresden.inf.lat.evee.protege.concreteDomainReasoner;

import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.CDAnnotationNames;
import de.tu_dresden.inf.lat.evee.concreteDomains.data.names.ConcreteDomainName;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Prepares the active ontology for PACER: declares the annotation properties cd:concreteDomain and cd:constraint,
 * sets the concrete domain chosen in a dialog as the value of cd:concreteDomain, and adds the prefix cd:. The
 * changes of the annotations can be undone; the prefix stays.
 */
public class ConcreteDomainSetupAction extends ProtegeOWLAction {

    private static final String PREFIX = "cd:";
    private static final String TITLE = "Concrete domain for PACER";

    @Override
    public void initialise() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        OWLModelManager modelManager = getOWLModelManager();
        OWLOntology ontology = modelManager.getActiveOntology();
        OWLDataFactory factory = getOWLDataFactory();
        OWLAnnotationProperty concreteDomain =
                factory.getOWLAnnotationProperty(IRI.create(CDAnnotationNames.CONCRETE_DOMAIN_ANNOTATION_PROP));
        OWLAnnotationProperty constraint =
                factory.getOWLAnnotationProperty(IRI.create(CDAnnotationNames.CONSTRAINT_ANNOTATION_PROP));

        OWLDocumentFormat format = ontology.getOWLOntologyManager().getOntologyFormat(ontology);
        PrefixDocumentFormat prefixes = format instanceof PrefixDocumentFormat ? (PrefixDocumentFormat) format : null;
        if (prefixes != null && prefixes.containsPrefixMapping(PREFIX)
                && !CDAnnotationNames.NAMESPACE.equals(prefixes.getPrefix(PREFIX))) {
            JOptionPane.showMessageDialog(getOWLWorkspace(), "The prefix " + PREFIX + " stands for "
                    + prefixes.getPrefix(PREFIX) + " in this ontology, but PACER needs it for "
                    + CDAnnotationNames.NAMESPACE + ".\nPlease change it in the tab \"Active ontology\" first.",
                    TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }

        ConcreteDomainName domain = askForDomain(currentDomain(ontology, concreteDomain));
        if (domain == null)
            return;

        List<OWLOntologyChange> changes = new ArrayList<>();
        for (OWLAnnotationProperty property : new OWLAnnotationProperty[]{concreteDomain, constraint}) {
            OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom(property);
            if (!ontology.containsAxiom(declaration))
                changes.add(new AddAxiom(ontology, declaration));
        }
        for (OWLAnnotation annotation : ontology.getAnnotations())
            if (annotation.getProperty().equals(concreteDomain))
                changes.add(new RemoveOntologyAnnotation(ontology, annotation));
        changes.add(new AddOntologyAnnotation(ontology,
                factory.getOWLAnnotation(concreteDomain, factory.getOWLLiteral(domain.name()))));
        modelManager.applyChanges(changes);

        if (prefixes != null && !prefixes.containsPrefixMapping(PREFIX)) {
            prefixes.setPrefix(PREFIX, CDAnnotationNames.NAMESPACE);
            modelManager.setDirty(ontology);
            modelManager.refreshRenderer();
        }
    }

    /** the concrete domain the ontology has now, or null if it has none or an unknown one */
    private static ConcreteDomainName currentDomain(OWLOntology ontology, OWLAnnotationProperty concreteDomain) {
        for (OWLAnnotation annotation : ontology.getAnnotations())
            if (annotation.getProperty().equals(concreteDomain) && annotation.getValue() instanceof OWLLiteral) {
                String name = ((OWLLiteral) annotation.getValue()).getLiteral();
                if (ConcreteDomainName.isName(name))
                    return ConcreteDomainName.getConcreteDomainName(name);
            }
        return null;
    }

    /** the concrete domain chosen in the dialog, or null if it was cancelled */
    private ConcreteDomainName askForDomain(ConcreteDomainName current) {
        JComboBox<ConcreteDomainName> domains = new JComboBox<>(ConcreteDomainName.values());
        if (current != null)
            domains.setSelectedItem(current);
        JLabel syntax = new JLabel(syntax((ConcreteDomainName) domains.getSelectedItem()));
        domains.addActionListener(e -> syntax.setText(syntax((ConcreteDomainName) domains.getSelectedItem())));

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(new JLabel("<html>PACER reads the concrete domain from the ontology annotation "
                + "<tt>cd:concreteDomain</tt>.<br>Constraints are annotations <tt>cd:constraint</tt> of classes; "
                + "they name data properties<br>with the prefixes of the ontology.</html>"), BorderLayout.NORTH);
        panel.add(domains, BorderLayout.CENTER);
        panel.add(syntax, BorderLayout.SOUTH);
        int answer = JOptionPane.showConfirmDialog(getOWLWorkspace(), panel, TITLE, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return answer == JOptionPane.OK_OPTION ? (ConcreteDomainName) domains.getSelectedItem() : null;
    }

    /** the forms of constraints in the concrete domain, with examples */
    private static String syntax(ConcreteDomainName domain) {
        switch (domain) {
            case QDiff:
                return "<html>Constraints: <tt>x = c</tt>, <tt>x &gt; c</tt>, <tt>x + c = y</tt>, <tt>x - c = y</tt>, "
                        + "<tt>def(x)</tt><br>e.g. <tt>:age &gt; 17</tt> or <tt>:end - 2 = :start</tt></html>";
            case QMult:
                return "<html>Constraints: <tt>x = c</tt>, <tt>x &lt; c</tt>, <tt>c * x = y</tt> with c &gt; 0, "
                        + "<tt>def(x)</tt><br>e.g. <tt>:weight &lt; 80</tt> or <tt>2 * :radius = :diameter</tt></html>";
            case QLinear:
            default:
                return "<html>Constraints: <tt>a1 * x1 + ... + an * xn = c</tt>, <tt>def(x)</tt><br>"
                        + "e.g. <tt>:water + :milk - :total = 0</tt></html>";
        }
    }
}
