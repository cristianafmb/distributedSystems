package edu.ufp.inf.sd.rmi.hashmatching.server;

public class User {

    public String name;

    public String pass;

    public User(String username, String pass) {
        this.name = username;
        this.pass = pass;
    }

    public FactoryImpl entry() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", pass='" + pass + '\'' +
                '}';
    }
}