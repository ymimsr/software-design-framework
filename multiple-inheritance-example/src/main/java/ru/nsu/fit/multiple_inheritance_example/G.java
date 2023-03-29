package ru.nsu.fit.multiple_inheritance_example;

import ru.nsu.fit.multiple_inheritance_module.annotation.Extends;

@Extends(superClasses = {D.class, E.class})
public class G implements ISomeInterface {

    @Override
    public void justHi() {
        System.out.println("G says hi and passes greeting to its parent");
        nextJustHi();
        System.out.println("And another parent");
        nextJustHi();
    }
}
