package com.cts.exceptions;

public class CandidateNotFoundException extends RuntimeException {
    //Parametrized Constructor
    public CandidateNotFoundException(String msg){
        super(msg);
    }
}
