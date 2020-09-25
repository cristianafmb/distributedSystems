package edu.ufp.inf.sd.rmi.hashmatching.client;


import java.io.Serializable;
import java.rmi.RemoteException;

public class State implements Serializable {

    public String hashCode;

    public WorkerRI worker;

    public String pass ="";

    public long threadId;

    public boolean find;


    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public boolean getFind() {
        return find;
    }

    public void setFind(boolean find) {
        this.find = find;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public State(String hashCode, WorkerRI worker, String pass, long thread_id,boolean find) throws RemoteException {
        this.hashCode = hashCode;
        this.worker = worker;
        this.pass = pass;
        this.threadId = thread_id;
        this.find = find;
    }

    @Override
    public String toString() {
        return "State{" +
                "hashCode='" + hashCode + '\'' +
                ", worker=" + worker +
                ", pass='" + pass + '\'' +
                ", threadId=" + threadId +
                '}';
    }
}