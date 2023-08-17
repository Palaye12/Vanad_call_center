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

    double[] LES ;
    LinkedList<Customer> waitList = new LinkedList<>();

    int[] qLengthRealTime ;
    HashMap<Integer,HashSet<Integer> > nbAgentsList = new HashMap<>();
    ArrayList<Customer> servedCustomers = new ArrayList<>();
    Set<String> dayList = new HashSet<>();

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

    public int getCase(int type){
        switch (type) {
            case 30172:
                return 0;
            case 30175:
                return 1;
            case 30179:
                return 2 ;

            case 30066:
                return 3;

            case 30511:
                return 4;

            case 30241:
                return 5;
            case 30181:
                return 6;

            case 30519:
                return 7;

            case 30174:
                return 8;

            case 30176:
                return 9;

            case 30180:
                return 10;

            case 30325:
                return 11;

            case 30236:
                return 12;

            case 30173:
                return 13;

            case 30177:
                return 14;

            case 30584:
                return 15;

            case 30598:
                return 16;

            case 30518:
                return 17;
            case 30363:
                return 18;

            case 30170:
                return 19;

            case 30694:
                return 20;

            case 30334:
                return 21;

            case 30729:
                return 22;

            case 30178:
                return 23;

            case 30747:
                return 24;
            case 30764:
                return 25;
            case 30560:
                return 26;
            default:
                return 27;

        }

    }
    //la fonction récupère les jours distincts du fichier du mois
    public void getDays(String monthFile) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(monthFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;

        while((readedLine= br.readLine()) != null){

            dayList.add(readedLine.split(",")[0].split(" ")[0]);

        }

    }
    public void getAgentsByDays(String jour,String monthFile) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(monthFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;
        Integer type;
        Integer agent_number;
        nbAgentsList.clear();
        while((readedLine= br.readLine()) != null){
            String dayOfMonth =readedLine.split(",")[0].split(" ")[0];

            if(jour.equals(dayOfMonth)) {
                type = Integer.parseInt(readedLine.split(",")[1]);

                String agentNumberString = readedLine.split(",")[2];
                if (!agentNumberString.isEmpty() && !agentNumberString.equals("NULL")) {
                    agent_number = Integer.parseInt(agentNumberString);
                    if (nbAgentsList.containsKey(type)) {
                        nbAgentsList.get(type).add(agent_number);
                    } else {
                        HashSet<Integer> nbAgents = new HashSet<>();
                        nbAgents.add(agent_number);
                        nbAgentsList.put(type, nbAgents);
                    }
                }
            }

        }

    }
    public void getMonthDataset(String monthfile) throws IOException{
        this.getDays(monthfile);

        for(String jour:dayList) {
            this.getAgentsByDays(jour, monthfile);
            Sim.init();
            new EndOfSim().schedule(46800);
            this.createDayCustomers(jour, monthfile);
            Sim.start();
        }


    }

    public void createDayCustomers(String dayOfMonth,String dayFile) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(dayFile));
        //lis l'entête du fichier
        String headerLine = br.readLine();
        String readedLine ;

        qLengthRealTime= new int[27];
        LES= new double[27];

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
            customer.qLength = qLengthRealTime[getCase(customer.type)];
            customer.les = LES[getCase(customer.type)];
            for (int i = 0 ; i < 27; i++){
                customer.qLengthAtArrival[i] = qLengthRealTime[i];
            }
            // Initialiser le nombre de serveurs disponibles (A faire)
            if(nbAgentsList.get(customer.type)!=null)
                customer.nbServeurs= nbAgentsList.get(customer.type).size();
            else
                customer.nbServeurs=0;
            waitList.add(customer);
            // mise à jour de la longueur de la file d'attente
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
            csvPrinter.printRecord("T","qt","r","t","s","Les","w");


            // Écriture des données pour chaque objet dans l'ArrayList
            for (Customer c : customers) {
                ArrayList<Object> entrees = new ArrayList<>();
                entrees.add(c.type);
                entrees.add(c.qLength);
                String arrivalTab="";
                for(int i=0;i<c.qLengthAtArrival.length;i++){
                    arrivalTab= arrivalTab+c.qLengthAtArrival[i];
                    if(i<26)
                        arrivalTab= arrivalTab+",";
                }
                entrees.add(arrivalTab);
                entrees.add(c.arrivalTime);
                entrees.add(c.nbServeurs);
                entrees.add(c.les);
                entrees.add(c.waitingTime);
                csvPrinter.printRecord(entrees);
            }

            csvPrinter.flush();
            System.out.println("Fichier exporté avec succès");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}