package ru.nsu.fit.multiple_inheritance_module.domain;

import java.util.List;
import java.util.Map;

public class ClassData {

    private String className;
    private List<VariableData[]> selfConstParameters;
    private List<ConstructorData> constructors;
    private String packageName;
    private String[] superclassNames;
    private String[] initMethodNames;

    public List<ConstructorData> getConstructors() {
        return constructors;
    }

    public void setConstructors(List<ConstructorData> constructors) {
        this.constructors = constructors;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<VariableData[]> getSelfConstParameters() {
        return selfConstParameters;
    }

    public void setSelfConstParameters(List<VariableData[]> selfConstParameters) {
        this.selfConstParameters = selfConstParameters;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String[] getSuperclassNames() {
        return superclassNames;
    }

    public void setSuperclassNames(String[] superclassNames) {
        this.superclassNames = superclassNames;
    }

    public String[] getInitMethodNames() {
        return initMethodNames;
    }

    public void setInitMethodNames(String[] initMethodNames) {
        this.initMethodNames = initMethodNames;
    }
}
