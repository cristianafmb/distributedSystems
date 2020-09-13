package edu.ufp.inf.sd.rmi.hashmatching.server;

import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerImpl;

public class State {

    public String hashCode;

    public WorkerImpl worker;

    public Thread thread;

    public String pass ="";

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public State(String hashCode, WorkerImpl worker, Thread thread, String pass) {
        this.hashCode = hashCode;
        this.worker = worker;
        this.thread = thread;
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "State{" +
                "hashCode='" + hashCode + '\'' +
                ", worker=" + worker +
                '}';
    }
}