package edu.ufp.inf.sd.rmi.util.threading;


import edu.ufp.inf.sd.rmi.hashmatching.client.State;
import edu.ufp.inf.sd.rmi.hashmatching.client.WorkerImpl;

import java.io.Serializable;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ThreadHashing  implements Runnable, Serializable {

    List<String> stringToHash;
    WorkerImpl worker;
    State state;



    public ThreadHashing(List<String> stringToHash, WorkerImpl worker) {
        this.stringToHash = stringToHash;
        this.worker = worker;

    }

    //public void run() {
    //    Runnable r = new Runnable() {
    @Override
    public void run() {
        synchronized (worker) {
            for (String input : stringToHash) {

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

                    boolean find = hashtext.equalsIgnoreCase(worker.getTaskGroup().getHashCode());
                    //System.out.println("thread id in  thread hashing: "+Thread.currentThread().getId());
                    /** state = texto para codificar, worker que está a trabalhar e thread atual */
                    state = new State(hashtext, worker, "",Thread.currentThread().getId(), find);


                   // System.out.println("input->" + input + "\n hash " + worker.getName() + " :" + hashtext);
                    try {

                        if (find) {
                            /** se encontrar o mesmo hash - notifica os workers - imprime "encontrei hash" */
                            state.setPass(input);
                            state.setFind(true);

                            /** se encontrar a pass chama o notify workers */
                            this.worker.getTg().notifyWorkers(state);
                            System.out.println("\n Pass found : " + state.pass);
                        }else{
                            /**
                             * se não encontrar a pass pede mais trabalho
                             * atualiza state do worker */
                            worker.setState(state);
                        }



                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }// For specifying wrong message digest algorithms
                catch (NoSuchAlgorithmException | RemoteException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
                    throw new RuntimeException(e);

                }
            }
        }
        try {
            this.worker.setState(state);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //    };
    }

    @Override
    public String toString() {
        return "ThreadHashing{" +
                "stringToHash=" + stringToHash +
                ", worker=" + worker +
                ", state=" + state +
                '}';
    }
}
