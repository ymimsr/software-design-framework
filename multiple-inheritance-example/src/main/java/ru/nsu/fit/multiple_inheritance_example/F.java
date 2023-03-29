package ru.nsu.fit.multiple_inheritance_example;

import ru.nsu.fit.multiple_inheritance_module.annotation.Extends;

@Extends(superClasses = {A.class, B.class, C.class})
public class F implements ISomeInterface {
}
