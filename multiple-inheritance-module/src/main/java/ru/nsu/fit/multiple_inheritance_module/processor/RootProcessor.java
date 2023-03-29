package ru.nsu.fit.multiple_inheritance_module.processor;

import com.google.auto.service.AutoService;
import ru.nsu.fit.multiple_inheritance_module.ClassBuilder;
import ru.nsu.fit.multiple_inheritance_module.annotation.Extends;
import ru.nsu.fit.multiple_inheritance_module.annotation.Root;
import ru.nsu.fit.multiple_inheritance_module.domain.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SupportedAnnotationTypes(
        "ru.nsu.fit.multiple_inheritance_module.annotation.Root")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RootProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

            Map<Boolean, List<Element>> partitionMap = annotatedElements.stream()
                    .collect(Collectors.partitioningBy(element ->
                            element.getKind().isInterface() &&
                                    // no parents
                                    processingEnv.getTypeUtils().directSupertypes(element.asType()).get(0).toString()
                                            .equals("java.lang.Object")
                    ));

            List<Element> validElements = partitionMap.get(true);
            List<Element> invalidElements = partitionMap.get(false);

            invalidElements.forEach(element ->
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "@Root must be applied to an interface")
            );

            validElements.forEach(annotatedRootClass -> {
                RootData rootData = scanRootClass(annotatedRootClass);
                String[] packagesToScan = annotatedRootClass.getAnnotation(Root.class).basePackages();
                if (packagesToScan.length == 0)
                    packagesToScan = new String[]{Optional.ofNullable(rootData.getPackageName()).orElse("")};

                Map<String, ClassData> classesData = Arrays.stream(packagesToScan)
                        .map(packageName ->
                                scanPackage(rootData.getUserRootName(), packageName, rootData.getMethods()))
                        .flatMap(packageClassesData -> packageClassesData.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                postScanClass(classesData);

                ClassBuilder classBuilder = new ClassBuilder(rootData, classesData, processingEnv.getFiler());
                classBuilder.build();

                System.out.println(classesData);
            });

        }

        return true;
    }

    private Map<String, ClassData> scanPackage(
            String rootClassName,
            String packageName,
            MethodData[] rootMethods
    ) {
        PackageElement packageToScan = processingEnv.getElementUtils().getPackageElement(packageName);

        return packageToScan
                .getEnclosedElements()
                .stream()
                .map(element -> (TypeElement) element)
                .filter(element -> element.getInterfaces().stream()
                        .anyMatch(interfaceElement ->
                                ((DeclaredType) interfaceElement).asElement().getSimpleName().contentEquals(rootClassName)
                        ))
                .map(typeElement -> scanClass(typeElement, rootMethods))
                .collect(Collectors.toMap(ClassData::getClassName, classData -> classData));
    }

    private void postScanClass(Map<String, ClassData> classDataMap) {
        classDataMap.forEach((key, value) -> {
            if (value.getConstructors() == null)
                postScanSingleClass(classDataMap, value);
        });
    }

    private List<ConstructorData> postScanSingleClass(Map<String, ClassData> classDataMap,
                                     ClassData classData) {
        if (classData.getConstructors() != null)
            return classData.getConstructors();

        if (classData.getSuperclassNames().length == 0) {
            if (classData.getConstructors() == null) {
                List<ConstructorData> constructors = new ArrayList<>();
                classData.getSelfConstParameters()
                        .forEach(selfParameters -> {
                            ConstructorData constructorData = new ConstructorData();

                            constructorData.setSelfConstParameters(selfParameters);
                            constructorData.setClassName(classData.getClassName());
                            constructorData.setAllConstParameters(selfParameters);

                            constructors.add(constructorData);
                        });
                classData.setConstructors(constructors);
            }
            return classData.getConstructors();
        }

        List<List<ConstructorData>> superClassesConstructors = Arrays.stream(classData.getSuperclassNames())
                .map(superclassName -> postScanSingleClass(
                        classDataMap,
                        classDataMap.get(getSimpleClassName(superclassName))
                ))
                .collect(Collectors.toList());

        List<ConstructorData> constructorDataList = classData.getSelfConstParameters().stream()
                .map(parameters -> {
                    ConstructorData constructorData = new ConstructorData();

                    constructorData.setClassName(classData.getClassName());
                    constructorData.setSelfConstParameters(parameters);

                    return constructorData;
                }).collect(Collectors.toList());
        superClassesConstructors.add(0, constructorDataList);

        int maxIndex = superClassesConstructors.stream()
                .map(List::size)
                .reduce((x, y) -> x * y)
                .orElse(0);

        List<ConstructorData> constructors = new ArrayList<>();
        IntStream.range(0, maxIndex).forEach(index -> {
            Map<String, VariableData[]> curConstParameters = new HashMap<>();
            //final VariableData[][] curSelfConstParameters = new VariableData[1][];
            final int[] prevDivider = new int[]{1};
            IntStream.range(1, superClassesConstructors.size())
                    .map(i -> superClassesConstructors.size() - i)
                    .forEach(i -> {
                        int curListIndex = index / prevDivider[0];
                        prevDivider[0] *= superClassesConstructors.get(i).size();
                        curConstParameters.put(
                                superClassesConstructors.get(i)
                                        .get(curListIndex % superClassesConstructors.get(i).size()).getClassName(),
                                superClassesConstructors.get(i)
                                        .get(curListIndex % superClassesConstructors.get(i).size()).getAllConstParameters());
                    });

            VariableData[] selfConstParameters = superClassesConstructors.get(0)
                    .get((index / prevDivider[0]) % superClassesConstructors.get(0).size())
                    .getSelfConstParameters();

            ConstructorData constructorData = new ConstructorData();

            constructorData.setClassName(classData.getClassName());
            constructorData.setSelfConstParameters(selfConstParameters);

            Map<String, VariableData[]> copyCurConstParameters = new HashMap<>(curConstParameters);
            copyCurConstParameters.put(classData.getClassName(), selfConstParameters);
            constructorData.setAllConstParameters(copyCurConstParameters.values().stream()
                    .flatMap(Arrays::stream)
                    .toArray(VariableData[]::new));

            constructorData.setConstParameters(curConstParameters);

            constructors.add(constructorData);
        });

        classData.setConstructors(constructors);
        return constructors;
    }

    private ClassData scanClass(
            TypeElement classElement,
            MethodData[] rootMethods
    ) {
        ClassData classData = new ClassData();

        String className = classElement.getQualifiedName().toString();
        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        Extends extendsAnnotation = classElement.getAnnotation(Extends.class);
        String[] superclassNames;
        if (extendsAnnotation != null) {
            try {
                superclassNames = new String[]{};
                extendsAnnotation.superClasses();
            } catch (MirroredTypesException mte) {
                superclassNames = mte.getTypeMirrors().stream()
                        .map(typeMirror -> getSimpleClassName(typeMirror.toString()))
                        .toArray(String[]::new);
            }
        } else {
            superclassNames = new String[]{};
        }

        List<VariableData[]> constParameters = scanConst(classElement);

        String[] initMethods = classElement.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .map(element -> (ExecutableElement) element)
                .map(executableElement -> executableElement.getSimpleName().toString())
                .filter(methodName ->
                    Arrays.stream(rootMethods)
                            .anyMatch(rootMethod -> methodName.equals(rootMethod.getMethodName()))
                ).toArray(String[]::new);


        classData.setClassName(className.substring(lastDot + 1));
        classData.setPackageName(packageName);
        classData.setSuperclassNames(superclassNames);
        classData.setSelfConstParameters(constParameters);
        classData.setInitMethodNames(initMethods);

        return classData;
    }

    private List<VariableData[]> scanConst(TypeElement classElement) {
        return classElement.getEnclosedElements().stream()
                .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.CONSTRUCTOR)
                .map(element -> ((ExecutableElement) element))
                .map(this::scanMethodParameters)
                .collect(Collectors.toList());
    }

    private RootData scanRootClass(
            Element annotatedRootClass
    ) {
        RootData rootData = new RootData();
        String className = ((TypeElement) annotatedRootClass).getQualifiedName().toString();

        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String rootClassName = className + "Root";
        String rootSimpleClassName = rootClassName.substring(lastDot + 1);
        String classSimpleName = className.substring(lastDot + 1);

        List<? extends Element> methodElements = annotatedRootClass.getEnclosedElements();
        MethodData[] methodDataArray = new MethodData[methodElements.size()];
        final int[] index = {0};
        methodElements.forEach(methodElement -> {
            methodDataArray[index[0]] = scanMethod(methodElement);
            index[0]++;
        });

        rootData.setRootName(rootSimpleClassName);
        rootData.setPackageName(packageName);
        rootData.setUserRootName(classSimpleName);
        rootData.setMethods(methodDataArray);

        return rootData;
    }

    private MethodData scanMethod(
            Element methodElement
    ) {
        MethodData methodData = new MethodData();

        String methodName = methodElement.getSimpleName().toString();
        String returnType = ((ExecutableType) methodElement.asType()).getReturnType().toString();
        VariableData[] parameterTypes = scanMethodParameters((ExecutableElement) methodElement);

        methodData.setMethodName(methodName);
        methodData.setReturnType(returnType);
        methodData.setParameterTypes(parameterTypes);

        return methodData;
    }

    private VariableData[] scanMethodParameters(ExecutableElement method) {
        return method.getParameters().stream()
                .map(element -> (VariableElement) element)
                .map(variableElement -> {
                    VariableData variableData = new VariableData();

                    variableData.setName(variableElement.getSimpleName().toString());
                    variableData.setType(variableElement.asType().toString());

                    return variableData;
                }).toArray(VariableData[]::new);
    }

    private String getSimpleClassName(String className) {
        int lastDot = className.lastIndexOf('.');
        return className.substring(lastDot + 1);
    }


}
