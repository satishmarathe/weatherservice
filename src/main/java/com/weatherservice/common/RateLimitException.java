package com.weatherservice.common;

public class RateLimitException extends RuntimeException {
	
	public RateLimitException(String errorMessage) {
        super(errorMessage);
    }
	
	public RateLimitException(String errorMessage, Throwable t) {
        super(errorMessage,t);
    }
	
}


