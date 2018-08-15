package com.togacure.async.filecopy.util.exceptions;

import com.togacure.async.filecopy.mem.MemoryBuffer;

@SuppressWarnings("serial")
public class InvalidBufferSizeException extends OperationDeniedException {

	public InvalidBufferSizeException(int size) {
		super(String.format("Invalid buffer size %s. The size should be between %s and %s.", size,
				MemoryBuffer.MIN_BUFFER_SIZE, MemoryBuffer.MAX_BUFFER_SIZE));
	}
}
