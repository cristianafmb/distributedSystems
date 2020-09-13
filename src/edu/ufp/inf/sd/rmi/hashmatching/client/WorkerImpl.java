package edu.ufp.inf.sd.rmi.hashmatching.client;


import edu.ufp.inf.sd.rmi.hashmatching.server.State;
import edu.ufp.inf.sd.rmi.hashmatching.server.TaskGroupImpl;
import edu.ufp.inf.sd.rmi.util.threading.ThreadHashing;
import edu.ufp.inf.sd.rmi.util.threading.ThreadPool;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class WorkerImpl implements WorkerRI, Serializable {

    public Integer threadsNumber;

    private String name;

    private Integer numberOfLine;

    private Integer delta; // numero de linhas

    private TaskGroupImpl taskGroup;

    private List<Thread> threads = new ArrayList<Thread>();

    public WorkerImpl(String name, TaskGroupImpl tg) {

        this.name = name;
        this.taskGroup = tg;

    }

    public Integer getThreadsNumber() {
        return threadsNumber;
    }

    public void setThreadsNumber(Integer threadsNumber) {
        this.threadsNumber = threadsNumber;
    }

    @Override
    public Integer getNumberOfLine() {
        return numberOfLine;
    }

    @Override
    public void setNumberOfLine(Integer numberOfLine) {
        this.numberOfLine = numberOfLine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskGroupImpl getTaskGroup() {
        return taskGroup;
    }


    /**
     * imprime quem encontrou o hash
     *
     * @param s - state da thread com o worker que encontrou o hash
     * @throws RemoteException
     */
    @Override
    public void update(State s) throws RemoteException {
        System.out.println(this.name + "-> sei que o hash foi encontrado \n\n pass encontrada: "+s.getPass());

    }

    /**
     * inicia a thread pool com o tamanho das linhas, para encontrarem o hash
     * cria um array de todas as threads para serem guardadas que contêm o threadHashing
     * cria um runnable r que tem todas as threads (=*threadHashing)
     * chama a pool e sincroniza as threads através da lista ligada que tem
     *
     * @param myLines - texto que se encontra no ficheiro "testdarkc0de.txt"
     * @throws RemoteException
     */
    @Override
    public void initAllThreads(List<String> myLines) throws RemoteException {
        ThreadPool pool = new ThreadPool(myLines.size());
        List<Thread> allThreads = new ArrayList<>();
        for (int i = 0; i < myLines.size(); i++) {
            System.out.println("line to hash->" + myLines.get(i));
            Runnable r = new Thread(new ThreadHashing(myLines.get(i), this, this.getTaskGroup().getWorkers()).run());
            pool.execute(r);
        }
    }

    @Override
    public WorkerImpl getWorker() throws RemoteException {
        return this;
    }

    public Integer getDelta() {
        return delta;
    }

    @Override
    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    @Override
    public String toString() {
        return "WorkerImpl{" +
                "threadsNumber=" + threadsNumber +
                ", name='" + name + '\'' +
                ", taskGroupName='" + taskGroup + '\'' +
                '}';
    }
}