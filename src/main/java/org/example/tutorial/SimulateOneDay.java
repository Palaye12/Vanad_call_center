package org.example.tutorial;

import umontreal.ssj.simevents.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SimulateOneDay {

    private List<Double> arrivalTimes = new ArrayList<>();

    HashMap<Integer, Double> LES = new HashMap<Integer, Double>();
    LinkedList<Customer> waitList = new LinkedList<>();
    int nbServeurs = 0 ;
    HashMap<Integer,Integer> qLengthRealTime = new HashMap<Integer,Integer>();
    HashMap<Integer,HashSet<Double> > nbagentsList = new HashMap<>();

    ArrayList<Customer> servedCustomers = new ArrayList<>();


    class Customer {
        public Double agentNumber;
        int type;
        double arrivalTime;
        double les;
        int nbServeurs;
        double waitingTime;
        int qLength;
        // Savoir la case qui est réservé à chaque type
        HashMap<Integer,Integer> qLengthAtArrival = new HashMap<Integer,Integer>();
        double serviceTime ;
        boolean abandon = false;

    }

    public void createDayCustomers(String dayFile) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(dayFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;
        Integer type;
        Double agent_number;


        while((readedLine= br.readLine()) != null){
            Customer customer = new Customer();
            type = Integer.parseInt(readedLine.split(",")[1]);
            if(qLengthRealTime.isEmpty() || qLengthRealTime.containsKey(type)==false ){
                qLengthRealTime.put(type,0);
            }
            String agentNumberString = readedLine.split(",")[2];
            if (!agentNumberString.isEmpty() && !agentNumberString.equals("NULL")) {
                agent_number = Double.parseDouble(agentNumberString);
                if (nbagentsList.containsKey(type)) {
                    nbagentsList.get(type).add(agent_number);
                } else {
                    HashSet<Double> nbAgents = new HashSet<>();
                    nbAgents.add(agent_number);
                    nbagentsList.put(type, nbAgents);
                }
            }
            customer.type = type;
            customer.arrivalTime = getTime(readedLine.split(",")[0]);

            arrivalTimes.add(customer.arrivalTime); // Ajouter le temps d'arrivée du client à la liste

            if (readedLine.split(",")[3] != "NULL"){
                customer.serviceTime = getTime(readedLine.split(",")[6]) - getTime(readedLine.split(",")[3]);
                customer.waitingTime = getTime(readedLine.split(",")[3]) - getTime(readedLine.split(",")[0]);
            }
            else{
                customer.abandon = true;
                customer.waitingTime = getTime(readedLine.split(",")[6]) - getTime(readedLine.split(",")[0]);
            }
            new ArrivalInQueue(customer).schedule(getTime(readedLine.split(",")[0]));
        }
    }
    public void displayArrivalTimes() {
        System.out.println("Temps d'arrivée des clients :");
        for (int i = 0; i < arrivalTimes.size(); i++) {
            double arrivalTime = arrivalTimes.get(i);
            System.out.println("Client " + (i + 1) + " : " + arrivalTime + " secondes");
        }
    }

    public double getTime(String s) {
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

    public void displayNbAgentsList() {
        System.out.println("Nombre de serveurs pour chaque type :");
        for (Integer type : nbagentsList.keySet()) {
            HashSet<Double> nbAgents = nbagentsList.get(type);
            System.out.println("Type " + type + " : " + nbAgents.size() + " serveur(s)");
        }
    }


    public void displayFileContent() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("Dataset/janvier/2014-01-02.csv"));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }

    class ArrivalInQueue extends Event {

        Customer customer ;
        public ArrivalInQueue(Customer customer){
            this.customer = customer;
        }

        @Override
        public void actions() {
            Integer length = qLengthRealTime.get(customer.type);
            customer.les = LES.get(customer.type);
            customer.qLength = length ;

            for (Integer i :qLengthRealTime.keySet()){
                customer.qLengthAtArrival.put(i,qLengthRealTime.get(i)) ;
            }
            // Initialiser le nombre de serveurs disponibles (A faire)
            customer.nbServeurs= nbagentsList.get(customer.type).size();
            waitList.add(customer);
            // mise à jour de la longueur de la file d'attente
            qLengthRealTime.put(customer.type,length+1);


            new Departure(customer).schedule(customer.waitingTime);
        }
    }
    class Departure extends Event{
        Customer customer;

        public Departure(Customer customer){
            this.customer = customer;
        }

        @Override
        public void actions() {
            Integer length = qLengthRealTime.get(customer.type);
            waitList.remove(customer);
            qLengthRealTime.put(customer.type,length-1);

            if (!customer.abandon){
                LES.put(customer.type, customer.waitingTime);
                servedCustomers.add(customer);


            }

        }
    }
}
