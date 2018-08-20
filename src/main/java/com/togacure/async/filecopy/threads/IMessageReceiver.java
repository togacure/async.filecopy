package com.togacure.async.filecopy.threads;

import com.togacure.async.filecopy.threads.messages.IMessage;

@FunctionalInterface
public interface IMessageReceiver {

	void receiveMessage(IMessage message);
}
