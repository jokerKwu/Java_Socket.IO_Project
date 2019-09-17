package Server;

import java.awt.Button;
import java.awt.TextArea;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Server extends Application {

	// UI 持失 五社球
	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = (Parent) FXMLLoader.load(getClass().getResource("serverUI.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("serverStyle.css").toString());

		primaryStage.setScene(scene);
		primaryStage.setTitle("Server");
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
