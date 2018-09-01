package com.togacure.async.filecopy.threads;

import com.togacure.async.filecopy.mem.IMemoryBufferStateObserver;
import com.togacure.async.filecopy.mem.MemoryBuffer;
import com.togacure.async.filecopy.threads.messages.IMessage;
import com.togacure.async.filecopy.ui.IUICopyCompleteObserver;
import com.togacure.async.filecopy.ui.IUIThreadStateObserver;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class ThreadsHolder {

	private final MemoryBuffer buffer;
	private final AbstractThread readThread;
	private final AbstractThread writeThread;

	public ThreadsHolder(@NonNull IMemoryBufferStateObserver observer,
			@NonNull IUIThreadStateObserver readThreadLabelObserver,
			@NonNull IUIThreadStateObserver writeThreadLabelObserver,
			@NonNull IUICopyCompleteObserver copyCompleteObserver) {

		buffer = new MemoryBuffer(observer);
		readThread = new ReadFileThread(buffer, this::receiveFromRead, readThreadLabelObserver, copyCompleteObserver);
		writeThread = new WriteFileThread(buffer, this::receiveFromWrite, writeThreadLabelObserver,
				copyCompleteObserver);
	}

	private void receiveFromRead(IMessage message) {
		writeThread.receiveMessage(message);
	}

	private void receiveFromWrite(IMessage message) {
		readThread.receiveMessage(message);
	}
}
