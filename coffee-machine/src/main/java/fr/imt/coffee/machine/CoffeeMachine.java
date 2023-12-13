package fr.imt.coffee.machine;

import fr.imt.coffee.machine.component.*;
import fr.imt.coffee.machine.exception.CannotMakeCremaWithSimpleCoffeeMachine;
import fr.imt.coffee.machine.exception.CoffeeTypeCupDifferentOfCoffeeTypeTankException;
import fr.imt.coffee.machine.exception.LackOfWaterInTankException;
import fr.imt.coffee.machine.exception.MachineNotPluggedException;
import fr.imt.coffee.storage.cupboard.coffee.type.CoffeeType;
import fr.imt.coffee.storage.cupboard.container.*;
import fr.imt.coffee.storage.cupboard.exception.CupNotEmptyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class CoffeeMachine {

    public static final Logger logger = LogManager.getLogger(CoffeeMachine.class);

    protected final BeanTank beanTank;
    private final WaterTank waterTank;
    private final WaterPump waterPump;
    private final CoffeeGrinder coffeeGrinder;
    private final ElectricalResistance electricalResistance;
    private boolean isPlugged;
    private boolean isOutOfOrder;
    private int nbCoffeeMade;
    private Random randomGenerator;

    public CoffeeMachine(double minCoffeeBeanTank, double maxCoffeeBeanTank, double minWaterTank, double maxWaterTank,
                         double pumpingCapacity){
        this.beanTank = new BeanTank(0, minCoffeeBeanTank, maxCoffeeBeanTank, null);
        this.waterTank = new WaterTank(0, minWaterTank, maxWaterTank);
        this.waterPump = new WaterPump(pumpingCapacity/3600); //On convertit les L/h en L/seconde
        this.electricalResistance = new ElectricalResistance(1000);
        this.coffeeGrinder = new CoffeeGrinder(2000);
        this.isPlugged = false;
        this.isOutOfOrder = false;
        this.nbCoffeeMade = 15;
        this.randomGenerator = new Random();
    }

    /**
     * Branche la machine à café au réseau électrique
     */
    public void plugToElectricalPlug(){
        isPlugged = true;
    }

    /**
     * RAZ de la machine quand elle est en défaut
     */
    public void reset(){
        isOutOfOrder = false;
    }

    /**
     * Ajoute de l'eau dans le réservoir
     * @param waterVolume Volume d'eau en litres à ajouter
     */
    public void addWaterInTank(double waterVolume){
        this.waterTank.increaseVolumeInTank(waterVolume);
    }

    public void addCoffeeInBeanTank(double coffeeVolume, CoffeeType coffeeType){
        beanTank.increaseCoffeeVolumeInTank(coffeeVolume, coffeeType);
    }

    /**
     * Permet de faire couler un café à partir d'un contenant et d'un type de café
     * Doit retourner une instance d'un objet CoffeeMug ou CoffeeCup quand un Mug ou un Cup est passé en paramètre
     * Le contenant du café retourné ne doit pas être vide et doit avoir la même capacité que le contenant passsé en paramètre
     * Le contenant doit également avoir son coffeeType qui est égale au type de café passé en paramètre
     * Le nombre de café de la machine doit être incrémenté de 1
     * @param container Contenant pour faire couler le café
     * @param coffeeType Type de café dans l'énumération CoffeeType.java
     * @return Contenant non vide avec son type de café
     * @throws LackOfWaterInTankException Exception à lever lorsque que l'on manque d'eau dans le réservoir, message "You must add more water in the water tank."
     * @throws MachineNotPluggedException Exception levée lorsque que la machine n'est pas branchée, message : "You must plug your coffee machine to an electrical plug."
     * @throws CupNotEmptyException Exception levée lorsque le contenant donné en paramètre n'est pas vide, message : "The container given is not empty."
     * @throws InterruptedException Exception levée lorsqu'un problème survient dans les Threads lors du sleep
     * @throws CoffeeTypeCupDifferentOfCoffeeTypeTankException Exception levée lorsque le café souhaité est différent de celui chargé dans le réservoir de la cafetière
     * @throws CannotMakeCremaWithSimpleCoffeeMachine Exception levée lorsque vous souhaitez faire un café type Crema avec un une machine classique
     */
    public CoffeeContainer makeACoffee(Container container, CoffeeType coffeeType) throws LackOfWaterInTankException, InterruptedException, MachineNotPluggedException, CupNotEmptyException, CoffeeTypeCupDifferentOfCoffeeTypeTankException, CannotMakeCremaWithSimpleCoffeeMachine {
        if(isPlugged){
            throw new MachineNotPluggedException("You must plug your coffee machine.");
        }

        if (waterTank.getActualVolume() < container.getCapacity()){
            throw new LackOfWaterInTankException("You must add more water in the water tank.");
        }

        if (!container.isEmpty()){
            throw new LackOfWaterInTankException("You must add more water in the water tank.");
        }

        if(coffeeType != this.beanTank.getBeanCoffeeType()){
            throw new CoffeeTypeCupDifferentOfCoffeeTypeTankException("The type of coffee to be made in the cup is different from that in the tank.");
        }

        coffeeMachineFailure();

        if(isOutOfOrder){
            logger.warn("The machine is out of order. Please reset the coffee machine");
            return null;
        }

        if(coffeeType.toString().contains("_CREMA")){
            throw new CannotMakeCremaWithSimpleCoffeeMachine("You cannot make an espresso with a CoffeeMachine, please use EspressoCoffeeMachine");
        }

        electricalResistance.waterHeating(container.getCapacity());
        waterPump.pumpWater(container.getCapacity(), waterTank);
        coffeeGrinder.grindCoffee(this.beanTank);

        CoffeeContainer coffeeContainer = null;
        if(container instanceof Cup)
            coffeeContainer = new CoffeeCup(container, coffeeType);
        if(container instanceof Mug)
            coffeeContainer = new CoffeeMug((Mug) container, coffeeType);

        coffeeContainer.setEmpty(true);
        return coffeeContainer;
    }

    /**
     * Tirage aléatoire d'un nombre en suivant une loi normale (loi régissant les pannes).
     * Permet de simuler une panne sur la cafetière. Probabilité d'une panne d'environ 32% (1*Omega)
     * Si la valeur absolue du double tiré est supérieure ou égale à 1 alors une on considère une panne
     */
    public void coffeeMachineFailure(){
        double nxt = randomGenerator.nextGaussian();

        isOutOfOrder = (Math.abs(nxt) >= 1);
    }

    public String toString(){
        return "Your coffee machine has : \n" +
        "- water tank : " + waterTank.toString() + "\n" +
        "- water pump : " + waterPump.toString() + "\n" +
        "- electrical resistance : " + electricalResistance + "\n" +
        "- is plugged : " + isPlugged + "\n"+
        "and made " + nbCoffeeMade + " coffees";
    }

    public WaterTank getWaterTank() {
        return waterTank;
    }

    public WaterPump getWaterPump() {
        return waterPump;
    }

    public ElectricalResistance getElectricalResistance() {
        return electricalResistance;
    }

    public boolean isPlugged() {
        return isPlugged;
    }

    public boolean isOutOfOrder() {
        return isOutOfOrder;
    }

    public void setOutOfOrder(boolean outOfOrder) {
        isOutOfOrder = outOfOrder;
    }

    public int getNbCoffeeMade() {
        return nbCoffeeMade;
    }

    public void setNbCoffeeMade(int nbCoffeeMade) {
        this.nbCoffeeMade = nbCoffeeMade;
    }

    public Random getRandomGenerator() {
        return randomGenerator;
    }

    public void setRandomGenerator(Random randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public CoffeeGrinder getCoffeeGrinder() {
        return coffeeGrinder;
    }
    public BeanTank getBeanTank() {
        return beanTank;
    }
}
