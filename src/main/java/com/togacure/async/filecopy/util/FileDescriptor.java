package com.togacure.async.filecopy.util;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public final class FileDescriptor {

	private String dir;

	@Setter
	private String file;

	public String toPath() {
		StringBuilder sb = new StringBuilder();
		if (!Utils.isNullOrEmpty(dir)) {
			sb.append(dir);
		}
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) != File.separatorChar) {
			sb.append(File.separatorChar);
		}
		if (!Utils.isNullOrEmpty(file)) {
			sb.append(file);
		}
		return sb.toString();
	}
}
