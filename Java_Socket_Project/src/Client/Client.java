package Client;


import javafx.application.Application;

import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
	
	// UI »ý¼º
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
