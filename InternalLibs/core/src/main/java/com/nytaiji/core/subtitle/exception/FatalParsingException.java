package com.nytaiji.core.subtitle.exception;

public class FatalParsingException extends Exception {

	private static final long serialVersionUID = 6798827566637277804L;
	
	private final String parsingError;
	
	public FatalParsingException(String parsingError){
		super(parsingError);
		this.parsingError = parsingError;
	}
	
	@Override
	public String getLocalizedMessage(){
		return parsingError;
	}
	
}
