package org.example.tutorial;

import umontreal.ssj.simevents.Event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class SimulateOneDay {

    double[] LES = new double[27];
    LinkedList<Customer> waitList = new LinkedList<>();
    int nbServeurs = 0 ;
    int[] qLengthRealTime = new int[27];

    ArrayList<Customer> servedCustomers = new ArrayList<>();
    public int getCase(int type){
        if (type == 30175){
            return 0;
        }
        // A compléter pour les 27 types de services
        return 1;
    }

    class Customer {
        int type;
        double arrivalTime;
        double les;
        int nbServeurs;
        double waitingTime;
        int qLength;
        // Savoir la case qui est réservé à chaque type
        int[] qLengthAtArrival = new int[27];
        double serviceTime ;
        boolean abandon = false;
    }

    public void createDayCustomers(String dayFile) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(dayFile));
        String readedLine = br.readLine();

        while(readedLine != null){
            Customer customer = new Customer();
            customer.type = Integer.parseInt(readedLine.split(",")[1]);
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
            customer.les = LES[getCase(customer.type)];
            customer.qLength = qLengthRealTime[getCase(customer.type)];

            for (int i = 0 ; i < 27; i++){
                customer.qLengthAtArrival[i] = qLengthRealTime[i];
            }
            // Initialiser le nombre de serveurs disponibles (A faire)

            waitList.add(customer);
            qLengthRealTime[getCase(customer.type)] += 1;
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
            waitList.remove(customer);
            qLengthRealTime[getCase(customer.type)] -= 1;

            if (!customer.abandon){
                LES[getCase(customer.type)] = customer.waitingTime;
                servedCustomers.add(customer);
            }
        }
    }
}
