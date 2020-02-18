package de.tum.www1.orion.connector;

import netscape.javascript.JSObject;

public abstract class ArtemisConnector implements JavaUpcallBridge {
    @Override
    public void attachTo(JSObject javascriptObject, String memberName) {
        javascriptObject.setMember(memberName, this);
    }
}
