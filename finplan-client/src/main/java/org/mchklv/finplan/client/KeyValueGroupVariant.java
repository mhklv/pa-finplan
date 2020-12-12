package org.mchklv.finplan.client;

import org.mchklv.finplan.common.KeyValue;
import org.mchklv.finplan.common.KeyValueGroup;

public class KeyValueGroupVariant {
    private KeyValueGroup keyValueGroup;
    private KeyValue keyValue;

    public KeyValueGroupVariant() {
        
    }

    public KeyValueGroupVariant(KeyValueGroup keyValueGroup) {
        this.setContent(keyValueGroup);
    }

    public KeyValueGroupVariant(KeyValue keyValue) {
        this.setContent(keyValue);
    }

    public void setContent(KeyValueGroup keyValueGroup) {
        this.keyValueGroup = keyValueGroup;
        this.keyValue = null;
    }

    public void setContent(KeyValue keyValue) {
        this.keyValue = keyValue;
        this.keyValueGroup = null;
    }

    public KeyValueGroup getKeyValueGroup() {
        return keyValueGroup;
    }

    public KeyValue getKeyValue() {
        return keyValue;
    }

    public boolean isEmpty() {
        return keyValue == null && keyValueGroup == null;
    }

    public boolean isKeyValue() {
        return keyValue != null;
    }

    public boolean isKeyValueGroup() {
        return keyValueGroup != null;
    }
}
