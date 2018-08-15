package com.togacure.async.filecopy.util.exceptions;

@SuppressWarnings("serial")
public class MemBufferIsBusyException extends OperationDeniedException {

	public MemBufferIsBusyException() {
		super("Buffer contains unread data");
	}

}
