package com.togacure.async.filecopy.util;

import java.util.Collection;
import java.util.Map;

import javafx.scene.control.Alert;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class Utils {

	public static final boolean isNotNullOrEmpty(Object o) {
		return !isNullOrEmpty(o);
	}

	public static final boolean isNullOrEmpty(Object o) {
		if (o == null) {
			return true;
		} else if (o instanceof String) {
			return ((String) o).trim().isEmpty();
		} else if (o instanceof Collection) {
			return ((Collection<?>) o).isEmpty();
		} else if (o instanceof Map) {
			return ((Map<?, ?>) o).isEmpty();
		}
		return false;
	}

	public static final void alertError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error!");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
