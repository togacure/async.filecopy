package com.togacure.async.filecopy.mem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import com.togacure.async.filecopy.util.exceptions.InvalidBufferSizeException;
import com.togacure.async.filecopy.util.exceptions.OperationDeniedException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MemoryBuffer {

	public static final int INITIAL_BUFFER_SIZE = 0x100;
	public static final int MIN_BUFFER_SIZE = 0x10;
	public static final int MAX_BUFFER_SIZE = 0x100000;

	private final MemoryBufferManager manager = new MemoryBufferManager(INITIAL_BUFFER_SIZE);

	private final AtomicInteger readOrder = new AtomicInteger();

	@NonNull
	private final IMemoryBufferStateObserver observer;

	public int size() {
		return manager.getSize();
	}

	public void resize(int capacity) throws OperationDeniedException {
		if (capacity < MIN_BUFFER_SIZE || capacity > MAX_BUFFER_SIZE) {
			throw new InvalidBufferSizeException(capacity);
		}
		manager.resize(capacity);
	}

	public Chunk malloc() {
		return manager.malloc();
	}

	@SneakyThrows(IOException.class)
	public void in(@NonNull InputStream is, @NonNull Chunk chunk) {
		val readed = is.read(manager.getBuffer(), chunk.getOffset(), chunk.getSize());
		chunk.setDataSize(readed);
		chunk.setReadOrder(readOrder.getAndIncrement());
		log.debug("chunk: {}", chunk);
		if (chunk.getDataSize() > 0) {
			change();
		}
	}

	@SneakyThrows(IOException.class)
	public void out(@NonNull OutputStream os, @NonNull Chunk chunk) {
		log.debug("chunk: {}", chunk);
		if (chunk.getDataSize() > 0) {
			os.write(manager.getBuffer(), chunk.getOffset(), chunk.getDataSize());
		}
		manager.free(chunk);
		change();
	}

	private void change() {
		observer.observe((((double) manager.getStat()) / manager.getSize()) * 100);
	}
}
