package edu.ufp.inf.sd.rmi.hashmatching.client;

import edu.ufp.inf.sd.rmi.hashmatching.server.FactoryRI;
import edu.ufp.inf.sd.rmi.hashmatching.server.SessionRI;
import edu.ufp.inf.sd.rmi.hashmatching.server.TaskGroupImpl;
import edu.ufp.inf.sd.rmi.hashmatching.server.TaskGroupRI;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Title: Projecto SD</p>
 * <p>
 * Description: Projecto apoio aulas SD</p>
 * <p>
 * Copyright: Copyright (c) 2017</p>
 * <p>
 * Company: UFP </p>
 *
 * @author Rui S. Moreira
 * @version 3.0
 */
public class HashMatchingClient {

    /**
     * Context for connecting a RMI client to a RMI Servant
     */
    private SetupContextRMI contextRMI;
    /**
     * Remote interface that will hold the Servant proxy
     */
    private FactoryRI factoryRI;
    private SessionRI sessionRI;

    public HashMatchingClient(String[] args) {
        try {
            //List ans set args
            SetupContextRMI.printArgs(this.getClass().getName(), args);
            String registryIP = args[0];
            String registryPort = args[1];
            String serviceName = args[2];
            //Create a context for RMI setup
            contextRMI = new SetupContextRMI(this.getClass(), registryIP, registryPort, new String[]{serviceName});
        } catch (RemoteException e) {
            Logger.getLogger(HashMatchingClient.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void main(String[] args) {
        if (args != null && args.length < 2) {
            System.err.println("usage: java [options] edu.ufp.sd.inf.rmi.hashmatching.server.HaschMatchngClient <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        } else {
            //1. ============ Setup client RMI context ============
            HashMatchingClient hwc = new HashMatchingClient(args);
            //2. ============ Lookup service ============
            hwc.lookupService();
            //3. ============ Play with service ============
            hwc.playService();
        }
    }

    private Remote lookupService() {
        try {
            //Get proxy to rmiregistry
            Registry registry = contextRMI.getRegistry();
            //Lookup service on rmiregistry and wait for calls
            if (registry != null) {
                //Get service url (including servicename)
                String serviceUrl = contextRMI.getServicesUrl(0);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going to lookup service @ {0}", serviceUrl);

                //============ Get proxy to HashingMatching service ============
                factoryRI = (FactoryRI) registry.lookup(serviceUrl);
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "registry not bound (check IPs). :(");
                //registry = LocateRegistry.createRegistry(1099);
            }
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return factoryRI;
    }

    private void playService() {
        try {
            //============ Call HaschMatching remote service ============

            System.out.println("\n" + "1: REGISTER NEW USER \n" + "2: LOGIN \n\n");
            String option = null;
            while (option == null || (!option.equals("1") && !option.equals("2"))) {
                option = this.readInput("option");
            }

            String name = this.readInput("name");
            String pass = this.readInput("pass");

            if (option.equals("1")) {
                TaskGroupRI subject = null;
                boolean check = this.factoryRI.register(name, pass);
                if (check) {
                    sessionRI = this.factoryRI.login(name, pass);
                }

            } else if (option.equals("2")) {
                this.sessionRI = null;
                while (this.sessionRI == null) {
                    sessionRI = this.factoryRI.login(name, pass);
                }
            }

            String taskNameOfWorker;
            System.out.println("Choose:\n" + "1: CREATE TASK GROUPS\n" + "2: DELETE TASK GROUP \n" +
                    "3: LIST TASKS GROUP\n" + "4: ASSOCIATE WORKER AT TASK GROUP\n" + "5: LIST WORKERS OF TASK GROUP\n" +
                    "6: START WORK\n" +  "7: LIST ACTIVE TASK GROUPS \n" + "8: JOIN TASK GROUP \n" + "9: LOGOUT\n\n");
            Integer menuOption = Integer.valueOf(this.readInput("Menu Option"));
            System.out.println(menuOption);
            while (!menuOption.equals(-1)) {
                switch (menuOption) {
                    case 1:
                        String taskNameCreate = this.readInput("Task Name To Create");
                        String numberOfParts = this.readInput("the number of parts that you want");
                        boolean checkCreate = this.sessionRI.createTaskGroup(taskNameCreate,numberOfParts);
                        if (checkCreate == false)
                            System.out.println("Taskname '" + taskNameCreate + "' already been used, Choose wisely!\n");
                        menuOption = 0;
                        break;

                    case 2:
                        taskNameOfWorker = this.readInput("Task Name To Delete");
                        boolean checkDelete = this.sessionRI.deleteTaskGroup(taskNameOfWorker);
                        if (checkDelete == false)
                            System.out.println("\nYou can't delete Task group '" + taskNameOfWorker );
                        else System.out.println("\nTask Group'" + taskNameOfWorker + "' has been deleted!!");
                        menuOption = 0;
                        break;

                    case 3:
                        for (String task : this.sessionRI.listTaskGroups()) {
                            System.out.println(task);
                        }
                        menuOption = 0;
                        break;

                    case 4:
                        //Associar n worker a x task
                        String taskNameAttachWorker = this.readInput("Task Name To Attach Workers");
                        TaskGroupRI tgRI = this.sessionRI.getTaskGroup(taskNameAttachWorker);
                        if(tgRI==null){
                            System.out.println("You can't add workers to "+ taskNameAttachWorker +" because you don't have access");
                            menuOption = 0;
                            break;
                        }
                        String workerName = this.readInput("Worker");

                      //  boolean checkAttach = this.sessionRI.attachWorker(workerName,taskNameAttachWorker);
                        WorkerRI worker = new WorkerImpl(workerName, tgRI,this.sessionRI.getUsername());
                        boolean checkAttach = tgRI.attach(worker);
                        if (checkAttach == false)
                            System.out.println("\nWorker '" + workerName + "' couldn't get attached!!");
                        else System.out.println("\nWorker'" + workerName + "' has been attached!!");
                        menuOption = 0;
                        break;
                    case 5:
                        taskNameOfWorker = this.readInput("Task Name To See Workers");
                        System.out.println("\n Workers from " + taskNameOfWorker + ":\n");

                        //verify access of task group
                        TaskGroupRI taskGroup = this.sessionRI.getTaskGroup(taskNameOfWorker);
                        if(taskGroup==null){
                            System.out.println("You can't add workers to "+ taskGroup + ", you don't have access");
                            menuOption = 0;
                            break;
                        }
                        List<WorkerRI> out = this.sessionRI.listWorkers(taskNameOfWorker);
                        if(out.isEmpty()){
                            System.out.println("You don't have workers on this task group");
                        }
                        for(int i=0; i<out.size(); i++){
                            System.out.println("\n"+out.get(i).getName()+"\n");
                        }
                        menuOption = 0;
                        break;

                    case 6:
                        taskNameOfWorker = this.readInput("Task Name To Start Work");
                        boolean checkWorker = this.sessionRI.startWork(taskNameOfWorker);
                        if (checkWorker == false)
                            System.out.println("\nYou don't have access to: " + taskNameOfWorker);
                        else System.out.println("\nTask '" + taskNameOfWorker + "' has been start work!!");
                        menuOption = 0;
                        break;
                    case 7:
                        List<String> strings = this.sessionRI.getActiveTaskGroups();
                        for (String s: strings) {
                            System.out.println(s);
                        }
                        menuOption = -1;
                        break;
                    case 8:
                        String taskName = this.readInput("Task Name To Join");
                        boolean check = this.sessionRI.joinTaskGroup(taskName);
                        if(check){
                            System.out.println("Joined in "+taskName);
                        }else{
                            System.out.println("limete maximo atingido de users");
                        }
                        menuOption =0;
                        break;
                    case 9:
                        this.sessionRI.logOut();
                        menuOption = -1;
                        break;
                    default:
                        menuOption = Integer.valueOf(readInput("Menu Option"));
                        break;
                }
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "");
        } catch (RemoteException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean register(String name, String pass) {
        boolean regist = false;
        try {
            regist = factoryRI.register(name, pass);
        } catch (RemoteException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Exception in register");
        }
        return regist;
    }

    public String readInput(String dados) {
        System.out.println("\n Insert " + dados);
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        String name = null;
        try {
            name = bufferRead.readLine();
            if (name == null || name.isEmpty()) {
                readInput("option");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return name;
    }
}
