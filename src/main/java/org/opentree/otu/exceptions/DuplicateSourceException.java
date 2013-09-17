package org.opentree.otu.exceptions;

public class DuplicateSourceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String error = "";
	
	public DuplicateSourceException(String error) {
		this.error = error;
	}
	
	@Override
	public String toString() {
		return error;
	}

}
