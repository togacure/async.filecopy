package com.togacure.async.filecopy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.val;

public class AsyncFilecopyApp extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		val loader = new FXMLLoader();
		val scene = new Scene((Parent) loader.load(getClass().getResourceAsStream("/fxml/main.fxml")));
		primaryStage.setTitle("Async file copy");
		primaryStage.setResizable(true);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
