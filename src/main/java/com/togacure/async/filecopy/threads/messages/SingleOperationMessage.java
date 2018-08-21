package com.togacure.async.filecopy.threads.messages;

import lombok.Getter;
import lombok.ToString;

@ToString
public abstract class SingleOperationMessage implements IMessage {

	@Getter
	private MessagePriority priority = MessagePriority.NORMAL;
}
