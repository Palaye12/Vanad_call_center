package org.example;

import org.example.tutorial.SimulateOneDay;
import org.example.tutorial.CsvUtils;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        String mois;
        for(int i=1;i<=12;i++){
            if(i<10)
                mois="calls-2014-0"+i+".csv";
            else
                mois ="calls-2014-"+i+".csv";
            try {

                // Appel de la fonction createDayCustomers depuis SimulateOneday
                SimulateOneDay simulate = new SimulateOneDay();
                simulate.getMonthDataset("data_with_VANAD/"+mois);
                String output = "C:\\Users\\PC\\Dataset\\extracted"+i+".csv";
                simulate.exportToCSV(output);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }






    }
}