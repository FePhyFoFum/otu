package org.opentree.otu.exceptions;

public class NoSuchTreeException extends Exception {

	String error = "";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoSuchTreeException(String error) {
		this.error = error;
	}

    @Override
    public String toString() {
    	return error;
    }
}
