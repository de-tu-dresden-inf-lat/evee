package de.tu_dresden.inf.lat.evee.protege.nonEntailment.interfaces;

import de.tu_dresden.inf.lat.evee.protege.tools.eventHandling.GeneralPreferencesChangeEventType;

public interface PreferencesChangeListener {

    void handlePreferenceChange(GeneralPreferencesChangeEventType eventType);

}