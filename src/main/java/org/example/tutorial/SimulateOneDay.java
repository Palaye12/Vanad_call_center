package org.example.tutorial;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
public class SimulateOneDay  {


    HashMap<Integer, Double> LES = new HashMap<>();
    LinkedList<Customer> waitList = new LinkedList<>();


    HashMap<Integer,Integer> qLengthRealTime = new HashMap<>();
    HashMap<Integer,HashSet<Double> > nbAgentsList = new HashMap<>();

    ArrayList<Customer> servedCustomers = new ArrayList<>();
    Set<String> dayList = new HashSet<>();
    Set<Integer> typesService = new HashSet<>();




    class Customer {

        int type;
        double arrivalTime;
        double les;
        int nbServeurs;
        double waitingTime;
        int qLength;
        // Savoir la case qui est réservé à chaque type
        HashMap<Integer,Integer> qLengthAtArrival = new HashMap<>();
        double serviceTime ;
        boolean abandon = false;

    }
    //la fonction récupère les types de services et les jours distincts du fichier du mois
    public void getTypesAndDays(String monthFile) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(monthFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;

        while((readedLine= br.readLine()) != null){

           dayList.add(readedLine.split(",")[0].split(" ")[0]);
           typesService.add(Integer.parseInt(readedLine.split(",")[1]));


        }

    }
    public void getAgentsByDays(String jour,String monthFile) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(monthFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;
        Integer type;
        Double agent_number;
        nbAgentsList.clear();

        while((readedLine= br.readLine()) != null){
            String dayOfMonth =readedLine.split(",")[0].split(" ")[0];

            if(jour.equals(dayOfMonth)) {
                type = Integer.parseInt(readedLine.split(",")[1]);

                String agentNumberString = readedLine.split(",")[2];
                if (!agentNumberString.isEmpty() && !agentNumberString.equals("NULL")) {
                    agent_number = Double.parseDouble(agentNumberString);
                    if (nbAgentsList.containsKey(type)) {
                        nbAgentsList.get(type).add(agent_number);
                    } else {
                        HashSet<Double> nbAgents = new HashSet<>();
                        nbAgents.add(agent_number);
                        nbAgentsList.put(type, nbAgents);
                    }
                }
            }

        }


    }
    public void getMonthDataset(String monthfile) throws IOException{
        this.getTypesAndDays(monthfile);


        for(String jour:dayList) {
            this.getAgentsByDays(jour, monthfile);
            Sim.init();
            new EndOfSim().schedule(46800);
            this.createDayCustomers(jour, monthfile);

        }


    }

    public void createDayCustomers(String dayOfMonth,String dayFile) throws IOException {


        BufferedReader br = new BufferedReader(new FileReader(dayFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;


        qLengthRealTime.clear();
        LES.clear();

        for (Integer typeService:typesService){
            qLengthRealTime.put(typeService,0);
        }





        while((readedLine= br.readLine()) != null){
            String jour =readedLine.split(",")[0].split(" ")[0];
            if(jour.equals(dayOfMonth)){

                Customer customer = new Customer();
                customer.type = Integer.parseInt(readedLine.split(",")[1]);
                customer.arrivalTime = getTime(readedLine.split(",")[0]);
                if (!readedLine.split(",")[3].equals("NULL") ){
                    customer.serviceTime = getTime(readedLine.split(",")[6]) - getTime(readedLine.split(",")[3]);
                    customer.waitingTime = getTime(readedLine.split(",")[3]) - getTime(readedLine.split(",")[0]);
                }
                else{
                    customer.abandon = true;
                    customer.waitingTime = getTime(readedLine.split(",")[6]) - getTime(readedLine.split(",")[0]);
                }


                        new ArrivalInQueue(customer).schedule(getTime(readedLine.split(",")[0]));

                        Sim.start();

            }


        }






    }




    public double getTime(String s) {
        String[] sParts = s.split(" ");
        if (sParts.length >= 2) {
            String s1 = sParts[1];
            return Double.parseDouble(s1.split(":")[0]) * 3600
                    + Double.parseDouble(s1.split(":")[1]) * 60
                    + Double.parseDouble(s1.split(":")[2]) - (7.0 * 3600.0);
        } else {
            // Gérer l'erreur ou retourner une valeur par défaut si nécessaire
            return 0.0;
        }
    }








    class ArrivalInQueue extends Event {

        Customer customer ;
        public ArrivalInQueue(Customer customer){
            this.customer = customer;

        }


        @Override
        public void actions() {

            Integer length = qLengthRealTime.get(customer.type);
            if(LES.get(customer.type)==null)
            customer.les = 0.0;
            else
                customer.les = LES.get(customer.type);
            customer.qLength = length ;

            for (Integer i :typesService){
                customer.qLengthAtArrival.put(i,qLengthRealTime.get(i)) ;
            }
            // Initialiser le nombre de serveurs disponibles (A faire)
            if(nbAgentsList.get(customer.type)!=null)
             customer.nbServeurs= nbAgentsList.get(customer.type).size();
            else
                customer.nbServeurs=0;
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


    class EndOfSim extends Event {
        public void actions() {

            Sim.stop();
        }
    }

    public  void exportToCSV( String filePath) {
        ArrayList<Customer> customers =this.servedCustomers;
        try (FileWriter writer = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            // Écriture de l'en-tête du fichier CSV (si nécessaire)
             csvPrinter.printRecord("Type","qT","l", "t" ,"s","LES","w");

            // Écriture des données pour chaque objet dans l'ArrayList
            for (Customer c : customers) {

                csvPrinter.printRecord(c.type, c.qLength,c.qLengthAtArrival,c.arrivalTime,c.nbServeurs,c.les,c.waitingTime);
            }

            csvPrinter.flush();
            System.out.println("Fichier exporté avec succès");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
