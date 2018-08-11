package com.togacure.async.filecopy.threads.messages;

public interface IMessage extends Comparable<IMessage> {

	default MessagePriority getPriority() {
		return MessagePriority.LOW;
	}

	@Override
	default int compareTo(IMessage msg) {
		return msg.getPriority().compareTo(getPriority());
	}
}
