package com.togacure.async.filecopy.util.exceptions;

@SuppressWarnings("serial")
public class FileNotSelectedException extends OperationDeniedException {

	public FileNotSelectedException() {
		super("You must select a file");
	}
}
