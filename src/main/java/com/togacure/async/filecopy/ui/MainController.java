package com.togacure.async.filecopy.ui;

import java.util.Optional;

import com.togacure.async.filecopy.util.FileDescriptor;
import com.togacure.async.filecopy.util.Utils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class MainController {

	@FXML
	private Label inputFileLabel;

	@FXML
	private Label outputFileLabel;

	@FXML
	private Label copyBufferFillValueLabel;

	@FXML
	private ProgressBar copyBufferFillValueProgressBar;

	@FXML
	private TextField copyBufferSizeTextField;

	@FXML
	private Label readThreadStateLabel;

	@FXML
	private Label writeThreadStateLabel;

	private FileDescriptor inputFile;

	private FileDescriptor outputFile;

	@FXML
	public void selectInputFile(final ActionEvent event) {
		Platform.runLater(() -> {
			chooseFile(inputFileLabel, event.getSource(), "Select input file", inputFile, false).ifPresent((fd) -> {
				inputFile = fd;
				inputFileLabel.setText(inputFile.toPath());
				Optional.ofNullable(outputFile).ifPresent((f) -> {
					f.setFile(inputFile.getFile());
					outputFileLabel.setText(f.toPath());
				});
			});
		});
	}

	@FXML
	public void selectOutputDirectory(final ActionEvent event) {
		Platform.runLater(() -> {
			chooseFile(outputFileLabel, event.getSource(), "Select output directory", outputFile, true)
					.ifPresent((fd) -> {
						outputFile = fd;
						Optional.ofNullable(inputFile).ifPresent((f) -> {
							outputFile.setFile(f.getFile());
						});
						outputFileLabel.setText(outputFile.toPath());
					});
		});
	}

	@FXML
	public void onInputCopyBufferSize() {
		Platform.runLater(() -> {
			Optional.ofNullable(copyBufferSizeTextField.getText()).filter(Utils::isNotNullOrEmpty).ifPresent((v) -> {
				copyBufferSizeTextField.setText(v.replaceAll("[^\\d]", ""));
			});
			Optional.ofNullable(copyBufferSizeTextField.getText()).filter(Utils::isNullOrEmpty).ifPresent((v) -> {
				copyBufferSizeTextField.setText("256");
			});
		});
	}

	@FXML
	public void readThreadControl(final ActionEvent event) {
		Platform.runLater(() -> {

		});
	}

	@FXML
	public void writeThreadControl(final ActionEvent event) {
		Platform.runLater(() -> {

		});
	}

	private Optional<FileDescriptor> chooseFile(Label label, Object button, String title, FileDescriptor old,
			boolean asDir) {
		return Optional.ofNullable(button).map((b) -> {
			return new ChooseFileProvider(((Button) button).getScene().getWindow(), title);
		}).map((chooser) -> {
			return Optional.ofNullable(label).map((l) -> {
				return chooser.chooseFile(old, asDir);
			});
		}).get();
	}
}
