package ru.nsu.fit.multiple_inheritance_module.domain;

import java.util.Map;

public class ConstructorData {

    private String className;
    private Map<String, VariableData[]> constParameters;
    private VariableData[] selfConstParameters;
    private VariableData[] allConstParameters;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, VariableData[]> getConstParameters() {
        return constParameters;
    }

    public void setConstParameters(Map<String, VariableData[]> constParameters) {
        this.constParameters = constParameters;
    }

    public VariableData[] getSelfConstParameters() {
        return selfConstParameters;
    }

    public void setSelfConstParameters(VariableData[] selfConstParameters) {
        this.selfConstParameters = selfConstParameters;
    }

    public VariableData[] getAllConstParameters() {
        return allConstParameters;
    }

    public void setAllConstParameters(VariableData[] allConstParameters) {
        this.allConstParameters = allConstParameters;
    }
}
