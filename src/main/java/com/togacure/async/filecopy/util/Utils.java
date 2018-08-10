package com.togacure.async.filecopy.util;

import java.util.Collection;
import java.util.Map;

public abstract class Utils {

	public static final boolean isNotNullOrEmpty(Object o) {
		return !isNullOrEmpty(o);
	}

	public static final boolean isNullOrEmpty(Object o) {
		if (o == null) {
			return true;
		} else if (o instanceof String) {
			return ((String) o).isEmpty();
		} else if (o instanceof Collection) {
			return ((Collection<?>) o).isEmpty();
		} else if (o instanceof Map) {
			return ((Map<?, ?>) o).isEmpty();
		}
		return false;
	}

	private Utils() {
	}
}
