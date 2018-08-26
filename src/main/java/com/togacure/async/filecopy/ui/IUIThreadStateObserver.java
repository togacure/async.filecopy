package com.togacure.async.filecopy.ui;

import com.togacure.async.filecopy.threads.ThreadState;

@FunctionalInterface
public interface IUIThreadStateObserver {

	void observeThreadState(ThreadState state);
}
