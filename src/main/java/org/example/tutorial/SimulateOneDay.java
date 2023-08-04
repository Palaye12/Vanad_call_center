package org.example.tutorial;

import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SimulateOneDay  {

    private List<Double> arrivalTimes = new ArrayList<>();


    HashMap<Integer, Double> LES = new HashMap<Integer, Double>();
    LinkedList<Customer> waitList = new LinkedList<>();


    HashMap<Integer,Integer> qLengthRealTime = new HashMap<Integer,Integer>();
    HashMap<Integer,HashSet<Double> > nbagentsList = new HashMap<>();

    ArrayList<Customer> servedCustomers = new ArrayList<>();
    Set<String> dayList = new HashSet<String>();
    Set<Integer> typesService = new HashSet<Integer>();
    int nbClientsTraites;
    int i=0;



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
    //la fonction récupère les types de services et les jours distincts du fichier du mois
    public void getTypesAndDays(String monthFile) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(monthFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;
        Integer type;
        String jour;
        while((readedLine= br.readLine()) != null){
           jour =readedLine.split(",")[0].split(" ")[0];
           if(dayList.contains(jour)== false)
               dayList.add(jour);
            type = Integer.parseInt(readedLine.split(",")[1]);
            if(typesService.contains(type)==false ){
                typesService.add(type);

            }
        }

    }
    public void getAgentsByDays(String jour,String monthFile) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(monthFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;
        Integer type;
        Double agent_number;
        nbagentsList.clear();

        while((readedLine= br.readLine()) != null){
            String dayOfMonth =readedLine.split(",")[0].split(" ")[0];

            if(jour.equals(dayOfMonth)) {

                Customer customer = new Customer();
                type = Integer.parseInt(readedLine.split(",")[1]);

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
            }

        }

    }
    public void getMonthDataset(String monthfile) throws IOException{
        this.getTypesAndDays(monthfile);


        for(String jour:dayList) {
            Sim.init();
            new EndOfSim().schedule(43200);
            this.getAgentsByDays(jour, monthfile);
            this.createDayCustomers(jour, monthfile);

        }


    }

    public void createDayCustomers(String dayOfMonth,String dayFile) throws IOException {


        BufferedReader br = new BufferedReader(new FileReader(dayFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;
        Integer type;

        qLengthRealTime.clear();

        for (Integer typeService:typesService){
            qLengthRealTime.put(typeService,0);
        }

        this.nbClientsTraites=0;


        while((readedLine= br.readLine()) != null){
            String jour =readedLine.split(",")[0].split(" ")[0];

            if(jour.equals(dayOfMonth)){

                Customer customer = new Customer();
                type = Integer.parseInt(readedLine.split(",")[1]);

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


                    if(customer.waitingTime>=0) {
                        new ArrivalInQueue(customer).schedule(getTime(readedLine.split(",")[0]));
                        Sim.start();
                    }





                this.nbClientsTraites++;





            }


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


    public void displayCustomers() {
        //System.out.println(this.nbClientsTraites);
        //System.out.println("Nombre de jours oeuvrés "+dayList.size());
        //System.out.println("Nombre de type de service "+typesService.size());
        System.out.println("Nombre de Client servi "+servedCustomers.size());



    }
    public void displayNbAgentsList() {
        System.out.println("Nombre de serveurs pour chaque type :");
        for (Integer type : nbagentsList.keySet()) {
            HashSet<Double> nbAgents = nbagentsList.get(type);
            System.out.println("Type " + type + " : " + nbAgents.size() + " serveur(s)");
        }
    }




    class ArrivalInQueue extends Event {

        Customer customer ;
        public ArrivalInQueue(Customer customer){
            this.customer = customer;

        }


        @Override
        public void actions() {



            Integer length;
            if( qLengthRealTime.get(customer.type)==null){
                 length = 0;
            }
            else {
                length = qLengthRealTime.get(customer.type);
            }

            if(LES.get(customer.type)==null)
            customer.les = 0.0;
            else
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

            System.out.println(customer.nbServeurs);
            System.out.println(customer.waitingTime);

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


    class EndOfSim extends Event {
        public void actions() {

            Sim.stop();
        }
    }

}
