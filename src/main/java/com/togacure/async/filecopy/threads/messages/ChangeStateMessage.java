package com.togacure.async.filecopy.threads.messages;

import lombok.Getter;
import lombok.ToString;

@ToString
public class ChangeStateMessage implements IMessage {

	@Getter
	private MessagePriority priority = MessagePriority.URGENT;
}
