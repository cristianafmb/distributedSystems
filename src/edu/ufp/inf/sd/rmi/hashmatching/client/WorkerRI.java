package edu.ufp.inf.sd.rmi.hashmatching.client;

import edu.ufp.inf.sd.rmi.hashmatching.server.TaskGroupRI;
import edu.ufp.inf.sd.rmi.util.threading.ThreadPool;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface WorkerRI extends Remote {


    /**
     * imprime quem encontrou o hash
     *
     * @throws RemoteException
     */
    public void update() throws RemoteException ;

    public String getResponsible() throws RemoteException ;

    /**
     * inicia a thread pool, para encontrarem o hash
     * cria um array de todas as threads para serem guardadas que contêm o threadHashing
     *
     * @param myLines - texto que se encontra no ficheiro "testdarkc0de.txt"
     * @throws RemoteException
     */
    void initAllThreads(List<String> myLines,boolean moreWork) throws RemoteException;

    WorkerImpl getWorker() throws RemoteException;

    TaskGroupRI getTg() throws RemoteException;

    String getName() throws RemoteException;

    Integer getNumberOfLine() throws RemoteException;

    void setNumberOfLine(Integer lineAux) throws RemoteException;

    public void setState(State state) throws RemoteException;

    void setDelta(Integer delta) throws RemoteException;
    public void initEachThread(List<String> linesForHash, ThreadPool pool) throws RemoteException;
}