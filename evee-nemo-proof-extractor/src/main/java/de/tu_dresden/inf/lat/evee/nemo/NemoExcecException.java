package de.tu_dresden.inf.lat.evee.nemo;

public class NemoExcecException extends Exception {

    public NemoExcecException(String msg){
        super(msg);
    }

    public NemoExcecException(String msg, Throwable e){
        super(msg, e);
    }
}
