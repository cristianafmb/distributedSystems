package edu.ufp.inf.sd.rmi.hashmatching.server;

import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerImpl;
import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerRI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TaskGroupRI extends Remote {

    /**
     * adiciona worker passado em parametro ao array list de workers da taskgroup
     *
     * @param w - worker a ser adicionado
     * @throws RemoteException
     */
    void attach(WorkerRI w) throws RemoteException;

    /**
     * remove o worker passado em parametro do array list de workers da task
     *
     * @param w - worker a ser eliminado
     * @throws RemoteException
     */
    void dettach(WorkerRI w) throws RemoteException;

    /**
     * a retirar localmente para enviar ao rmi
     *
     * @throws RemoteException
     */
    void export() throws RemoteException;

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
    void prepareWork() throws RemoteException;

    /**
     * envia para o worker passado:
     * guarda as linhas a ler em myLines
     * inicia poolThread com as linhas a ler
     *
     * @param worker
     * @throws RemoteException
     */
    void sendToWorker(WorkerRI worker) throws RemoteException;

// private List<String> saveWords() throws RemoteException

    /**
     * notifica os workers todos que encontrou o hash,
     * chama update que imprime "sei que o hash foi encontrado"
     *
     * @param state   -  hashCode, WorkerImpl worker, Thread thread, String pass - pass encontrada
     * @throws RemoteException
     */
    void notifyWorkers(State state) throws RemoteException;

}