package com.togacure.async.filecopy.threads;

import com.togacure.async.filecopy.ui.ThreadControlButtonLable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ThreadState {

	death(ThreadControlButtonLable.start), alive(ThreadControlButtonLable.stop), paused(ThreadControlButtonLable.start);

	private ThreadControlButtonLable buttonLabel;

}
