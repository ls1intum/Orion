package de.tum.www1.orion.connector;

import netscape.javascript.JSObject;

public interface JavaUpcallBridge {
    void attachTo(JSObject javascriptObject, String memberName);
}
