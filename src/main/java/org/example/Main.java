package org.example;

import org.example.tutorial.SimulateOneDay;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        //System.out.println("Hello world!");

        try {
            // Appel de la fonction createDayCustomers depuis SimulateOneday
            SimulateOneDay simulate = new SimulateOneDay();
            simulate.createDayCustomers("Dataset/janvier/2014-01-02.csv");
            simulate.displayFileContent();
            simulate.displayNbAgentsList();
            simulate.displayArrivalTimes(); // Afficher les temps d'arrivée des clients

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}