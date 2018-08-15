package com.togacure.async.filecopy.mem;

@FunctionalInterface
public interface IMemoryBufferStateObserver {

	void observe(double percent);
}
