package com.togacure.async.filecopy.threads;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.togacure.async.filecopy.mem.Chunk;
import com.togacure.async.filecopy.mem.MemoryBuffer;
import com.togacure.async.filecopy.threads.messages.EOFMessage;
import com.togacure.async.filecopy.threads.messages.FreeChunkMessage;
import com.togacure.async.filecopy.threads.messages.ReadChunkMessage;
import com.togacure.async.filecopy.threads.messages.ResumeOperationsMessage;
import com.togacure.async.filecopy.threads.messages.SingleOperationMessage;
import com.togacure.async.filecopy.ui.IUIThreadStateObserver;
import com.togacure.async.filecopy.util.exceptions.CloseFileException;
import com.togacure.async.filecopy.util.exceptions.FileOperationException;
import com.togacure.async.filecopy.util.exceptions.OperationDeniedException;
import com.togacure.async.filecopy.util.exceptions.ThreadStopException;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ReadFileThread extends AbstractThread {

	@NonNull
	private final MemoryBuffer buffer;

	@NonNull
	private final IMessageReceiver writeThreadReceiver;

	@Getter
	@NonNull
	private final IUIThreadStateObserver labelObserver;

	// TODO single read thread
	private final ThreadLocal<InputStream> inputStream = new ThreadLocal<InputStream>();

	@Override
	public void handleMessage(SingleOperationMessage message) throws ThreadStopException {
		log.debug("message: {}", message);
		super.handleMessage(message);
		if (message instanceof ResumeOperationsMessage || message instanceof FreeChunkMessage) {
			Chunk chunk;
			while ((chunk = buffer.malloc()) != null) {
				log.debug("chunk: {}", chunk);
				buffer.in(inputStream.get(), chunk);
				if (chunk.getDataSize() >= 0) {
					writeThreadReceiver.receiveMessage(new ReadChunkMessage(chunk));
				} else {
					writeThreadReceiver.receiveMessage(new EOFMessage());
					throw new ThreadStopException();
				}
			}
		}
	}

	@Override
	public void openFile() throws OperationDeniedException {
		try {
			if (inputStream.get() == null) {
				inputStream.set(new FileInputStream(getFileDescriptor().toPath()));
			}
		} catch (FileNotFoundException e) {
			throw new FileOperationException(e.getMessage());
		}
	}

	@Override
	public void closeFile() throws CloseFileException {
		try {
			inputStream.get().close();
		} catch (IOException e) {
			throw new CloseFileException(e.getMessage());
		}
		inputStream.set(null);
	}

}
