package com.togacure.async.filecopy.threads;

import com.togacure.async.filecopy.threads.messages.SingleOperationMessage;
import com.togacure.async.filecopy.ui.IUIThreadStateObserver;
import com.togacure.async.filecopy.util.FileDescriptor;
import com.togacure.async.filecopy.util.exceptions.CloseFileException;
import com.togacure.async.filecopy.util.exceptions.OperationDeniedException;
import com.togacure.async.filecopy.util.exceptions.ThreadStopException;

public interface IThread extends Runnable {

	FileDescriptor getFileDescriptor();

	ThreadState getCurrentState();

	IUIThreadStateObserver getLabelObserver();

	void switchFile(FileDescriptor fd) throws OperationDeniedException;

	ThreadState switchState() throws OperationDeniedException;

	void handleMessage(SingleOperationMessage message) throws ThreadStopException;

	void openFile() throws OperationDeniedException;

	void closeFile() throws CloseFileException;

	void shutdown();
}
