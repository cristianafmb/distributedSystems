package edu.ufp.inf.sd.rmi.hashmatching.server;

import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerRI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SessionRI extends Remote {

    /**
     * criar taskgroup se nome da task for válido:
     * vai procurar se o nome da task já existe na base de dados,
     * se não existir, insere no array de sessões da base de dados daquele utilizador
     *
     * @param name - nome da task a criar - unico
     * @return false se for null o nome ou se já houver uma task com aquele nome, true se conseguir criar na base de dados
     * @throws RemoteException
     */
    boolean createTaskGroup(String name,String numberOfParts) throws RemoteException;

    /**
     * lista taskgroup daquele utilizador
     *
     * @return lista de strings com info das tasks
     * @throws RemoteException
     */
    List<String> listTaskGroups() throws RemoteException;

    /**
     * elimina task da base de dados
     *
     * @param name - nome da task a eliminar
     * @return true se eliminar da base de dados, false se não houver nenhuma task com aquele nome
     * @throws RemoteException
     */
    boolean deleteTaskGroup(String name) throws RemoteException;

    /**
     * associa worker a determinada taskgroup:
     * vai à base de dados procurar a task,
     * cria worker com aquele nome,
     * adiciona o worker no array de workers daquela task
     *
     * @param workerName - nome do worker a criar e a associar, nao é unico
     * @param task       - nome da task a ser associada ao worker
     * @return false se não conseguir adicionar, true se conseguir adicionar o worker à task
     * @throws RemoteException
     */
    boolean attachWorker(String workerName, String task) throws RemoteException;

    /**
     * procura task pelo nome e lista os seus workers
     *
     * @param taskGroupName - nome da task a procurar
     * @return - lista de strings com info dos workers daquela task
     * @throws RemoteException
     */
    public List<WorkerRI> listWorkers(String taskGroupName) throws RemoteException;

    /**
     * envia sinal para a taskgroup preparar para inciar o trabalho (lançar threads para o hashing)
     *
     * @param taskGroup - nome da task para iniciar trabalho
     * @return false se não conseguir enviar sinal para iniciar trabalho, true se mandar o sinal
     * @throws RemoteException
     */
    boolean startWork(String taskGroup) throws RemoteException;

    /**
     * termina sessao - logout
     * retira sessao com este username do user do array de sessoes da factory
     *
     * @throws RemoteException
     */
    void logOut() throws RemoteException;

    public TaskGroupRI getTaskGroup(String taskName) throws RemoteException;

    public List<String> getActiveTaskGroups () throws RemoteException;

    boolean joinTaskGroup(String taskName) throws RemoteException;

    String getUsername() throws RemoteException;
}