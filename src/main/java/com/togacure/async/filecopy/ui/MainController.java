package com.togacure.async.filecopy.ui;

import java.util.Optional;

import com.togacure.async.filecopy.threads.ThreadsHolder;
import com.togacure.async.filecopy.util.FileDescriptor;
import com.togacure.async.filecopy.util.Utils;
import com.togacure.async.filecopy.util.exceptions.OperationDeniedException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	@FXML
	private Button readThreadButton;

	@FXML
	private Button writeThreadButton;

	private FileDescriptor inputFile;

	private FileDescriptor outputFile;

	private final ThreadsHolder threadsHolder = new ThreadsHolder((v) -> {
		log.debug("buffer fill: {}", v);
		Platform.runLater(() -> {
			copyBufferFillValueLabel.setText(String.format("%s %%", v));
			copyBufferFillValueProgressBar.setProgress(v);
		});
	}, (st) -> {
		log.info("read file change state observer: st: {}", st);
		Platform.runLater(() -> {
			readThreadStateLabel.setText(st.name());
			readThreadButton.setText(st.getButtonLabel().name());
		});
	}, (st) -> {
		log.info("write file change state observer: st: {}", st);
		Platform.runLater(() -> {
			writeThreadStateLabel.setText(st.name());
			writeThreadButton.setText(st.getButtonLabel().name());
		});
	});

	@FXML
	public void selectInputFile(final ActionEvent event) {
		Platform.runLater(() -> {
			chooseFile(inputFileLabel, event.getSource(), "Select input file", inputFile, false).ifPresent((fd) -> {
				log.info("fd: {}", fd);
				inputFile = fd;
				inputFileLabel.setText(inputFile.toPath());
				Optional.ofNullable(outputFile).ifPresent((f) -> {
					f.setFile(inputFile.getFile());
					outputFileLabel.setText(f.toPath());
				});
				try {
					threadsHolder.getReadThread().switchFile(fd);
				} catch (OperationDeniedException e) {
					log.error("", e);
					Utils.alertError(e.getMessage());
				}
			});
		});
	}

	@FXML
	public void selectOutputDirectory(final ActionEvent event) {
		Platform.runLater(() -> {
			chooseFile(outputFileLabel, event.getSource(), "Select output directory", outputFile, true)
					.ifPresent((fd) -> {
						log.info("fd: {}", fd);
						outputFile = fd;
						Optional.ofNullable(inputFile).ifPresent((f) -> {
							outputFile.setFile(f.getFile());
						});
						outputFileLabel.setText(outputFile.toPath());
						try {
							threadsHolder.getWriteThread().switchFile(outputFile);
						} catch (OperationDeniedException e) {
							log.error("", e);
							Utils.alertError(e.getMessage());
						}
					});
		});
	}

	@FXML
	public void onInputCopyBufferSize() {
		Platform.runLater(() -> {
			log.debug("value: {}", copyBufferSizeTextField.getText());
			Optional.ofNullable(copyBufferSizeTextField.getText()).filter(Utils::isNotNullOrEmpty).ifPresent((v) -> {
				val text = v.replaceAll("[^\\d]", "");
				val position = copyBufferSizeTextField.getCaretPosition();
				copyBufferSizeTextField.setText(text);
				copyBufferSizeTextField.positionCaret(position < text.length() ? position : text.length());
			});
			Optional.ofNullable(copyBufferSizeTextField.getText()).filter(Utils::isNullOrEmpty).ifPresent((v) -> {
				copyBufferSizeTextField.setText("256");
				copyBufferSizeTextField.positionCaret(3);
			});
			try {
				threadsHolder.getBuffer().resize(Integer.valueOf(copyBufferSizeTextField.getText()));
			} catch (NumberFormatException e) {
				log.error("", e);
				throw new RuntimeException(e);
			} catch (OperationDeniedException e) {
				log.error("", e);
				Utils.alertError(e.getMessage());
			}
		});
	}

	@FXML
	public void readThreadControl(final ActionEvent event) {
		log.debug("event: {}", event);
		Platform.runLater(() -> {
			try {
				threadsHolder.getReadThread().switchState();
			} catch (OperationDeniedException e) {
				log.error("", e);
				Utils.alertError(e.getMessage());
			}
		});
	}

	@FXML
	public void writeThreadControl(final ActionEvent event) {
		log.debug("event: {}", event);
		Platform.runLater(() -> {
			try {
				threadsHolder.getWriteThread().switchState();
			} catch (OperationDeniedException e) {
				log.error("", e);
				Utils.alertError(e.getMessage());
			}
		});
	}

	public void shutdown() {
		threadsHolder.getReadThread().shutdown();
		threadsHolder.getWriteThread().shutdown();
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
