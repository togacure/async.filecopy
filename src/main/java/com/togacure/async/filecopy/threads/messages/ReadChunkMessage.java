package com.togacure.async.filecopy.threads.messages;

import com.togacure.async.filecopy.mem.Chunk;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class ReadChunkMessage extends SingleOperationMessage {

	@NonNull
	private final Chunk chunk;

	@Override
	public int compareTo(IMessage msg) {
		return (msg instanceof ReadChunkMessage)
				? Integer.valueOf(chunk.getReadOrder()).compareTo(((ReadChunkMessage) msg).getChunk().getReadOrder())
				: super.compareTo(msg);
	}
}
