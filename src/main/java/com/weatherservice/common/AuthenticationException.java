package com.weatherservice.common;

public class AuthenticationException extends RuntimeException {
	
	public AuthenticationException(String errorMessage) {
        super(errorMessage);
    }
	
	public AuthenticationException(String errorMessage, Throwable t) {
        super(errorMessage,t);
    }
	
}


