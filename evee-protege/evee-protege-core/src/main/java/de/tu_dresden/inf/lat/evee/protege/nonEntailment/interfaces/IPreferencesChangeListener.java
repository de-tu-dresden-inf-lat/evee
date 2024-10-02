package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces;


import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.GeneralPreferencesChangeEvent;

public interface IPreferencesChangeListener {

    void handlePreferenceChange(GeneralPreferencesChangeEvent event);

}
