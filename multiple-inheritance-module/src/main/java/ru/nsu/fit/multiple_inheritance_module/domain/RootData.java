package ru.nsu.fit.multiple_inheritance_module.domain;

public class RootData {

    private MethodData[] methods;
    private String rootName;
    private String userRootName;
    private String packageName;

    public String getUserRootName() {
        return userRootName;
    }

    public void setUserRootName(String userRootName) {
        this.userRootName = userRootName;
    }

    public MethodData[] getMethods() {
        return methods;
    }

    public void setMethods(MethodData[] methods) {
        this.methods = methods;
    }

    public String getRootName() {
        return rootName;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
