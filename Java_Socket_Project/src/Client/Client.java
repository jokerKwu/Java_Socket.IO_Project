package Client;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Client extends Application {
	
	@Override
	public void start(Stage primaryStage) throws Exception{
		Parent root= (Parent)FXMLLoader.load(getClass().getResource("clientUI.fxml"));
		Scene scene=new Scene(root);
		scene.getStylesheets().add(getClass().getResource("clientStyle.css").toString());
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Client");
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
