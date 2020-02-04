package de.tum.www1.orion.bridge;

import netscape.javascript.JSObject;

public abstract class ArtemisConnector implements JavaUpcallBridge {
    @Override
    public void attachTo(JSObject javascriptObject, String memberName) {
        javascriptObject.setMember(memberName, this);
    }
}
