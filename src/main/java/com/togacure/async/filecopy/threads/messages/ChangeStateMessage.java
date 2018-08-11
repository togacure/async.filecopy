package com.togacure.async.filecopy.threads.messages;

import lombok.Getter;

public class ChangeStateMessage implements IMessage {

	@Getter
	private MessagePriority priority = MessagePriority.URGENT;
}
