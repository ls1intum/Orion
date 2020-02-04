package de.tum.www1.orion.bridge;

import netscape.javascript.JSObject;

public interface JavaUpcallBridge {
    void attachTo(JSObject javascriptObject, String memberName);
}
