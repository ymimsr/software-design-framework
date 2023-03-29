package ru.nsu.fit.multiple_inheritance_example;

import ru.nsu.fit.multiple_inheritance_module.annotation.Extends;

@Extends(superClasses = {A.class, B.class})
public class D implements ISomeInterface {

    private String d;

    public D(String d) {
        this.d = d;
    }
}
