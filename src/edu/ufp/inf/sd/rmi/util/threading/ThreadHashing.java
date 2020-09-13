package edu.ufp.inf.sd.rmi.util.threading;


import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerImpl;
import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerRI;
import edu.ufp.inf.sd.rmi.hashmatching.server.State;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ThreadHashing {

    String input;
    WorkerImpl worker;
    List<WorkerRI> allWorkersOfTask;

    public ThreadHashing(String stringToHash, WorkerImpl worker, List<WorkerRI> workers) {
        this.input = stringToHash;
        this.worker = worker;
        this.allWorkersOfTask = workers;
    }

    public Runnable run() {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                try {
                    // getInstance() é chamado com o algoritmo SH!-512
                    MessageDigest md = MessageDigest.getInstance("SHA-512");

                    // digest() é chamado para calcular o resumo da mensagem da string de input
                    // retorna um array de bytes
                    byte[] messageDigest = md.digest(input.getBytes());

                    // converte o array de bytes numa representação de sinal
                    BigInteger no = new BigInteger(1, messageDigest);

                    // converte a mensagem em hexadecimal
                    String hashtext = no.toString(16);

                    // adiciona os 0's antes para terem 32 btis
                    while (hashtext.length() < 32) {
                        hashtext = "0" + hashtext;
                    }
                    /** state = texto para codificar, worker que está a trabalhar e thread atual */
                    State state = new State(hashtext, worker, Thread.currentThread(), "");
                    System.out.println("input->" + input + "\n hash " + worker.getName() + " :" + hashtext);
                    try {
                        if (hashtext.equalsIgnoreCase(worker.getTaskGroup().getHashCode())) {
                            /** se encontrar o mesmo hash - notifica os workers - imprime "encontrei hash" */
                            state.setPass(input);
                            worker.getTaskGroup().notifyWorkers(state);
                        }

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }// For specifying wrong message digest algorithms
                catch (NoSuchAlgorithmException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
                    throw new RuntimeException(e);

                }
            }
        };
        return r;
    }

    @Override
    public String toString() {
        return "ThreadHashing{" +
                "input='" + input + '\'' +
                ", worker=" + worker +
                '}';
    }
}
