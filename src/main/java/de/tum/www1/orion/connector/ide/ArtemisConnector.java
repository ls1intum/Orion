package de.tum.www1.orion.connector.ide;

import netscape.javascript.JSObject;

public interface ArtemisConnector {
    void attachTo(JSObject jsObject, String memberName);
}
