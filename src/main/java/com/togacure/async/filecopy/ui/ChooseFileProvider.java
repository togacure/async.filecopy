package com.togacure.async.filecopy.ui;

import java.io.File;
import java.util.Optional;

import com.togacure.async.filecopy.util.FileDescriptor;
import com.togacure.async.filecopy.util.Utils;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class ChooseFileProvider {

	private Window window;

	private String title;

	public FileDescriptor chooseFile(FileDescriptor oldValue, boolean asDir) {
		if (asDir) {
			val chooser = new DirectoryChooser();
			chooser.setTitle(title);
			Optional.ofNullable(oldValue).filter(Utils::isNotNullOrEmpty).map((fd) -> {
				return fd.getDir();
			}).filter(Utils::isNotNullOrEmpty).ifPresent((dir) -> {
				chooser.setInitialDirectory(new File(dir));
			});
			val file = chooser.showDialog(window);
			return Utils.isNullOrEmpty(file) ? null : new FileDescriptor(file.getAbsolutePath(), null);
		} else {
			val chooser = new FileChooser();
			chooser.setTitle(title);
			Optional.ofNullable(oldValue).filter(Utils::isNotNullOrEmpty).map((fd) -> {
				return fd.getDir();
			}).filter(Utils::isNotNullOrEmpty).ifPresent((dir) -> {
				chooser.setInitialDirectory(new File(dir));
			});
			Optional.ofNullable(oldValue).filter(Utils::isNotNullOrEmpty).map((fd) -> {
				return fd.getFile();
			}).filter(Utils::isNotNullOrEmpty).ifPresent((f) -> {
				chooser.setInitialFileName(f);
			});
			val file = chooser.showOpenDialog(window);
			return Utils.isNullOrEmpty(file) ? null
					: new FileDescriptor(
							Utils.isNullOrEmpty(file.getParent()) ? "." + File.pathSeparator : file.getParent(),
							file.getName());
		}

	}

}
