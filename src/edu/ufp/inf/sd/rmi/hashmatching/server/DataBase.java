package edu.ufp.inf.sd.rmi.hashmatching.server;

import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerImpl;
import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerRI;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class DataBase implements Serializable {

    // hashmap of taskGroup of users (the string is the name of user)
    HashMap<String, List<TaskGroupImpl>> taskGroups = new HashMap<String, List<TaskGroupImpl>>();
    //hashmap of workers in taskGroup ( the string is the name of task group)
    HashMap<String, List<WorkerRI>> taskWorkers = new HashMap<String, List<WorkerRI>>();


    //all tasks groups (the string is the name of task)
    HashMap<String, TaskGroupImpl> allTasks = new HashMap<String, TaskGroupImpl>();
    private Vector<User> users = new Vector<>();

    List<String> activeTaskGroups = new ArrayList<>();

    public HashMap<String, SessionRI> getSessions() {
        return sessions;
    }

    private HashMap<String, SessionRI> sessions = new HashMap<>();


    public DataBase() {
        super();
    }

    /**
     * regista outilizador/user na base de dados com o seu username único e a sua pass
     *
     * @param username único
     * @param pass     do user
     * @return o user/utilizador criado
     */
    public User registerUser(String username, String pass) {
        //System.out.println(username+" - "+pass+"-"+group);

        User user = null;
        if (!userExists(username)) {
            user = new User(username, pass);

            //add user in hash map of users
            this.users.add(user);

            //add user with empty task groups list
            this.taskGroups.put(user.getName(), new ArrayList<TaskGroupImpl>());

            System.out.println("User " + username + " has been created!!!");
            printUsers();
            return user;
        } else {
            System.out.println("Oh no, User " + username + " couldn't been created, username already taken!!!!!");
            return null;
        }
    }

    /**
     * verifica se o username do utilizador já se encontra na base de dados
     *
     * @param name username a ser procurado, a verificar se existe
     * @return true se já existe, false se não existe
     */
    public boolean userExists(String name) {
        for (User u : this.users) {
            if (u.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * vai a todos os users e guarda numa string
     *
     * @return a string com todos os utilizadores, username e pass
     */
    public String printUsers() {
        String s = "";
        for (User user : this.users) {
            s += "username:" + user.name + " pass: " + user.pass;
            s += "\n";
        }
        return s;
    }

    /**
     * verifica se o username e a pass correspondem à guardada na base de dados
     *
     * @param username do user a fazer login
     * @param pass     do user a fazer login
     * @return o user se encontrar, se não encontrar/não existir retorna null
     */
    public User verifyUser(String username, String pass) {
        this.printUsers();
        for (User u : this.users) {
            if (u.getName().equals(username) && u.getPass().equals(pass)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Dado o username, retorna as taskGroups que estão associadas ao user
     *
     * @param username do user
     * @return lista de taskgroups que correspondem ao user
     */
    public List<TaskGroupImpl> getTaskGroupsOfUser(String username) {
        if (this.taskGroups.get(username) == null) {
            System.out.println("There aren't any tasks with that username!!!");
        }
        return this.taskGroups.get(username);
    }

    /**
     * adiciona a taskgroup na base de dados, com o owner da task
     *
     * @param taskGroup task a guardar
     */
    public void addTaskGroup(TaskGroupImpl taskGroup) {
        List<TaskGroupImpl> myTasks = this.getTaskGroupsOfUser(taskGroup.getOwner());//ir buscar a lista de taskgroups do user
        if (myTasks == null) {//adicionar a taskGroup na lista
            myTasks = new ArrayList<TaskGroupImpl>();
        }
        myTasks.add(taskGroup);
        this.taskGroups.put(taskGroup.getOwner(), myTasks); // guardar na database
        this.taskWorkers.put(taskGroup.getName(), new ArrayList<WorkerRI>()); // guardar na database

    }

    /**
     * elimina a taskgroup da base de dados
     *
     * @param tg task a eliminar
     */
    public void deleteTaskGroup(TaskGroupImpl tg) {
        List<TaskGroupImpl> tasks = this.getTaskGroupsOfUser(tg.getOwner());
        tasks.remove(tg);
    }

    /**
     * procura a taskgroup com x nome e y owner e devolve-a
     *
     * @param taskGroupName - nome da taskgroup que se procura
     * @param username      - owner da taskgroup
     * @return a taskgroup procurada se encontrar, null se não houver nenhuma task com aquele nome pertencente aquele user
     */
    public TaskGroupImpl getTaskGroupsByName(String taskGroupName, String username) {
        for (TaskGroupImpl tg : this.taskGroups.get(username)) {
            if (tg.getName().equals(taskGroupName)) {
                return tg;
            }
        }
        return null;
    }


    /**
     * procura todos os workers de x taskgroup
     *
     * @param taskName - a que pertencem os workers a procurar
     * @param username - owner da taskgroup
     * @return todos os workers de x taskgroup
     */
    public List<WorkerRI> getWorkersOfTaskGroup(String taskName, String username) {
        List<WorkerRI> workers = new ArrayList<>();
        TaskGroupImpl tg = this.allTasks.get(taskName);
        for(WorkerRI w : tg.getWorkers()){
            workers.add(w);
        }
        return workers;
    }

    /**
     * procura taskgroup pelo nome e devolve os seus workers
     *
     * @param task - nome da task que se quer saber os workers
     * @return workers daquela task
     */
    public List<WorkerRI> getWorkersTaskGroup(String task) {
        return this.taskWorkers.get(task);
    }

    /**
     * taskGroups ativas
     * @return
     */
    public List<String> getActiveTaskGroups() {
        if(this.activeTaskGroups != null && !this.activeTaskGroups.isEmpty()){
            return this.activeTaskGroups;
        }
        return null;
    }

    /**
     * imprime os users associados aquela task
     * verifica se o numero de users associados aquela task é menor que n partes
     * se for adiciona o user aquela task
     * se nao retorna false
     * @param taskName
     * @param username
     * @return
     * @throws RemoteException
     */
    public boolean joinTaskGroup(String taskName, String username) throws RemoteException {

        TaskGroupImpl taskGroup = this.allTasks.get(taskName);
        System.out.println("users of task group");
        for (String s :
                taskGroup.getUsers()) {
            System.out.println(s);
        }

        if(taskGroup.getUsers().size()>=taskGroup.getNumberOfParts()){
            System.out.println("excedeu o numero de users");
            return false;
        }
        List<TaskGroupImpl> list = this.taskGroups.get(username);
        if(list==null){
            list = new ArrayList<>();
        }
        list.add(taskGroup);
        this.taskGroups.put(username,list);
        System.out.println("adicionou o user à task");
        System.out.println("tasks of user "+username+"->"+this.taskGroups.get(username));

        return true;
    }

    /**
     * adiciona a task no array de allTasks
     * @param taskGroup
     */
    public void addTaskToAllTasks(TaskGroupImpl taskGroup) {
        this.allTasks.put(taskGroup.getName(),taskGroup);
    }
}
