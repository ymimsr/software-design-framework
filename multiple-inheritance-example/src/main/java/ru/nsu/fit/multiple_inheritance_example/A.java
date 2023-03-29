package ru.nsu.fit.multiple_inheritance_example;

public class A implements ISomeInterface {

    private String a;

    public A(String a) {
        this.a = a;
    }

    @Override
    public void justHi() {
        System.out.println("A says hi!");
    }

    @Override
    public void hi(String name) {
        System.out.println("A says hi to" + name);
    }
}
