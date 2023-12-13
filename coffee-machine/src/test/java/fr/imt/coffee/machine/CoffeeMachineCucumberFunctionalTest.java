package fr.imt.coffee.machine;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * Classe qui permet de faire le lien entre le fichier "make_a_coffee.feature" et la classe implémentant les étapes
 * de test CoffeeMachineTestFunctionalTest.java
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:functional/features/"},
        glue = "fr.imt.coffee.machine.cucumber.steps"
)
//Permet d'ignorer les tests fonctionnels de Cucumber
//Ne lance pas la class CoffeeMachineFunctionalTest

public class CoffeeMachineCucumberFunctionalTest {

}
