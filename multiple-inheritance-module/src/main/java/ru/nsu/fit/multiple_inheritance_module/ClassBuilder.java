package ru.nsu.fit.multiple_inheritance_module;

import ru.nsu.fit.multiple_inheritance_module.domain.ClassData;
import ru.nsu.fit.multiple_inheritance_module.domain.MethodData;
import ru.nsu.fit.multiple_inheritance_module.domain.RootData;
import ru.nsu.fit.multiple_inheritance_module.domain.VariableData;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ClassBuilder {

    private RootData rootData;
    private Map<String, ClassData> classDataMap;
    private Filer filer;

    public ClassBuilder(RootData rootData, Map<String, ClassData> classDataMap, Filer filer) {
        this.rootData = rootData;
        this.classDataMap = classDataMap;
        this.filer = filer;
    }

    public void build() {
        try {
            buildRoot();
            for (ClassData classData : classDataMap.values()) {
                if (classData.getSuperclassNames() != null && classData.getSuperclassNames().length > 0)
                    buildClassProxy(classData);
            }
            buildStaticFactory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildStaticFactory() throws IOException {
        String factoryName = buildNewName(rootData.getUserRootName(), null, "Factory");

        JavaFileObject javaFileObject = filer.createSourceFile(
                (rootData.getPackageName() == null ? "" : rootData.getPackageName() + ".")
                        + factoryName);
        try (PrintWriter out = new PrintWriter(javaFileObject.openWriter())) {
            buildPackage(out, rootData.getPackageName());

            out.print("public class ");
            out.print(factoryName);
            out.println(" {");
            out.println();

            classDataMap.values().stream()
                    .filter(classData -> classData.getSuperclassNames() != null
                            && classData.getSuperclassNames().length > 0)
                    .map(ClassData::getConstructors)
                    .forEach(constructors -> constructors.forEach(constructorData -> {
                        out.print("    ");
                        out.print("public static ");
                        out.print(constructorData.getClassName());
                        out.print(" get");
                        out.print(constructorData.getClassName());

                        buildMethodParameters(out, constructorData.getAllConstParameters());
                        out.println(" {");

                        out.print("    ");
                        out.print("    ");
                        out.print("return new ");
                        out.print(buildNewName(constructorData.getClassName(), null, "Proxy"));
                        buildArgumentsToMethod(out, constructorData.getAllConstParameters());
                        out.println();

                        out.print("    ");
                        out.println("}");
                        out.println();
                    }));

            out.println("}");
        }
    }

    private void buildClassProxy(ClassData classData) throws IOException {
        String proxyClassName = buildNewName(classData.getClassName(), null, "Proxy");
        JavaFileObject javaFileObject = filer.createSourceFile(
                (classData.getPackageName() == null ? "" : classData.getPackageName() + ".") + proxyClassName);

        try (PrintWriter out = new PrintWriter(javaFileObject.openWriter())) {
            buildPackage(out, classData.getPackageName());

            out.print("public class ");
            out.print(proxyClassName);
            out.print(" extends " + classData.getClassName());
            out.println(" {");
            out.println();

            buildSuperClassesFields(out, classData.getSuperclassNames());
            buildConstructors(out, classData);

            String[] notImplMethods = Arrays.stream(rootData.getMethods())
                    .map(MethodData::getMethodName)
                    .filter(rootMethodName -> Arrays.stream(classData.getInitMethodNames()).noneMatch(implMethodName ->
                            implMethodName.equals(rootMethodName)
                    )).toArray(String[]::new);
            buildNotImplMethods(out, classData, notImplMethods);
            buildImplMethod(out, classData.getInitMethodNames());
            buildNextMethods(out, classData, classData.getInitMethodNames());

            out.println("}");
        }
    }

    private void buildImplMethod(PrintWriter out, String[] implMethodNames) {
        Arrays.stream(implMethodNames)
                .forEach(implMethodName -> {
                    MethodData implMethod = Arrays.stream(rootData.getMethods())
                            .filter(methodData -> methodData.getMethodName().equals(implMethodName))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Shouldn't be thrown"));

                    out.print("    ");
                    out.print("private int ");
                    out.print(buildNewName(implMethodName, null, "Index"));
                    out.println(" = 0;");

                    out.print("    ");
                    out.println("@Override");

                    out.print("    ");
                    out.print("public ");
                    buildMethodSignature(out, implMethod);
                    out.println(" {");

                    out.print("    ");
                    out.print("    ");
                    out.print("this.");
                    out.print(buildNewName(implMethodName, null, "Index"));
                    out.println(" = 0;");

                    out.print("    ");
                    out.print("    ");
                    out.print("super.");
                    out.print(implMethodName);

                    buildArgumentsToMethod(out, implMethod.getParameterTypes());
                    out.println("");

                    out.print("    ");
                    out.println("}");
                    out.println();
                });
    }

    private void buildNextMethods(PrintWriter out, ClassData classData, String[] methods) {
        Arrays.stream(methods)
                .forEach(methodName -> {
                    MethodData method = Arrays.stream(rootData.getMethods())
                            .filter(methodData -> methodData.getMethodName().equals(methodName))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Shouldn't be thrown"));

                    String nextMethodName = buildNewName(methodName, "next", null);

                    out.print("    ");
                    out.println("@Override");

                    MethodData nextMethod = new MethodData();
                    nextMethod.setMethodName(nextMethodName);
                    nextMethod.setReturnType(method.getReturnType());
                    nextMethod.setParameterTypes(method.getParameterTypes());

                    out.print("    ");
                    out.print("public ");
                    buildMethodSignature(out, nextMethod);
                    out.println(" {");

                    out.print("    ");
                    out.print("    ");
                    out.print("if (this.");
                    out.print(buildNewName(methodName, null, "Index"));
                    out.println(" >= superClasses.length)");

                    out.print("    ");
                    out.print("    ");
                    out.print("    ");
                    out.println("throw new IllegalStateException(\"There is no next method\");");

                    out.print("    ");
                    out.print("    ");
                    if (!method.getReturnType().equals("void"))
                        out.print("return ");
                    out.print("superClasses[");
                    out.print("this.");
                    out.print(buildNewName(methodName, null, "Index"));
                    out.print("++].");
                    out.print(methodName);
                    buildArgumentsToMethod(out, method.getParameterTypes());
                    out.println();

                    out.print("    ");
                    out.println("}");
                    out.println();
                });
    }

    private void buildNotImplMethods(PrintWriter out, ClassData classData, String[] notImplMethodNames) {
        Arrays.stream(notImplMethodNames)
                .forEach(notImplMethodName -> {
                    MethodData notImplMethod = Arrays.stream(rootData.getMethods())
                            .filter(methodData -> methodData.getMethodName().equals(notImplMethodName))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Shouldn't be thrown"));

                    out.print("    ");
                    out.println("@Override");

                    out.print("    ");
                    out.print("public ");
                    buildMethodSignature(out, notImplMethod);
                    out.println(" {");

                    int[] index = new int[]{0};
                    Arrays.stream(classData.getSuperclassNames())
                            .forEach(superClassName -> {
                                out.print("    ");
                                out.print("    ");
                                if (index[0] == classData.getSuperclassNames().length - 1
                                        && !notImplMethod.getReturnType().equals("void"))
                                    out.print("return ");
                                out.print("this.");
                                out.print(buildNewName(superClassName, null, "Field"));
                                out.print(".");
                                out.print(notImplMethodName);

                                buildArgumentsToMethod(out, notImplMethod.getParameterTypes());
                                out.println();
                            });

                    out.print("    ");
                    out.println("}");
                    out.println();
                });
    }

    private void buildConstructors(PrintWriter out, ClassData classData) {
        classData.getConstructors()
                .forEach(constructorData -> {
                    out.print("    ");
                    out.print("public ");
                    out.print(buildNewName(classData.getClassName(), null, "Proxy"));
                    buildMethodParameters(out, constructorData.getAllConstParameters());
                    out.println(" {");

                    if (constructorData.getSelfConstParameters() != null
                            && constructorData.getSelfConstParameters().length > 0) {
                        out.print("    ");
                        out.print("    ");
                        out.print("super(");
                        int[] index = new int[]{0};
                        Arrays.stream(constructorData.getSelfConstParameters())
                                .forEach(variableData -> {
                                    out.print(variableData.getName());
                                    if (index[0] != constructorData.getSelfConstParameters().length - 1) {
                                        out.print(", ");
                                    }

                                    index[0]++;
                                });
                        out.println(");");
                    }

                    for (Map.Entry<String, VariableData[]> pair : constructorData.getConstParameters().entrySet()) {
                        out.print("    ");
                        out.print("    ");
                        out.print("this.");
                        out.print(buildNewName(pair.getKey(), null, "Field"));
                        out.print(" = new ");
                        if (classDataMap.get(pair.getKey()).getSuperclassNames() != null
                                && classDataMap.get(pair.getKey()).getSuperclassNames().length > 0) {
                            out.print(buildNewName(pair.getKey(), null, "Proxy"));
                        } else {
                            out.print(pair.getKey());
                        }
                        out.print("(");
                        int ind = 0;
                        for (VariableData variableData : pair.getValue()) {
                            out.print(variableData.getName());
                            if (ind != pair.getValue().length - 1)
                                out.print(", ");
                            ind++;
                        }
                        out.println(");");
                    }

                    out.print("    ");
                    out.print("    ");
                    out.print("this.superClasses = new ");
                    out.print(rootData.getUserRootName());
                    out.print("[");
                    out.print(classData.getSuperclassNames().length);
                    out.println("];");

                    final int[] ind = {0};
                    Arrays.stream(classData.getSuperclassNames())
                            .forEach(superClassName -> {
                                out.print("    ");
                                out.print("    ");
                                out.print("superClasses[");
                                out.print(ind[0]++);
                                out.print("] = this.");
                                out.print(buildNewName(superClassName, null, "Field"));
                                out.println(";");
                            });

                    out.print("    ");
                    out.println("}");
                    out.println();
                });
    }

    private void buildSuperClassesFields(PrintWriter out, String[] superClassesNames) {
        Arrays.stream(superClassesNames)
                .forEach(superClassName -> {
                    out.print("    ");
                    out.print("private ");
                    out.print(superClassName);
                    out.print(" ");
                    out.print(buildNewName(superClassName, null,"Field"));
                    out.println(";");
                });

        out.print("    ");
        out.print("private ");
        out.print(rootData.getUserRootName());
        out.print("[] ");
        out.println("superClasses;");
        out.println();
    }

    private void buildRoot() throws IOException {
        JavaFileObject javaFileObject = filer.createSourceFile(
                (rootData.getPackageName() == null ? "" : rootData.getPackageName() + ".")
                        + rootData.getRootName());

        try (PrintWriter out = new PrintWriter(javaFileObject.openWriter())) {
            buildPackage(out, rootData.getPackageName());

            out.print("public interface ");
            out.print(rootData.getRootName());
            out.println(" {");
            out.println();

            Arrays.stream(rootData.getMethods())
                    .forEach(methodData -> {
                        buildDefaultMethod(out, methodData);

                        MethodData nextMethodData = new MethodData();
                        nextMethodData.setMethodName(
                                buildNewName(methodData.getMethodName(), "next", null));
                        nextMethodData.setReturnType(methodData.getReturnType());
                        nextMethodData.setParameterTypes(methodData.getParameterTypes());

                        buildDefaultMethod(out, nextMethodData);
                    });

            out.println("}");
        }
    }

    private void buildPackage(PrintWriter out, String packageName) {
        if (packageName == null)
            return;

        out.print("package ");
        out.print(packageName);
        out.println(";");
        out.println();
    }

    private void buildDefaultMethod(PrintWriter out, MethodData methodData) {
        out.print("    ");
        out.print("default ");
        buildMethodSignature(out, methodData);
        out.println(" { }");
        out.println();
    }

    private void buildMethodSignature(PrintWriter out, MethodData methodData) {
        out.print(methodData.getReturnType());
        out.print(" ");
        out.print(methodData.getMethodName());
        buildMethodParameters(out, methodData.getParameterTypes());
    }

    private void buildMethodParameters(PrintWriter out, VariableData[] parameters) {
        final int[] index = {0};
        out.print("(");
        Arrays.stream(parameters)
                .distinct()
                .forEach(variableData -> {
                    if (index[0] != 0) {
                        out.print(", ");
                    }

                    out.print(variableData.getType());
                    out.print(" ");
                    out.print(variableData.getName());

                    index[0]++;
                });
        out.print(")");
    }

    private void buildArgumentsToMethod(PrintWriter out, VariableData[] arguments) {
        out.print("(");
        int[] ind = new int[]{0};
        Arrays.stream(arguments)
                .map(VariableData::getName)
                .distinct()
                .forEach(parameterName -> {
                    if (ind[0] != 0)
                        out.print(", ");
                    out.print(parameterName);

                    ind[0]++;
                });
        out.print(");");
    }

    private void buildArgumentsToMethod(PrintWriter out, VariableData[] arguments, String postfix) {
        out.print("(");
        int[] ind = new int[]{0};
        Arrays.stream(arguments)
                .map(VariableData::getName)
                .distinct()
                .forEach(parameterName -> {
                    if (ind[0] != 0)
                        out.print(", ");
                    out.print(buildNewName(parameterName, null, postfix));

                    ind[0]++;
                });
        out.print(");");
    }

    private String buildNewName(String name, String prefix, String postfix) {
        String newName = name;

        if (prefix != null) {
            newName = prefix
                    + newName.substring(0, 1).toUpperCase(Locale.ROOT)
                    + newName.substring(1);
        }

        if (postfix != null) {
            newName = newName + postfix;
        }

        return newName;
    }

}
