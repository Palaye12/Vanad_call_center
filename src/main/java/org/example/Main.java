package org.example;

import org.example.tutorial.SimulateOneDay;
import org.example.tutorial.CsvUtils;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {



        try {

            // Appel de la fonction createDayCustomers depuis SimulateOneday
            SimulateOneDay simulate = new SimulateOneDay();
            simulate.getMonthDataset("data_with_VANAD/calls-2014-12.csv");
            simulate.displayCustomers();

            //simulate.displayFileContent();

            //simulate.displayArrivalTimes(); // Afficher les temps d'arrivée des clients
            //String input = "Dataset/janvier/2014-01-02.csv";
            //String output = "C:\\Users\\LENOVO\\Desktop\\Master1_UCAD_FST\\B.I\\extracted1.csv";

            //CsvUtils.extractCSV(input, output);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}