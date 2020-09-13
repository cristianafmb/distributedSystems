package edu.ufp.inf.sd.rmi.hashmatching.client;

import edu.ufp.inf.sd.rmi.hashmatching.server.State;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface WorkerRI extends Remote {
    /**
     * imprime quem encontrou o hash
     *
     * @param s - state da thread com o worker que encontrou o hash
     * @throws RemoteException
     */
    void update(State s) throws RemoteException;

    /**
     * inicia a thread pool, para encontrarem o hash
     * cria um array de todas as threads para serem guardadas que contêm o threadHashing
     *
     * @param myLines - texto que se encontra no ficheiro "testdarkc0de.txt"
     * @throws RemoteException
     */
    void initAllThreads(List<String> myLines) throws RemoteException;

    WorkerImpl getWorker() throws RemoteException;

    String getName();

    Integer getNumberOfLine();

    void setNumberOfLine(Integer lineAux);

    void setDelta(Integer delta);
}