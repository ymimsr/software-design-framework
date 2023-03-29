package ru.nsu.fit.multiple_inheritance_example;

import ru.nsu.fit.multiple_inheritance_module.annotation.Root;

@Root
public interface ISomeInterface extends ISomeInterfaceRoot {

    default void justHi() {}

    default void hi(String name) {}

}
