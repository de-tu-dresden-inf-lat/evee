package de.tu_dresden.inf.lat.evee.protege.nonEntailment.abduction;

import org.protege.editor.core.ui.preferences.PreferencesLayoutPanel;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

import javax.swing.*;
import java.awt.*;

public class AbductionGeneralPreferencesUI extends OWLPreferencesPanel {

    private final AbductionGeneralPreferencesManager preferencesManager;
    protected static final String MAX_HYPOTHESIS_LABEL = "Maximal number of new hypotheses:";
    protected static final String MAX_HYPOTHESIS_SPINNER_TOOLTIP =
            "Number of hypotheses to be generated in each computation step";
    private JSpinner abductionNumberSpinner;

    public AbductionGeneralPreferencesUI(){
        this.preferencesManager = new AbductionGeneralPreferencesManager();
    }

    @Override
    public void applyChanges() {
        this.preferencesManager.saveMaximumHypothesisNumber((int) this.abductionNumberSpinner.getValue());
    }

    @Override
    public void initialise() throws Exception {
        SwingUtilities.invokeLater(() -> {
            this.setLayout(new BorderLayout());
            PreferencesLayoutPanel holderPanel = new PreferencesLayoutPanel();
            this.add(holderPanel, BorderLayout.NORTH);
            holderPanel.addGroup(MAX_HYPOTHESIS_LABEL);
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, null, 1);
            this.abductionNumberSpinner = new JSpinner(spinnerModel);
            this.abductionNumberSpinner.setToolTipText(MAX_HYPOTHESIS_SPINNER_TOOLTIP);
            this.abductionNumberSpinner.setPreferredSize(new Dimension(
                    100, (int) this.abductionNumberSpinner.getPreferredSize().getHeight()));
            holderPanel.addGroupComponent(this.abductionNumberSpinner);
        });

    }

    @Override
    public void dispose() throws Exception {

    }

}
