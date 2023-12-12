package fr.imt.coffee.storage.cupboard.container;

import fr.imt.coffee.storage.cupboard.coffee.type.CoffeeType;

public abstract class CoffeeContainer extends Container{
    private final CoffeeType coffeeType;

    public CoffeeContainer(double capacity, CoffeeType coffeeType) {
        super(capacity);
        this.coffeeType = coffeeType;
    }

    public CoffeeContainer(Container container, CoffeeType coffeeType) {
        super(container.getCapacity());
        this.setEmpty(false);
        this.coffeeType = coffeeType;
    }

    public CoffeeType getCoffeeType() {
        return coffeeType;
    }

    public String toString(){
        return super.toString() + "\n" + "Coffee type : " + coffeeType;
    }
}
