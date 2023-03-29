package ru.nsu.fit.multiple_inheritance_module;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassDispatcherBuilder {

    public void buildClassDispatcher(Filer filer, Types types, Class<?>[] superClasses, Element annotatedClass) throws IOException {

        String className = ((TypeElement) annotatedClass).getQualifiedName().toString();
        //String className = classElement.getSimpleName().toString();

        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String dispatcherClassName = className + "Dispatcher";
        String dispatcherSimpleClassName = dispatcherClassName
                .substring(lastDot + 1);

        JavaFileObject rootFile = filer.createSourceFile(dispatcherClassName);

        try (PrintWriter out = new PrintWriter(rootFile.openWriter())) {

            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(";");
                out.println();
            }

            out.print("public class ");
            out.print(dispatcherSimpleClassName);
            out.print(" extends ");
            out.print(simpleClassName);
            out.println(" {");
            out.println();

            out.print("    ");
            out.print("private ");
            out.print(annotatedClass.getSimpleName().toString());
            out.print(" ");
            out.print(toCamelCase(annotatedClass.getSimpleName().toString()));
            out.println(";");

            for (Class<?> superClass : superClasses) {
                out.print("    ");
                out.print("private ");
                out.print(superClass.getSimpleName());
                out.print(" ");
                out.print(toCamelCase(superClass.getSimpleName()));
                out.println(";");
            }

            List<? extends Element> enclosedElements = annotatedClass.getEnclosedElements();
            List<? extends Element> classMethods = enclosedElements.stream()
                    .filter(element -> element instanceof ExecutableType)
                    .collect(Collectors.toList());

            Element rootElement = types.asElement(((TypeElement) annotatedClass).getInterfaces().get(0));
            Map<Boolean, List<Element>> implMethodPartition = rootElement.getEnclosedElements().stream()
                    .collect(Collectors.partitioningBy(element ->
                            classMethods.stream()
                                    .anyMatch(
                                            element1 -> element.getSimpleName().equals(element1.getSimpleName())
                                    )
                    ));

            List<Element> notImplementedMethods = implMethodPartition.get(false)
                    .stream()
                    .filter(element -> !element.getSimpleName().toString().startsWith("next"))
                    .collect(Collectors.toList());

            for (Element method : classMethods) {
                String methodName = method.getSimpleName().toString();
                String nextMethodName = "next"
                        + String.valueOf(method.getSimpleName().charAt(0)).toUpperCase(Locale.ROOT)
                        + method.getSimpleName().toString().substring(1);
                String returnType = ((ExecutableType) method.asType()).getReturnType().toString();
                List<String> parameterTypes = ((ExecutableType) method.asType())
                        .getParameterTypes()
                        .stream()
                        .map(TypeMirror::toString)
                        .collect(Collectors.toList());

                printMethodSignature(out, nextMethodName, returnType, parameterTypes);

                out.println("{");
                for (int k = 0; k < superClasses.length; k++) {
                    Class<?> superClass = superClasses[k];

                    out.print("        ");

                    if (k == superClasses.length - 1 && !returnType.equalsIgnoreCase("void")) {
                        out.print("return ");
                    }

                    out.print(toCamelCase(superClass.getSimpleName()));
                    out.print(".");
                    out.print(methodName);
                    out.print("(");

                    for (int i = 0; i < parameterTypes.size(); i++) {
                        out.print("par" + i);
                        if (i != parameterTypes.size() - 1)
                            out.print(", ");
                    }

                    out.println(");");
                }
                out.print("    }");
            }

            for (Element notImplementedMethod : notImplementedMethods) {
                String methodName = notImplementedMethod.getSimpleName().toString();
                String returnType = ((ExecutableType) notImplementedMethod.asType()).getReturnType().toString();
                List<String> parameterTypes = ((ExecutableType) notImplementedMethod.asType())
                        .getParameterTypes()
                        .stream()
                        .map(TypeMirror::toString)
                        .collect(Collectors.toList());

                printMethodSignature(out, methodName, returnType, parameterTypes);

                out.println("{ ");
                out.print("        ");
                out.print("next");
                out.print(
                        String.valueOf(methodName.charAt(0)).toUpperCase(Locale.ROOT) +
                                methodName.substring(1)
                        );
                out.print("(");
                for (int i = 0; i < parameterTypes.size(); i++) {
                    out.print("par" + i);
                    if (i != parameterTypes.size() - 1)
                        out.print(", ");
                }
                out.println(");");
                out.println("    }");
            }

            out.println("}");
        }

    }

    private void printMethodSignature(PrintWriter out, String methodName, String returnType, List<String> parameterTypes) {
        out.print("    ");
        out.print(returnType + " ");
        out.print(methodName);
        out.print("(");
        for (int i = 0; i < parameterTypes.size(); i++) {
            out.print(parameterTypes.get(i) + " ");
            out.print("par" + i);
            if (i != parameterTypes.size() - 1)
                out.print(", ");
        }
        out.print(") ");
    }

    private String toCamelCase(String name) {
        return String.valueOf(name.charAt(0)).toUpperCase(Locale.ROOT) +
                name.substring(1);
    }

}
