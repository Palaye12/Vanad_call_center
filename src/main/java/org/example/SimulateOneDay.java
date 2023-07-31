package org.example.tutorial;

import umontreal.ssj.simevents.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class SimulateOneDay {


    HashMap<Integer, Double> LES = new HashMap<Integer, Double>();
    LinkedList<Customer> waitList = new LinkedList<>();
    int nbServeurs = 0 ;
   HashMap<Integer,Integer> qLengthRealTime = new HashMap<Integer,Integer>();
    HashMap<Integer,HashSet<Integer> > nbagentsList = new HashMap<>();

    ArrayList<Customer> servedCustomers = new ArrayList<>();


    class Customer {
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
        Integer agent_number;


        while((readedLine= br.readLine()) != null){
            Customer customer = new Customer();
            type = Integer.parseInt(readedLine.split(",")[1]);
            if(qLengthRealTime.isEmpty() || qLengthRealTime.containsKey(type)==false ){
                qLengthRealTime.put(type,0);
            }
            agent_number = Integer.parseInt(readedLine.split(",")[2]);
            if(nbagentsList.containsKey(type)==false  ){
                if(agent_number !=null ){
                    HashSet<Integer> nbAgents = new HashSet<>();
                    nbAgents.add(agent_number);
                    nbagentsList.put(type,nbAgents);
                }

            }
            else {
                if(agent_number !=null && nbagentsList.get(type).contains(agent_number)==false){
                    nbagentsList.get(type).add(agent_number);
                }
            }
            customer.type = type;
            customer.arrivalTime = getTime(readedLine.split(",")[0]);

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





    public double getTime(String s){
        String s1 = s.split(" ")[1];
        return Integer.parseInt(s1.split(":")[0])*3600
                +Integer.parseInt(s1.split(":")[1])*60
                +Integer.parseInt(s1.split(":")[2])-(8*3600);
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
