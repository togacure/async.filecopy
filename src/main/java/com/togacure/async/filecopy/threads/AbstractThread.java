package com.togacure.async.filecopy.threads;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.togacure.async.filecopy.threads.messages.ChangeStateMessage;
import com.togacure.async.filecopy.threads.messages.IMessage;
import com.togacure.async.filecopy.util.FileDescriptor;
import com.togacure.async.filecopy.util.Utils;
import com.togacure.async.filecopy.util.exceptions.FileNotSelectedException;
import com.togacure.async.filecopy.util.exceptions.IncorrectFlowState;
import com.togacure.async.filecopy.util.exceptions.OperationDeniedException;
import com.togacure.async.filecopy.util.exceptions.ThreadStopException;

import lombok.SneakyThrows;

public abstract class AbstractThread implements IThread {

	private static final int DEFAULT_INITIAL_CAPACITY = 32;

	private static final int INITIAL_THREAD_POOL_SIZE = 2;

	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(INITIAL_THREAD_POOL_SIZE);

	private FileDescriptor fileDescriptor;

	private ThreadState currentState = ThreadState.death;

	private final Lock lock = new ReentrantLock(true);

	private final BlockingQueue<IMessage> messageQueue = new PriorityBlockingQueue<IMessage>(DEFAULT_INITIAL_CAPACITY,
			(v1, v2) -> {
				return v1.compareTo(v2);
			});

	@Override
	public FileDescriptor getFileDescriptor() {
		lock.lock();
		try {
			return fileDescriptor;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public ThreadState getCurrentState() {
		lock.lock();
		try {
			return currentState;
		} finally {
			lock.unlock();
		}
	}

	@Override
	@SneakyThrows({ InterruptedException.class, OperationDeniedException.class })
	public void run() {
		IMessage message;
		while ((message = messageQueue.take()) != null) {
			if (message instanceof ChangeStateMessage && getCurrentState() == ThreadState.death) {
				closeFile();
				break;
			} else {
				try {
					handleMessage(message);
				} catch (ThreadStopException e) {
					setState(ThreadState.death);
				}
			}
		}
	}

	@Override
	public void switchFile(FileDescriptor fd) throws OperationDeniedException {
		lock.lock();
		try {
			switch (currentState) {
			case alive:
			case paused:
				throw new OperationDeniedException(
						"Can not perform an operation on the thread that performs the task.");
			case death:
				checkFileDescriptor(fd);
				fileDescriptor = fd;
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	@SneakyThrows(InterruptedException.class)
	public ThreadState switchState() throws OperationDeniedException {
		lock.lock();
		try {
			switch (currentState) {
			case alive:
				currentState = ThreadState.paused;
				break;
			case paused:
				checkFileDescriptor(fileDescriptor);
				currentState = ThreadState.alive;
				break;
			case death:
				checkFileDescriptor(fileDescriptor);
				currentState = ThreadState.alive;
				startTask();
			}
			messageQueue.put(new ChangeStateMessage());
			return currentState;
		} finally {
			lock.unlock();
		}
	}

	@SneakyThrows(InterruptedException.class)
	protected void setState(ThreadState state) throws OperationDeniedException {
		lock.lock();
		try {
			switch (currentState) {
			case alive:
				currentState = Optional.ofNullable(state).filter((v) -> {
					return v == ThreadState.paused || v == ThreadState.death;
				}).orElseThrow(IncorrectFlowState::new);
				break;
			case paused:
				checkFileDescriptor(fileDescriptor);
				currentState = Optional.ofNullable(state).filter((v) -> {
					return v == ThreadState.alive || v == ThreadState.death;
				}).orElseThrow(IncorrectFlowState::new);
				break;
			case death:
				checkFileDescriptor(fileDescriptor);
				openFile();
				currentState = Optional.ofNullable(state).filter((v) -> {
					return v == ThreadState.alive;
				}).orElseThrow(IncorrectFlowState::new);
				startTask();
			}
			messageQueue.put(new ChangeStateMessage());
		} finally {
			lock.unlock();
		}
	}

	private void startTask() {
		THREAD_POOL.submit(this);
	}

	private static void checkFileDescriptor(FileDescriptor fd) throws FileNotSelectedException {
		Optional.ofNullable(fd).map(v -> v.toPath()).filter(Utils::isNotNullOrEmpty)
				.orElseThrow(FileNotSelectedException::new);
	}

}
