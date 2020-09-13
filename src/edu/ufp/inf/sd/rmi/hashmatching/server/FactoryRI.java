package edu.ufp.inf.sd.rmi.hashmatching.server;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FactoryRI extends Remote {

    /**
     * fazer com que o user faça login:
     * procura se já existe na base de dados, o username com aquela pass associada
     *
     * @param username - username do user a tentar fazer login
     * @param pass     - pass inserida para o login
     * @return uma sessao para aquele utilizador
     * @throws RemoteException
     */
    SessionRI login(String username, String pass) throws RemoteException;

    /**
     * faz o registo daquele utilizador:
     * vai à base de dados verificar se o username já existe e adiciona
     *
     * @param username - a adicionar
     * @param pass     - passe do user
     * @return true se não houver nenhum username igual, false se já houver igual
     * @throws RemoteException
     */
    boolean register(String username, String pass) throws RemoteException;

    /**
     * faz logout : remove a sessao na lista
     *
     * @param username - o user que quer fazer logout
     */
    void removeSession(String username) throws RemoteException;

}