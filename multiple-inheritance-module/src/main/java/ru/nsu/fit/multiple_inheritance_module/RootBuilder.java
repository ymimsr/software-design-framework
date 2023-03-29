package ru.nsu.fit.multiple_inheritance_module;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RootBuilder {

    public void buildRootClass(Filer filer, Element classElement) throws IOException {
        String className = ((TypeElement) classElement).getQualifiedName().toString();
        //String className = classElement.getSimpleName().toString();

        System.out.println(className);

        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String rootClassName = className + "Root";
        String rootSimpleClassName = rootClassName
                .substring(lastDot + 1);

        JavaFileObject rootFile = filer.createSourceFile(rootClassName);

        try (PrintWriter out = new PrintWriter(rootFile.openWriter())) {

            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(";");
                out.println();
            }

            out.print("public interface ");
            out.print(rootSimpleClassName);
            out.println(" {");
            out.println();

            List<? extends Element> methodElements = classElement.getEnclosedElements();
            methodElements.forEach(methodElement -> {
                        String methodName = methodElement.getSimpleName().toString();
                        String returnType = ((ExecutableType) methodElement.asType()).getReturnType().toString();
                        List<String> parameterTypes = ((ExecutableType) methodElement.asType())
                                .getParameterTypes()
                                .stream()
                                .map(TypeMirror::toString)
                                .collect(Collectors.toList());

                        printMethod(out, methodName, returnType, parameterTypes);

                        String nextMethodName = "next"
                                + String.valueOf(methodName.charAt(0)).toUpperCase(Locale.ROOT)
                                + methodName.substring(1);

                        printMethod(out, nextMethodName, returnType, parameterTypes);
                    }
            );

            out.println("}");
        }
    }

    private void printMethod(PrintWriter out, String methodName, String returnType, List<String> parameterTypes) {
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
        out.println(");");
        out.println();
    }

}
