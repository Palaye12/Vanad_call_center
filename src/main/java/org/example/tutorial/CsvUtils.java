package org.example.tutorial;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CsvUtils {

    public static void extractCSV(String input, String output) throws IOException {
        // code extraction

        HashMap<Integer, List<Double>> nbagentsList = new HashMap<>();
        List<Double> arrivalTimes = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(input));
        FileWriter writer = new FileWriter(output);

        String line = reader.readLine();

        while ((line = reader.readLine()) != null) {

            String[] data = line.split(",");


            // Extraire les données souhaitées
            int type = (int) Math.round(Double.parseDouble(data[1]));
            Double agentNum = data[2].isEmpty() || data[2].equals("NULL") ? null : Double.parseDouble(data[2]);

            double arrivalTime = getTime(data[0]);
            double startTime =  getTime(data[3]);
            double endTime = getTime(data[6]);
            double waitTime = endTime - startTime;

            // Mettre à jour nbagentsList
            if (agentNum != null) {
                nbagentsList.computeIfAbsent(type, k -> new ArrayList<>()).add(agentNum);
            }

            // Ajouter heure d'arrivée
            arrivalTimes.add(arrivalTime);

            // Ecrire dans le nouveau fichier
            writer.write(type + "," + agentNum + "," + arrivalTime + "," + waitTime + "\n");

        }

        reader.close();
        writer.close();

    }
    public static double getTime(String s) {
        String[] sParts = s.split(" ");
        if (sParts.length >= 2) {
            String s1 = sParts[1];
            return Double.parseDouble(s1.split(":")[0]) * 3600
                    + Double.parseDouble(s1.split(":")[1]) * 60
                    + Double.parseDouble(s1.split(":")[2]) - (8 * 3600);
        } else {
            // Gérer l'erreur ou retourner une valeur par défaut si nécessaire
            return 0.0;
        }
    }
}
