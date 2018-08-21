package com.togacure.async.filecopy.mem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(exclude = "size")
public class Chunk implements Comparable<Chunk> {

	@NonNull
	private final Integer offset;
	@NonNull
	private final Integer size;
	@Setter
	private int dataSize = 0;

	@Override
	public int compareTo(Chunk o) {
		return offset.compareTo(o.offset);
	}
}
