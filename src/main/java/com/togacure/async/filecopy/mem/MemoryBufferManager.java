package com.togacure.async.filecopy.mem;

import static com.togacure.async.filecopy.util.Syncronized.execute;
import static com.togacure.async.filecopy.util.Syncronized.get;
import static com.togacure.async.filecopy.util.Syncronized.throwsGet;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.togacure.async.filecopy.util.exceptions.MemBufferIsBusyException;
import com.togacure.async.filecopy.util.exceptions.OperationDeniedException;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryBufferManager {

	public static final int IO_GRANULATION = 0x1000;

	private final Lock lock = new ReentrantLock(true);

	private SortedSet<Chunk> freeChunks = new TreeSet<Chunk>();

	@Getter
	private volatile int size = 0;
	@Getter
	private volatile int stat = 0;

	@Getter
	private volatile byte[] buffer;

	@SneakyThrows(OperationDeniedException.class)
	public MemoryBufferManager(int size) {
		resize(size);
	}

	public void resize(int size) throws OperationDeniedException {
		buffer = throwsGet(lock, () -> {
			isBusy();
			this.size = size;
			init();
			return new byte[size];
		});
		log.info(" size: {} freeChunks: {}", size, freeChunks);
	}

	public Chunk malloc() {
		return get(lock, () -> {
			if (freeChunks.isEmpty()) {
				return null;
			}
			val result = freeChunks.first();
			freeChunks.remove(result);
			stat += result.getSize();
			log.debug("chunk: {} stat: {}", result, stat);
			return result;
		});
	}

	public void free(Chunk chunk) {
		execute(lock, () -> {
			freeChunks.add(chunk);
			stat -= chunk.getSize();
			log.debug("chunk: {} stat: {}", chunk, stat);
			chunk.setDataSize(0);
		});
	}

	private void init() {
		freeChunks.clear();
		if (size <= IO_GRANULATION) {
			freeChunks.add(new Chunk(0, size));
		} else {
			int chunkNum = size / IO_GRANULATION;
			for (int i = 0; i < chunkNum; i++) {
				freeChunks.add(new Chunk(i * IO_GRANULATION, IO_GRANULATION));
			}
			int tail = size - (chunkNum * IO_GRANULATION);
			if (tail > 0) {
				freeChunks.add(new Chunk(chunkNum * IO_GRANULATION, tail));
			}
		}
	}

	@SneakyThrows(MemBufferIsBusyException.class)
	private void isBusy() {
		if (stat > 0) {
			throw new MemBufferIsBusyException();
		}
	}
}
