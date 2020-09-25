package edu.ufp.inf.sd.rmi.hashmatching.server;

import edu.ufp.inf.sd.rmi.hashmatching.client.State;
import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerImpl;
import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerRI;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class TaskGroupImpl implements TaskGroupRI{

    private String name;
    /**
     * hash a encontrar = hashCode
     */
    private String hashCode = "";

    private int delta;

    private Integer credits = 0;

    private int numberOfParts = 0;

    private int assignedWork = 0;

    private List<String> users = new ArrayList<String>();

    /**
     * ficheiro a procurar = testdarkc0de.txt
     */
    private File file = new File("C:/Users/crist/Documents/IntelliJ/SD/src/edu/ufp/inf/sd/rmi/util/testdarkcode.txt");

    private File fileHashToFind = new File("C:/Users/crist/Documents/IntelliJ/SD/src/edu/ufp/inf/sd/rmi/util/hashToFind.txt");

    private List<String> wordsToHash = new ArrayList<String>();

    private String owner;

    private List<WorkerRI> workers = new ArrayList<WorkerRI>();

    private HashMap<String,List<String>> restLines = new HashMap<>();

    public TaskGroupImpl(String owner, String name, String numberOfParts) throws RemoteException {
        this.owner = owner;
        this.name = name;
        this.wordsToHash = this.saveWords();
        this.hashCode = this.saveHash();
        this.numberOfParts = Integer.parseInt(numberOfParts);
       // this.workers = WorkerRI.getWorkers(owner);
        UnicastRemoteObject.exportObject(this, 0);
    }

    /**
     * adiciona worker passado em parametro ao array list de workers da taskgroup
     *
     * @param w - worker a ser adicionado
     * @throws RemoteException
     * @return
     */
    public boolean attach(WorkerRI w) throws RemoteException {
        if(this.workers.add(w)) return true;
        return false;
    }

    public boolean attachUser(String user) throws RemoteException {
        if(this.users.add(user)) return true;
        return false;
    }

    /**
     * remove o worker passado em parametro do array list de workers da task
     *
     * @param w - worker a ser eliminado
     * @throws RemoteException
     */
    public void dettach(WorkerRI w) throws RemoteException {
        this.workers.remove(w);
    }

    /**
     * a retirar localmente para enviar ao rmi
     *
     * @throws RemoteException
     */
    public void export() throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    /**
     * prepara o trabalho:
     * divide numero de linhas pelo numero de partes = linesOfUser
     * vai a cada user:
     * lista os workers dele
     * linhas do user / n Workers = delta
     * count = n workers, vai de 1 em 1
     * lineAux = linhas do user * trabalho atribuido ate agora
     * rest = resto da divisao das linhas do user e n workers
     * se rest != 0, sobram linhas -> chama saveLine antes de continuar
     * se nao houver rest vai à lista de workers
     * envia de que linha o worker precisa de ler
     * e quantas linhas ele precisa,
     * chama o sendToWorker
     *
     * @throws RemoteException
     */
    @Override
    public void prepareWork(int parts) throws RemoteException {

        System.out.println("\n Users of task: \n");
        System.out.println(this.users.toString());

        System.out.println("\n PrepareWork(): \n");
        Integer linesOfUser = this.wordsToHash.size()/parts;
        int countUsers = 0;

        for (String user:this.users) {
            countUsers++;
            System.out.println("user in prepare Work: "+user);
            List<WorkerRI> listWorkers = this.getWorkersOfUser(user);
            System.out.println("User "+user+" have "+listWorkers.size()+ " workers");
            this.delta = linesOfUser / listWorkers.size(); //calcular delta
            // System.out.println("delta->" + this.delta);
            int count = 1;
            int lineAux = linesOfUser * assignedWork;

            int rest = linesOfUser % listWorkers.size();
            System.out.println("!!!!!!!!!!!!!!! RESTO: "+ rest);
            if(rest!=0 ){
                System.out.println("\n RESTO != 0 -> VOU CHAMAR O SAVELINE\n");
                System.out.println("lines user "+user + "->"+linesOfUser);
                saveLine(rest,linesOfUser*countUsers,user);
            }

            for (WorkerRI w : listWorkers) {//iniciar o trabalho do worker
                if (count != 1) {
                    lineAux += delta;
                }
                System.out.println("lineAux = "+lineAux);
                w.setNumberOfLine(lineAux);//update worker
                w.setDelta(this.delta);

                this.sendToWorker(w);//iniciar trabalho
                count++;
            }
            System.out.println("assignedWork:" + assignedWork);
            assignedWork++;
        }

    }

    /**
     * começa desde as linhas do user - resto = linhas restantes
     * e vai ate ao fim das linahs do user
     * guarda num array list de strings
     * e coloca num hashmap com o username, e o array associado a esse username
     * @param rest resto da divisao entre n linhas do user, e n workers
     * @param linesOfUser linhas do user
     * @param username nome do user
     */
    private void saveLine(int rest, Integer linesOfUser,String username) {
        List<String> otherLines = new ArrayList<>();
        System.out.println("rest->"+rest);
        for (int i=linesOfUser-rest;i<linesOfUser;i++){
            System.out.println("i->"+i);
            otherLines.add(wordsToHash.get(i));
        }
        //System.out.println("\n OTHERlINES -> "+otherLines.toString());
        restLines.put(username,otherLines);
    }

@Override
    public int getDelta() throws RemoteException{
        return delta;
    }

    /**
     * verifica se o hashmap que guarda as linhas restantes está vazio
     * verifica que existe um array associado aquele username no hashmap
     * se as linhas restantes forem menores que delta, ou seja apenas é preciso 1 worker
     * remove essas linhas do hashmpa e envia para o worker iniciar as threads
     * se houver mais linhas restantes, vai ao hash daquele user
     * e para todas as strings verifica se count<delta
     * se for adiciona as linhas a otherLines,
     * se nao inicia as threads com otherLines e faz updateHashMap
     * @param w worker que pede mais trabalho
     * @return false se o hashmap estiver vazio ou nao houver nada para fazer daquele user
     * @throws RemoteException
     */
    @Override
    public boolean moreWork(WorkerRI w) throws RemoteException {
        System.out.println("more Work");
        if(restLines.isEmpty()){
           // System.out.println("Nao ha mais linhas!!!\n");
            return false;
        }
        if(restLines.get(w.getResponsible())==null){
            return false;
        }
        int linesSize = restLines.get(w.getResponsible()).size();
        System.out.println(w.getResponsible()+" ->"+ restLines.get(w.getResponsible()));
        if(linesSize<=this.delta){
            System.out.println("\n LINESSIZE <= DELTA!");
            if(restLines.get(w.getResponsible()) != null) {
                List<String> helpList = new ArrayList<String>(restLines.get(w.getResponsible()));
                restLines.remove(w.getResponsible());
                w.initAllThreads(helpList,true);
            }
            return false;
        }else{
            int count = 0;
            List<String> otherLines = new ArrayList<>();
            for (String s:restLines.get(w.getResponsible())) {
                if(count<this.delta){
                    otherLines.add(s);
                }else{
                    w.initAllThreads(otherLines,false);
                    updateHashMap(otherLines,w.getResponsible());
                }
                count ++;
            }
        }
    return false;
    }

    /**
     * vai buscar o responsavel, remove do hashmap
     * e remove da lista passada as strings
     * atualiza as restantes linhas daquele user
     * @param assignedLines linhas que restam daquele user
     * @param responsible nome do user
     */
    private void updateHashMap(List<String> assignedLines,String responsible) {
        List<String> myLines = restLines.get(responsible);
        restLines.remove(responsible);
        for (String s: assignedLines) {
            myLines.remove(s);
        }
        restLines.put(responsible,myLines);
    }

    /**
     * devolve workers daquele user
     * @param username nome do user
     * @return
     * @throws RemoteException
     */
    public List<WorkerRI> getWorkersOfUser(String username) throws RemoteException {
        System.out.println("getWorkersOfUser");
        List<WorkerRI> workersToReturn = new ArrayList<>();
        for (WorkerRI worker:this.workers) {
            if(username.equalsIgnoreCase(worker.getResponsible())){
                System.out.println("username->"+username+" && worker -> "+worker.getName());
                workersToReturn.add(worker);
            }
        }
        return workersToReturn;

    }

    /**
     * envia para o worker passado:
     * divide o texto de delta em delta
     * e envia para os workers de delta em delta
     *
     * @param worker
     * @throws RemoteException
     */
    public void sendToWorker(WorkerRI worker) throws RemoteException {
    //    System.out.println("wordsToHash ->"+wordsToHash.toString());
        List<String> myLines = new ArrayList<String>();
        int workerStartLine = worker.getNumberOfLine();
        int startLineWithDelta = workerStartLine+this.delta;
        System.out.println("workerStartLine + delta "+startLineWithDelta);
        for (int i = workerStartLine; i < workerStartLine + this.delta ; i++) {
            //System.out.println("i = "+i);
            myLines.add(this.wordsToHash.get(i));
        }
        System.out.println("sendToWorker(): " + worker.getName() + " start in "+ worker.getNumberOfLine()+" line\n");

        worker.initAllThreads(myLines,false);

    }

    /**
     * salva no wordsToHash em TaskGroupImpl o texto lido do ficheiro
     *
     * @return lista de String do texto lido
     */
    private List<String> saveWords() throws RemoteException {
        List<String> words = new ArrayList<String>();

        Scanner scan = null;
        try {
            scan = new Scanner(file);
            while (scan.hasNextLine()) {
                words.add(scan.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return words;
    }

    /**
     * salva o hash que é para procurar, dentro do ficheiro e guarda-o em "hash"
     *
     * @return o hash procurado lido do ficheiro
     * @throws RemoteException
     */
    private String saveHash() throws RemoteException {
        String hash = "";

        Scanner scan = null;
        try {
            scan = new Scanner(fileHashToFind);
            hash = scan.nextLine();
            System.out.println("\n saveHash(): hash a procurar: "+ hash);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return hash;
    }

    /**
     * notifica os workers todos que encontrou o hash,
     * chama update que imprime "sei que o hash foi encontrado" e para threads
     *
     * @param state   -  hashCode, WorkerImpl worker, Thread thread, String pass - pass encontrada
     * @throws RemoteException
     */
    @Override
    public void notifyWorkers(State state) throws RemoteException {
        System.out.println("state "+state.toString());
        List<WorkerRI> workers = this.workers;
        for (WorkerRI w : workers){
         w.update();
            System.out.println("\nPassword has been found: "+state.getPass()+" , you can stop now worker "+w.getName());
        }
       //System.out.println("\n\nnotifyWorkers()!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1:\n");
    }
    @Override
    public String getHashCode() throws RemoteException {
        return hashCode;
    }

    @Override
    public TaskGroupImpl getTaskGroup() throws RemoteException {
        return this;
    }

    @Override
    public String toString() {
        return "TaskGroupImpl{" +
                "name='" + name + '\'' +
                ", credits=" + credits +
                ", wordsToHash=" + wordsToHash +
                ", owner='" + owner + '\'' +
                '}';
    }

    public List<WorkerRI> getWorkers() {
        return workers;
    }

    @Override
    public Integer getNumberOfParts() throws RemoteException{
        return numberOfParts;
    }

    @Override
    public Integer getAssignedWork() throws RemoteException {
        return this.assignedWork;
    }

    public List<String> getUsers() {
        return users;
    }
}