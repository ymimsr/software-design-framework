package ru.nsu.fit.multiple_inheritance_module.domain;

public class MethodData {

    private String returnType;
    private String methodName;
    private VariableData[] parameterTypes;

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public VariableData[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(VariableData[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
