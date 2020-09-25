package edu.ufp.inf.sd.rmi.hashmatching.server;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class FactoryImpl extends UnicastRemoteObject implements FactoryRI {

    public DataBase dataBase;

    protected FactoryImpl() throws RemoteException {
        dataBase = new DataBase();
    }

    /**
     * fazer com que o user faça login:
     * procura se já existe na base de dados, o username com aquela pass associada
     *
     * @param username - username do user a tentar fazer login
     * @param pass     - pass inserida para o login
     * @return uma sessao para aquele utilizador
     * @throws RemoteException
     */
    public SessionRI login(String username, String pass) throws RemoteException {
        SessionRI session = null;
        User user = this.dataBase.verifyUser(username, pass);
        System.out.println(user.toString());
        if (user != null) {
            session = new SessionImpl(dataBase, this, user.getName());
            this.dataBase.getSessions().put(username, session);
        }
        return session;
    }

    /**
     * faz o registo daquele utilizador:
     * vai à base de dados verificar se o username já existe e adiciona
     *
     * @param username - a adicionar
     * @param pass     - passe do user
     * @return true se não houver nenhum username igual, false se já houver igual
     * @throws RemoteException
     */
    public boolean register(String username, String pass) throws RemoteException {
        User user = null;
        TaskGroupImpl taskGroup = null;
        if (this.dataBase.userExists(username)) {
            return false;
        }
        user = dataBase.registerUser(username, pass);
        return true;

    }

    /**
     * faz logout : remove a sessao na lista
     *
     * @param username - o user que quer fazer logout
     */
    @Override
    public void removeSession(String username) {
        this.dataBase.getSessions().remove(username);
    }
}