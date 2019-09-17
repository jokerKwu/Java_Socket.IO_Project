package Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import  javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import  javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class ClientController implements Initializable{
	
	@FXML private Button connBtn;
	@FXML private Button receiveBtn;
	@FXML private ComboBox uidComboBox;
	@FXML private TextArea clientLog;
	@FXML private TextField clientInput;
	@FXML private Button sendBtn;
	@FXML private TextField userIdInput;
	
	Socket socket;
	
	@Override
	public void initialize(URL location,ResourceBundle resources) {
		
		
		connBtn.setOnAction(event->handleClientBtnAction(event));
		
		sendBtn.setOnAction(event->handleClientMessageSendAction(event));
	}
	
	public void handleClientMessageSendAction(ActionEvent event) {
		String text="";
		if(clientInput.getText()!=null) {
			text=clientInput.getText();
			send(text);
		}
	}
	
	public void handleClientBtnAction(ActionEvent event) {
		if(connBtn.getText().equals("connect")) {
			startClient();
		} else if(connBtn.getText().equals("disconnect")){
			stopClient();
		}
	}
	void startClient() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress("localhost", 5001));
					Platform.runLater(()->{
						displayText("[���� �Ϸ�: "  + socket.getRemoteSocketAddress() + "]");
						connBtn.setText("disconnect");
				        sendBtn.setDisable(false);
				        receiveBtn.setDisable(false);
				        clientInput.setDisable(false);
				        
					});
				} catch(Exception e) {
					Platform.runLater(()->displayText("[���� ��� �ȵ�]"));
					if(!socket.isClosed()) { stopClient(); }
					return;
				}
				receive();
			}
		};
		thread.start();
	}
	
	void stopClient() {
		try {
			Platform.runLater(()->{
				displayText("[���� ����]");
				connBtn.setText("connect");
				sendBtn.setDisable(true);
			});
			if(socket!=null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {}
	}	
	
	void receive() {
		while(true) {
			try {
				byte[] byteArr = new byte[100];
				InputStream inputStream = socket.getInputStream();
				
				//������ ������������ �������� ��� IOException �߻�
				int readByteCount = inputStream.read(byteArr);
				
				//������ ���������� Socket�� close()�� ȣ������ ���
				if(readByteCount == -1) { throw new IOException(); }
				
				String data = new String(byteArr, 0, readByteCount, "UTF-8");
				
				Platform.runLater(()->displayText("[�ޱ� �Ϸ�] "  + data));
			} catch (Exception e) {
				Platform.runLater(()->displayText("[���� ��� �ȵ�]"));
				stopClient();
				break;
			}
		}
	}
	
	void send(String data) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {		
					byte[] byteArr = data.getBytes("UTF-8");
					OutputStream outputStream = socket.getOutputStream();
					outputStream.write(byteArr);
					outputStream.flush();
					Platform.runLater(()->displayText("[������ �Ϸ�]"));
				} catch(Exception e) {
					Platform.runLater(()->displayText("[���� ��� �ȵ�]"));
					stopClient();
				}				
			}
		};
		thread.start();
	}


	void displayText(String text) {
	
		clientLog.appendText(text + "\n");
	}	
	
	
}
