package com.togacure.async.filecopy.util;

import com.togacure.async.filecopy.util.exceptions.OperationDeniedException;

@FunctionalInterface
public interface IOperationCallback {

	void call() throws OperationDeniedException;
}
