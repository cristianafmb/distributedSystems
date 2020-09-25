package edu.ufp.inf.sd.rmi.hashmatching.client;


import edu.ufp.inf.sd.rmi.hashmatching.server.TaskGroupRI;
import edu.ufp.inf.sd.rmi.util.threading.ThreadHashing;
import edu.ufp.inf.sd.rmi.util.threading.ThreadPool;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorkerImpl extends UnicastRemoteObject implements WorkerRI {

    private static final Integer fixedThreads = 4;

    private String name;

    private String responsibleUser;

    private Integer numberOfLine;

    private Integer delta; // numero de linhas

    private TaskGroupRI tg;

    private List<Long> threads = new ArrayList<Long>();

    public WorkerImpl(String workerName, TaskGroupRI tgRI, String responsible) throws RemoteException {
        this.name = workerName;
        this.tg = tgRI;
        this.responsibleUser = responsible;
    }

    @Override
    public Integer getNumberOfLine() throws RemoteException {
        return numberOfLine;
    }

    @Override
    public void setNumberOfLine(Integer numberOfLine) throws RemoteException {
        this.numberOfLine = numberOfLine;
    }

    @Override
    public String getResponsible() throws RemoteException {
        return responsibleUser;
    }

    public String getName() throws RemoteException {
        return name;
    }

    public void setName(String name) throws RemoteException {
        this.name = name;
    }

    public TaskGroupRI getTaskGroup() throws RemoteException {
        return tg;
    }


    /**
     * recebe ordem de parar as threads, então vai buscar o id de cada thread
     * compara se contem na lista de threads do worker, se contiver faz stop na thread
     * imprime que todas as threads foram paradas
     *
     * @throws RemoteException
     */
    @Override
    public void update() throws RemoteException {
        System.out.println("\n update(): worker - " + this.name + "now stopping threads \n");
        //get all current threads
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread t : threadSet) {
            if (threads.contains(t.getId())) {
                t.stop();
            }
        }
        System.out.println("All threads has been stopped! \n");

    }

    /**
     * inicia a thread pool com 4 threads, para encontrarem o hash
     * n threadas para cada linha = linhas passadas / 4
     * rst = linhas passadas % 4
     * enquanto houver resto, guarda nas primeiras threads 1 a mais
     * inicia o initEachThread
     *
     * @param myLines - texto que se encontra no ficheiro "testdarkc0de.txt"
     * @throws RemoteException
     */
    @Override
    public void initAllThreads(List<String> myLines, boolean moreWork) throws RemoteException {
        if (moreWork) {
            System.out.println("moreWork");
        }
        System.out.println("\n MY LINES: " + myLines.toString());
        ThreadPool pool = new ThreadPool(fixedThreads);
        int countThreads = 0;
        int numberLinesForEachThread = myLines.size() / fixedThreads;
        int rest = myLines.size() % fixedThreads;
        int count = 1;
        List<String> linesToHash = new ArrayList<String>();
        for (int i = 0; i <= myLines.size(); i++) {
            if (count > numberLinesForEachThread) {
                countThreads++;
                System.out.println("Thread " + countThreads);
                if (rest != 0 && i != myLines.size()) {
                    linesToHash.add(myLines.get(i));
                    rest--;
                    i++;
                }
                initEachThread(linesToHash, pool);
                if (i == myLines.size()) {
                    return;
                }
                linesToHash = new ArrayList<String>();
                count = 1;
            }
            linesToHash.add(myLines.get(i));
            count++;

        }
    }

    /**
     * inicia thread pool com numberLinesForEachThread linhas para cada thread
     *
     * @param linesForHash texto com x linhas para cada thread
     * @param pool         pool de threads a serem executadas
     * @throws RemoteException
     */
    @Override
    public void initEachThread(List<String> linesForHash, ThreadPool pool) throws RemoteException {
        System.out.println("linesForHash in init each thread" + linesForHash.toString());
        Runnable r = new ThreadHashing(linesForHash, this);
        pool.execute(r);
    }

    @Override
    public WorkerImpl getWorker() throws RemoteException {
        return this;
    }

    @Override
    public void setDelta(Integer delta) throws RemoteException {
        this.delta = delta;
    }

    @Override
    public TaskGroupRI getTg() throws RemoteException {
        return tg;
    }

    /**
     * adiciona a thread acabada de fazer ao array de threads
     * chama o moreWork em tg e verifica se tem mais trabalho a fazer
     * se retornar false, não existe mais work a fazer para aquele user a que o worker pertence
     *
     * @param state estado recebido da threadHashing
     * @throws RemoteException
     */
    @Override
    public void setState(State state) throws RemoteException {
        //  System.out.println("date in set state "+state.toString());
        this.threads.add(state.getThreadId());
        if (true) {
            boolean checkIfHasMoreLines = this.getTg().moreWork(this);
            if (checkIfHasMoreLines == false) {
                // System.out.println("Work done for me!");
            }
        }
    }

    @Override
    public String toString() {
        return "WorkerImpl{" +
                ", name='" + name + '\'' +
                ", taskGroupName='" + tg + '\'' +
                '}';
    }

}