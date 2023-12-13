package fr.imt.coffee.machine;

import fr.imt.coffee.machine.component.BeanTank;
import fr.imt.coffee.machine.component.CoffeeGrinder;
import fr.imt.coffee.machine.exception.CannotMakeCremaWithSimpleCoffeeMachine;
import fr.imt.coffee.machine.exception.CoffeeTypeCupDifferentOfCoffeeTypeTankException;
import fr.imt.coffee.machine.exception.LackOfWaterInTankException;
import fr.imt.coffee.machine.exception.MachineNotPluggedException;
import fr.imt.coffee.storage.cupboard.coffee.type.CoffeeType;
import fr.imt.coffee.storage.cupboard.container.CoffeeContainer;
import fr.imt.coffee.storage.cupboard.container.Container;
import fr.imt.coffee.storage.cupboard.container.Cup;
import fr.imt.coffee.storage.cupboard.exception.CupNotEmptyException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CoffeeMachineUnitTest {
    public CoffeeMachine coffeeMachineUnderTest;

    /**
     * @BeforeEach est une annotation permettant d'exécuter la méthode annotée avant chaque test unitaire
     * Ici avant chaque test on initialise la machine à café
     */
    @BeforeEach
    public void beforeTest(){
        coffeeMachineUnderTest = new CoffeeMachine(
                0,10,
                0,10,  700);
    }

    /**
     * On vient tester si la machine ne se met pas en défaut
     */
    @Test
    public void testMachineFailureTrue(){
        //On créé un mock de l'objet random
        Random randomMock = Mockito.mock(Random.class, Mockito.withSettings().withoutAnnotations());
        //On vient ensuite stubber la méthode nextGaussian pour pouvoir contrôler la valeur retournée
        //ici on veut qu'elle retourne 1.0
        //when : permet de définir quand sur quelle méthode établir le stub
        //thenReturn : va permettre de contrôler la valeur retournée par le stub
        Mockito.when(randomMock.nextGaussian()).thenReturn(1.0);
        //On injecte ensuite le mock créé dans la machine à café
        coffeeMachineUnderTest.setRandomGenerator(randomMock);

        //On vérifie que le booleen outOfOrder est bien à faux avant d'appeler la méthode
        Assertions.assertFalse(coffeeMachineUnderTest.isOutOfOrder());
        //Ou avec Hamcrest
        assertThat(false, is(coffeeMachineUnderTest.isOutOfOrder()));

        //on appelle la méthode qui met la machine en défaut
        //On a mocké l'objet random donc la valeur retournée par nextGaussian() sera 1
        //La machine doit donc se mettre en défaut
        coffeeMachineUnderTest.coffeeMachineFailure();

        Assertions.assertTrue(coffeeMachineUnderTest.isOutOfOrder());
        assertThat(true, is(coffeeMachineUnderTest.isOutOfOrder()));
    }

    /**
     * On vient tester si la machine se met en défaut
     */
    @Test
    public void testMachineFailureFalse(){
        //On créé un mock de l'objet random
        Random randomMock = Mockito.mock(Random.class, Mockito.withSettings().withoutAnnotations());
        //On vient ensuite stubber la méthode nextGaussian pour pouvoir contrôler la valeur retournée
        //ici on veut qu'elle retourne 0.6
        //when : permet de définir quand sur quelle méthode établir le stub
        //thenReturn : va permettre de contrôler la valeur retournée par le stub
        Mockito.when(randomMock.nextGaussian()).thenReturn(0.6);
        //On injecte ensuite le mock créé dans la machine à café
        coffeeMachineUnderTest.setRandomGenerator(randomMock);

        //On vérifie que le booleen outOfOrder est bien à faux avant d'appeler la méthode
        Assertions.assertFalse(coffeeMachineUnderTest.isOutOfOrder());
        //Ou avec Hamcrest
        assertThat(false, is(coffeeMachineUnderTest.isOutOfOrder()));

        //on appelle la méthode qui met la machine en défaut
        //On a mocker l'objet random donc la valeur retournée par nextGaussian() sera 0.6
        //La machine doit donc NE PAS se mettre en défaut
        coffeeMachineUnderTest.coffeeMachineFailure();

        Assertions.assertFalse(coffeeMachineUnderTest.isOutOfOrder());
        //Ou avec Hamcrest
        assertThat(false, is(coffeeMachineUnderTest.isOutOfOrder()));
    }

    /**
     * On test que la machine se branche correctement au réseau électrique
     */
    @Test
    public void testPlugMachine(){
        Assertions.assertFalse(coffeeMachineUnderTest.isPlugged());

        coffeeMachineUnderTest.plugToElectricalPlug();

        Assertions.assertTrue(coffeeMachineUnderTest.isPlugged());
    }

    /**
     * On test qu'une exception est bien levée lorsque que le cup passé en paramètre retourne qu'il n'est pas vide
     * Tout comme le test sur la mise en défaut afin d'avoir un comportement isolé et indépendant de la machine
     * on vient ici mocker un objet Cup afin d'en maitriser complétement son comportement
     * On ne compte pas sur "le bon fonctionnement de la méthode"
     */
    @Test
    public void testMakeACoffeeCupNotEmptyException(){
        Cup mockCup = Mockito.mock(Cup.class);
        Mockito.when(mockCup.isEmpty()).thenReturn(false);

        coffeeMachineUnderTest.plugToElectricalPlug();

        //assertThrows( [Exception class expected], [lambda expression with the method that throws an exception], [exception message expected])
        //AssertThrows va permettre de venir tester la levée d'une exception, ici lorsque que le contenant passé en
        //paramètre n'est pas vide
        //On teste à la fois le type d'exception levée mais aussi le message de l'exception
        Assertions.assertThrows(CupNotEmptyException.class, ()->{
                coffeeMachineUnderTest.makeACoffee(mockCup, CoffeeType.MOKA);
            });
    }

    /* On test que l'on ne peut pas mettre plus d'eau dans le tank que son volume maximal' */
    @Test
    public void testVolumeInTankNotHigherThanMaximum(){
        double actualVolume = coffeeMachineUnderTest.getWaterTank().getActualVolume();
        double maxVolume = coffeeMachineUnderTest.getWaterTank().getMaxVolume();
        double volumeToAdd = maxVolume + 1; //On fait en sorte que le nouveau volume excède le volume maximal

        // On s'attend à ce qu'une IllegalArgumentException soit levée
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            coffeeMachineUnderTest.getWaterTank().increaseVolumeInTank(volumeToAdd);
        });

        // On vérifie que le volume actuel reste inchangé
        Assertions.assertEquals(actualVolume, coffeeMachineUnderTest.getWaterTank().getActualVolume());
    }

    /* Pareil mais pour le minimum*/
    @Test
    public void testVolumeInTankNotLowerThanMinimum(){
        double actualVolume = coffeeMachineUnderTest.getWaterTank().getActualVolume();
        double maxVolume = coffeeMachineUnderTest.getWaterTank().getMaxVolume();
        double volumeToRemove = maxVolume + 1; //On fait en sorte d'enlever plus que la capacité du tank

        // On s'attend à ce qu'une IllegalArgumentException soit levée
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            coffeeMachineUnderTest.getWaterTank().decreaseVolumeInTank(volumeToRemove);
        });

        // On vérifie que le volume actuel reste inchangé
        Assertions.assertEquals(actualVolume, coffeeMachineUnderTest.getWaterTank().getActualVolume());
    }

    /* On test les conditions initiales de la machine à cafe : elle n'est pas branchée, prête à être utilisée, aucun café n'a encore été fait*/
    @Test
    public void testInitialization() {
        Assertions.assertFalse(coffeeMachineUnderTest.isPlugged());
        Assertions.assertFalse(coffeeMachineUnderTest.isOutOfOrder());
        Assertions.assertEquals(0, coffeeMachineUnderTest.getNbCoffeeMade());
        Assertions.assertNotNull(coffeeMachineUnderTest.getRandomGenerator());
    }

    /* Test de l'ajout d'eau dans le réservoir*/
    @Test
    public void testAddWaterInTank() {
        double initialWaterVolume = coffeeMachineUnderTest.getWaterTank().getActualVolume();

        coffeeMachineUnderTest.addWaterInTank(2);

        Assertions.assertEquals(initialWaterVolume + 2, coffeeMachineUnderTest.getWaterTank().getActualVolume());
    }

    /* Test ajout de café dans le bean tank*/
    @Test
    public void testAddCoffeeInBeanTank() {
        double initialCoffeeVolume = coffeeMachineUnderTest.getBeanTank().getActualVolume();

        CoffeeType mockCoffeeType = Mockito.mock(CoffeeType.class);
        Mockito.when(mockCoffeeType.toString()).thenReturn("ARABICA");

        coffeeMachineUnderTest.addCoffeeInBeanTank(1, mockCoffeeType);

        Assertions.assertEquals(initialCoffeeVolume + 1, coffeeMachineUnderTest.getBeanTank().getActualVolume());
    }

    /* Test que lorsqu'on fait un café, le contenant du café retourné ne doit pas être vide et doit avoir la même capacité que le contenant passé en paramètre*/
    @Test
    public void testContainerCapacityAfterMakingCoffee(){
        Container mockContainer = Mockito.mock(Container.class);

        Mockito.when(mockContainer.getCapacity()).thenReturn(1.0);
        Mockito.when(mockContainer.isEmpty()).thenReturn(true);

        CoffeeType mockCoffeeType = Mockito.mock(CoffeeType.class);

        coffeeMachineUnderTest.plugToElectricalPlug();
        coffeeMachineUnderTest.addWaterInTank(2);

        Assertions.assertDoesNotThrow(() -> {
            CoffeeContainer result = coffeeMachineUnderTest.makeACoffee(mockContainer, mockCoffeeType);

            Assertions.assertFalse(result.isEmpty());
            Assertions.assertEquals(mockContainer.getCapacity(), result.getCapacity());
        });
    }

    /*Test que le contenant a son coffeeType égal au type de café passé en paramètre*/
    @Test
    public void testContainerCoffeeTypeAfterMakingCoffee(){
        Container mockContainer = Mockito.mock(Container.class);
        Mockito.when(mockContainer.getCapacity()).thenReturn(1.0);
        Mockito.when(mockContainer.isEmpty()).thenReturn(true);

        CoffeeType mockCoffeeType = Mockito.mock(CoffeeType.class);

        Assertions.assertDoesNotThrow(() -> {
            CoffeeContainer result = coffeeMachineUnderTest.makeACoffee(mockContainer, mockCoffeeType);

            Assertions.assertEquals(result.getCoffeeType(), mockCoffeeType);
        });
    }

    /* Test que le nombre de café s'incrémente de 1 lorsqu'on fait un café*/
    @Test
    public void testCoffeeCountIncrementedAfterMakingCoffee(){
        Container mockContainer = Mockito.mock(Container.class);
        Mockito.when(mockContainer.getCapacity()).thenReturn(1.0);
        Mockito.when(mockContainer.isEmpty()).thenReturn(true);

        CoffeeType mockCoffeeType = Mockito.mock(CoffeeType.class);

        int formerNbOfCoffeeMade = coffeeMachineUnderTest.getNbCoffeeMade();

        Assertions.assertDoesNotThrow(() -> {
            CoffeeContainer result = coffeeMachineUnderTest.makeACoffee(mockContainer, mockCoffeeType);

            Assertions.assertEquals(formerNbOfCoffeeMade + 1, coffeeMachineUnderTest.getNbCoffeeMade());
        });

    }

    /* Test qu'une exception se lève lorsqu'il n'y a pas assez d'eau*/
    @Test
    public void testLackOfWaterInTank() {
        Container mockContainer = Mockito.mock(Container.class);
        CoffeeType mockCoffeeType = Mockito.mock(CoffeeType.class);

        //On vide le réservoir d'eau
        coffeeMachineUnderTest.getWaterTank().decreaseVolumeInTank(coffeeMachineUnderTest.getWaterTank().getActualVolume());

        LackOfWaterInTankException exception = Assertions.assertThrows(LackOfWaterInTankException.class, () -> {
            coffeeMachineUnderTest.makeACoffee(mockContainer, mockCoffeeType);
        });

        String expectedMessage = "You must add more water in the water tank.";
        String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));

    }

    /*Test qu'une exception se lève lorsque le café demandé est différent de celui dans le réservoir*/
    @Test
    public void testCoffeeTypeException(){
        Container mockContainer = Mockito.mock(Container.class);
        CoffeeType mockCoffeeType = Mockito.mock(CoffeeType.class);

        coffeeMachineUnderTest.reset();
        coffeeMachineUnderTest.plugToElectricalPlug();
        coffeeMachineUnderTest.addWaterInTank(2);

        coffeeMachineUnderTest.addCoffeeInBeanTank(1.5, CoffeeType.ARABICA);

        // Configurez le comportement du mockCoffeeType
        Mockito.when(mockCoffeeType.toString()).thenReturn("BAHIA");

        // Utilisez assertThrows pour vérifier que l'exception est levée
        CoffeeTypeCupDifferentOfCoffeeTypeTankException exception = Assertions.assertThrows(CoffeeTypeCupDifferentOfCoffeeTypeTankException.class, () -> {
            coffeeMachineUnderTest.makeACoffee(mockContainer, mockCoffeeType);
        });

        String expectedMessage = "The type of coffee to be made in the cup is different from that in the tank.";
        String actualMessage = exception.getMessage();
        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }


    @AfterEach
    public void afterTest(){

    }
}
