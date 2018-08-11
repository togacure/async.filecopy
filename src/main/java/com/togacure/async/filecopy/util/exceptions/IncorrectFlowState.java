package com.togacure.async.filecopy.util.exceptions;

@SuppressWarnings("serial")
public class IncorrectFlowState extends OperationDeniedException {

	public IncorrectFlowState() {
		super("Incorrect flow state");
	}

}
