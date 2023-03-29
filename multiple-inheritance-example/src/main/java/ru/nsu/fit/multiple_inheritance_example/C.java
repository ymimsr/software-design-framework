package ru.nsu.fit.multiple_inheritance_example;

import ru.nsu.fit.multiple_inheritance_module.annotation.Extends;

// TODO: lookup javaagent
public class C implements ISomeInterface {

    private String sC;
    private int sI;

    public C(String sC, int sI) {
        this.sC = sC;
        this.sI = sI;
    }

    public C(String sC) {
        this.sC = sC;
    }


    @Override
    public void justHi() {
        System.out.println("C says hi! " + sC);
    }

    @Override
    public void hi(String name) {
        System.out.println("C says hi to" + name);
    }

}
