package fr.imt.coffee.storage.cupboard.container;

import fr.imt.coffee.storage.cupboard.coffee.type.CoffeeType;

public class CoffeeMug extends CoffeeContainer{

    public CoffeeMug(double capacity, CoffeeType coffeeType) {
        super(capacity, coffeeType);
    }

    public CoffeeMug(Mug mug, CoffeeType coffeeType) {
        super(mug, coffeeType);
    }
}
