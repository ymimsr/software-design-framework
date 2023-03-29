package ru.nsu.fit.multiple_inheritance_example;

public class B implements ISomeInterface {

    private int b;

    public B(int b) {
        this.b = b;
    }

    @Override
    public void justHi() {
        System.out.println("B says hi! b value is " + this.b);
    }

    @Override
    public void hi(String name) {
        System.out.println("B says hi to" + name);
    }
}
