package com.togacure.async.filecopy.threads;

import com.togacure.async.filecopy.threads.messages.IMessage;
import com.togacure.async.filecopy.util.FileDescriptor;
import com.togacure.async.filecopy.util.exceptions.CloseFileException;
import com.togacure.async.filecopy.util.exceptions.OperationDeniedException;
import com.togacure.async.filecopy.util.exceptions.ThreadStopException;

public interface IThread extends Runnable {

	FileDescriptor getFileDescriptor();

	ThreadState getCurrentState();

	void switchFile(FileDescriptor fd) throws OperationDeniedException;

	ThreadState switchState() throws OperationDeniedException;

	void handleMessage(IMessage message) throws ThreadStopException;

	void openFile() throws OperationDeniedException;

	void closeFile() throws CloseFileException;
}
