package edu.ufp.inf.sd.rmi.hashmatching.server;

import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerImpl;
import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerRI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


public class SessionImpl implements SessionRI {


    DataBase dataBase;

    String username;

    private FactoryImpl factory;

    public SessionImpl(DataBase dataBase, FactoryImpl factoryImpl, String user) throws RemoteException {
        this.factory = factoryImpl;
        this.dataBase = dataBase;
        this.username = user;
        String users = dataBase.printUsers();
        System.out.println(users);
        UnicastRemoteObject.exportObject(this, 0);
    }

    /**
     * criar taskgroup se nome da task for válido:
     * vai procurar se o nome da task já existe na base de dados,
     * se não existir, insere no array de sessões da base de dados daquele utilizador
     *
     * @param name - nome da task a criar - unico
     * @return false se for null o nome ou se já houver uma task com aquele nome, true se conseguir criar na base de dados
     * @throws RemoteException
     */
    @Override
    public boolean createTaskGroup(String name) throws RemoteException {
        if (username == null) {
            System.out.println("username is null \n");
            return false;
        }
        TaskGroupImpl taskGroup = new TaskGroupImpl(username, name);
        if (this.dataBase.getTaskGroupsOfUser(username) != null) {
            for (TaskGroupImpl tg : this.dataBase.getTaskGroupsOfUser(username)) {
                if (tg.getName().equals(name)) { // task name already been created then return false
                    return false;
                }
            }
        }
        this.dataBase.addTaskGroup(taskGroup);
        return true;
    }

    /**
     * lista taskgroup daquele utilizador
     *
     * @return lista de strings com info das tasks
     * @throws RemoteException
     */
    @Override
    public List<String> listTaskGroups() throws RemoteException {
        List<String> tasks = new ArrayList<>();
        for (TaskGroupImpl tg : this.dataBase.getTaskGroupsOfUser(this.username)) {
            tasks.add(tg.getName());
        }
        return tasks;
    }

    /**
     * elimina task da base de dados
     *
     * @param name - nome da task a eliminar
     * @return true se eliminar da base de dados, false se não houver nenhuma task com aquele nome
     * @throws RemoteException
     */
    @Override
    public boolean deleteTaskGroup(String name) throws RemoteException {
        if (this.dataBase.getTaskGroupsOfUser(username) != null) {
            for (TaskGroupImpl tg : this.dataBase.getTaskGroupsOfUser(username)) {
                if (tg.getName().equals(name)) {
                    this.dataBase.deleteTaskGroup(tg);
                    return true;
                }
            }
        }
        return false;
    }

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
    @Override
    public boolean attachWorker(String workerName, String task) throws RemoteException {
        TaskGroupImpl tg = this.dataBase.getTaskGroupsByName(task, username);
        WorkerRI worker = new WorkerImpl(workerName, tg);
        List<String> tasks = new ArrayList<>();
        if (tg != null) {
            List<WorkerImpl> workersInTask = this.dataBase.getWorkersTaskGroup(task);//get task groups worker
            workersInTask.add(worker.getWorker());//add new worker
            tg.attach(worker);//put array list
            return true;
        }
        return false;
    }

    /**
     * procura task pelo nome e lista os seus workers
     *
     * @param taskGroupName - nome da task a procurar
     * @return - lista de strings com info dos workers daquela task
     * @throws RemoteException
     */
    @Override
    public List<String> listWorkers(String taskGroupName) throws RemoteException {
        System.out.println("\nlistWorker():");
        List<String> workers = new ArrayList<>();
        if (this.dataBase.getWorkersOfTaskGroup(taskGroupName, this.username) != null) {
            for (WorkerImpl worker : this.dataBase.getWorkersOfTaskGroup(taskGroupName, this.username)) {
                System.out.println("listWorkers(): worker=" + worker.getName());
                workers.add(worker.getName());
            }
            return workers;
        }
        return new ArrayList<>();
    }

    /**
     * envia sinal para a taskgroup preparar para inciar o trabalho (lançar threads para o hashing)
     *
     * @param taskGroup - nome da task para iniciar trabalho
     * @return false se não conseguir enviar sinal para iniciar trabalho, true se mandar o sinal
     * @throws RemoteException
     */
    @Override
    public boolean startWork(String taskGroup) throws RemoteException {
        TaskGroupImpl tg = this.dataBase.getTaskGroupsByName(taskGroup, this.username);
        for (WorkerImpl w : this.dataBase.getWorkersOfTaskGroup(taskGroup, this.username)) {
            tg.prepareWork();
            return true;
        }
        return false;
    }

    /**
     * termina sessao - logout
     * retira sessao com este username do user do array de sessoes da factory
     *
     * @throws RemoteException
     */
    @Override
    public void logOut() throws RemoteException {
        factory.removeSession(this.username);
    }

}