package ru.nsu.fit.multiple_inheritance_example;

import ru.nsu.fit.multiple_inheritance_module.annotation.Extends;

@Extends(superClasses = {B.class, C.class})
public class E implements ISomeInterface {
}
