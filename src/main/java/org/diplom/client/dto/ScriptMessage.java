package org.diplom.client.dto;

import java.util.List;

public class ScriptMessage extends Message {
    private String scriptKey;
    private List<String> scriptTable;
    private String scriptSyncMessage;

    public String getScriptKey() {
        return scriptKey;
    }

    public void setScriptKey(String scriptKey) {
        this.scriptKey = scriptKey;
    }

    public List<String> getScriptTable() {
        return scriptTable;
    }

    public void setScriptTable(List<String> scriptTable) {
        this.scriptTable = scriptTable;
    }

    public String getScriptSyncMessage() {
        return scriptSyncMessage;
    }

    public void setScriptSyncMessage(String scriptSyncMessage) {
        this.scriptSyncMessage = scriptSyncMessage;
    }
}
