package ru.nsu.fit.multiple_inheritance_example;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException {
        ISomeInterfaceFactory.getF("a", 1, "sC", 2).hi("Mate");
        ISomeInterfaceFactory.getG("d", "a", 1, "sC", 3).justHi();
    }
}
