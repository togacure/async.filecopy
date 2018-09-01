package com.togacure.async.filecopy.threads;

import static com.togacure.async.filecopy.util.Syncronized.execute;
import static com.togacure.async.filecopy.util.Syncronized.get;
import static com.togacure.async.filecopy.util.Syncronized.throwsExecute;
import static com.togacure.async.filecopy.util.Syncronized.throwsGet;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.togacure.async.filecopy.threads.messages.ChangeStateMessage;
import com.togacure.async.filecopy.threads.messages.EOFMessage;
import com.togacure.async.filecopy.threads.messages.IMessage;
import com.togacure.async.filecopy.threads.messages.ResumeOperationsMessage;
import com.togacure.async.filecopy.threads.messages.SingleOperationMessage;
import com.togacure.async.filecopy.threads.messages.SuspendOperationsMessage;
import com.togacure.async.filecopy.util.FileDescriptor;
import com.togacure.async.filecopy.util.Utils;
import com.togacure.async.filecopy.util.exceptions.FileNotSelectedException;
import com.togacure.async.filecopy.util.exceptions.OperationDeniedException;
import com.togacure.async.filecopy.util.exceptions.ThreadStopException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractThread implements IThread, IMessageReceiver {

	private static final int DEFAULT_INITIAL_CAPACITY = 32;

	private static final int INITIAL_THREAD_POOL_SIZE = 2;

	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(INITIAL_THREAD_POOL_SIZE);

	private FileDescriptor fileDescriptor;

	private ThreadState currentState = ThreadState.death;

	private final Lock lock = new ReentrantLock(true);

	private final Object sleepMonitor = new Object();

	private final BlockingQueue<IMessage> messageQueue = new PriorityBlockingQueue<IMessage>(DEFAULT_INITIAL_CAPACITY,
			(v1, v2) -> {
				return v1.compareTo(v2);
			});

	@Override
	public FileDescriptor getFileDescriptor() {
		return get(lock, () -> fileDescriptor);
	}

	@Override
	public ThreadState getCurrentState() {
		return get(lock, () -> currentState);
	}

	@Override
	@SneakyThrows({ InterruptedException.class, OperationDeniedException.class })
	public void run() {
		log.info("{}", this);
		openFile();
		Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
			log.error("", e);
			stopTask();
		});
		try {
			IMessage message;
			while ((message = messageQueue.take()) != null) {
				log.debug("message: {}", message);

				if (message instanceof ChangeStateMessage) {
					switch (getCurrentState()) {
					case alive:
						handleMessage(new ResumeOperationsMessage());
						break;
					case paused:
						handleMessage(new SuspendOperationsMessage());
						break;
					default:
						log.error("icorrect state: {}", getCurrentState());
						throw new ThreadStopException();
					}
				} else if (message instanceof SingleOperationMessage) {
					handleMessage((SingleOperationMessage) message);

				} else {
					throw new RuntimeException(String.format("Unknown message %s", message));
				}
			}
		} catch (ThreadStopException e) {
			log.info("catch stop");
			stopTask();
		}
		log.info("done: {}", this);
	}

	@Override
	@SneakyThrows(InterruptedException.class)
	public void handleMessage(SingleOperationMessage message) throws ThreadStopException {
		log.debug("message: {}", message);
		if (message instanceof ResumeOperationsMessage) {
			getLabelObserver().observeThreadState(currentState);
			log.debug("resume: {}", this);
			synchronized (sleepMonitor) {
				sleepMonitor.notify();
			}
		} else if (message instanceof SuspendOperationsMessage) {
			getLabelObserver().observeThreadState(currentState);
			log.debug("sleep: {}", this);
			synchronized (sleepMonitor) {
				sleepMonitor.wait();
			}
		} else if (message instanceof EOFMessage) {
			throw new ThreadStopException();
		}
	}

	@Override
	public void switchFile(FileDescriptor fd) throws OperationDeniedException {
		log.info("fd: {}", fd);
		throwsExecute(lock, () -> {
			switch (currentState) {
			case alive:
			case paused:
				throw new OperationDeniedException(
						"Can not perform an operation on the thread that performs the task.");
			case death:
				checkFileDescriptor(fd);
				fileDescriptor = fd;
			}
		});
	}

	@Override
	public ThreadState switchState() throws OperationDeniedException {
		return throwsGet(lock, () -> {
			log.info("currentState: {}", currentState);
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
			putMessageWrapper(new ChangeStateMessage());
			return currentState;
		});
	}

	@Override
	public void receiveMessage(IMessage message) {
		putMessageWrapper(message);
	}

	@SneakyThrows(InterruptedException.class)
	public static final void shutdown() {
		if (!THREAD_POOL.isTerminated()) {
			THREAD_POOL.shutdownNow();
			THREAD_POOL.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	private void startTask() {
		log.info("{}", this);
		THREAD_POOL.execute(this);
	}

	private void stopTask() {
		execute(lock, () -> {
			closeFile();
			currentState = ThreadState.death;
			getLabelObserver().observeThreadState(currentState);
		});
	}

	@SneakyThrows(InterruptedException.class)
	protected void putMessageWrapper(IMessage msg) {
		log.debug("currentState: {} msg: {}", currentState, msg);
		messageQueue.put(msg);
	}

	@SneakyThrows(FileNotSelectedException.class)
	private static void checkFileDescriptor(FileDescriptor fd) {
		Optional.ofNullable(fd).map(v -> v.toPath()).filter(Utils::isNotNullOrEmpty)
				.orElseThrow(FileNotSelectedException::new);
	}

}
