package edu.ufp.inf.sd.rmi.hashmatching.server;

import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerImpl;
import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerRI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class TaskGroupImpl implements TaskGroupRI, Serializable {
    private Long id;

    private String name;

    private DataBase database;

    /**
     * hash a encontrar = hashCode
     */
    private String hashCode = "";

    private int delta;

    private Integer credits = 0;

    /**
     * ficheiro a procurar = testdarkc0de.txt
     */
    private File file = new File("C:/Users/crist/Documents/IntelliJ/SD/src/edu/ufp/inf/sd/rmi/util/testdarkcode.txt");

    private File fileHashToFind = new File("C:/Users/crist/Documents/IntelliJ/SD/src/edu/ufp/inf/sd/rmi/util/hashToFind.txt");

    private List<String> wordsToHash = new ArrayList<String>();

    private String owner;

    private List<WorkerRI> workers;

    public TaskGroupImpl(String owner, String name) throws RemoteException {
        this.owner = owner;
        this.name = name;
        this.wordsToHash = this.saveWords();
        this.hashCode = this.saveHash();
        this.workers = new ArrayList<>();
        export();
    }

    /**
     * adiciona worker passado em parametro ao array list de workers da taskgroup
     *
     * @param w - worker a ser adicionado
     * @throws RemoteException
     */
    public void attach(WorkerRI w) throws RemoteException {
        this.workers.add(w);
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

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * prepara o trabalho:
     * divide numero de linhas = linesOfFile
     * pelo numero dos workers = nWorkers
     * isto é o delta
     * count = n workers, vai de 1 em 1
     * lineAux = começa em 0 e vai de delta em delta
     * envia de que linha o worker precisa de ler
     * e quantas linhas ele precisa,
     * chama o sendToWorker
     *
     * @throws RemoteException
     */
    @Override
    public void prepareWork() throws RemoteException {
        Integer linesOfFile = this.wordsToHash.size();
        Integer nWorkers = this.workers.size();

        this.delta = linesOfFile / nWorkers; //calcular delta
        System.out.println("delta->" + this.delta);

        int count = 1;
        int lineAux = 0;

        for (WorkerRI w : this.workers) {//iniciar o trabalho do worker
            if (count != 1) {
                lineAux += delta;
            }
            w.setNumberOfLine(lineAux);//update worker
            w.setDelta(this.delta);
            this.sendToWorker(w);//iniciar trabalho
            count++;
        }

    }

    /**
     * envia para o worker passado:
     * guarda as linhas a ler em myLines
     * inicia poolThread com as linhas a ler
     *
     * @param worker
     * @throws RemoteException
     */
    public void sendToWorker(WorkerRI worker) throws RemoteException {
        List<String> myLines = new ArrayList<String>();
        int workerStartLine = worker.getNumberOfLine();
        for (int i = workerStartLine; i < workerStartLine + this.delta; i++) {
            myLines.add(this.wordsToHash.get(i));
        }
        System.out.println("sendToWorker(): " + worker.getName() + "\n");

        worker.initAllThreads(myLines);

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
     * chama update que imprime "sei que o hash foi encontrado"
     *
     * @param state   -  hashCode, WorkerImpl worker, Thread thread, String pass - pass encontrada
     * @throws RemoteException
     */
    @Override
    public void notifyWorkers(State state) throws RemoteException {
        System.out.println(" \n \n \nestou no notify workers: \n");
        for (WorkerRI w : this.workers) {
            w.update(state);
        }
    }

    public String getHashCode() {
        return hashCode;
    }

    public List<WorkerRI> getWorkers() {
        return workers;
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
}