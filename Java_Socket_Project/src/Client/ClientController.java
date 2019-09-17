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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
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
	
	// 클라이언트 프로그램의 작동을 시작하는 메소드
	void startClient() {
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					socket = new Socket();
					
					String userId=userIdInput.getText().toString();
					System.out.println(userId.length());
					
					// 서버와 연결 시도
					// 서버 연결 성공
					if(userId.length()!=0&&!userId.equals("아이디 입력")) {
						socket.connect(new InetSocketAddress("localhost", 5001));
						Platform.runLater(() -> {
							displayText("[연결 완료: " + socket.getRemoteSocketAddress() + "]");
							connBtn.setText("disconnect");
							sendBtn.setDisable(false);
							receiveBtn.setDisable(false);
							clientInput.setDisable(false);
						});
						send(userId);
						userIdInput.setDisable(true);
					}
					//서버 연결 실패
					else {
						Platform.runLater(()->displayText("[연결 실패] 아이디 입력해주세요."));
					}
				} catch(Exception e) {
					Platform.runLater(()->displayText("[서버 통신 안됨]"));
					if(!socket.isClosed()) { stopClient(); }
					return;
				}
				receive();
			}
		};
		thread.start();
	}
	
	// 클라이언트 프로그램의 작동을 종료하는 메소드
	void stopClient() {
		try {
			Platform.runLater(()->{
				displayText("[연결 끊음]");
				connBtn.setText("connect");
				sendBtn.setDisable(true);
			});
			userIdInput.setDisable(false);
			if(socket!=null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {}
	}	
	
	// 서버로부터 메시지를 전달받는 메소드
	void receive() {
		while(true) {
			try {
				byte[] byteArr = new byte[100];
				InputStream inputStream = socket.getInputStream();
				
				//서버가 비정상적으로 종료했을 경우 IOException 발생
				int readByteCount = inputStream.read(byteArr);
				
				//서버가 정상적으로 Socket의 close()를 호출했을 경우
				if(readByteCount == -1) { throw new IOException(); }
				
				String data = new String(byteArr, 0, readByteCount, "UTF-8");
				
				Platform.runLater(()->displayText("[받기 완료] "  + data));
			} catch (Exception e) {
				Platform.runLater(()->displayText("[서버 통신 안됨]"));
				stopClient();
				break;
			}
		}
	}
	
	// 서버로 메시지를 전송하는 메소드
	void send(String data) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {		
					byte[] byteArr = data.getBytes("UTF-8");
					OutputStream outputStream = socket.getOutputStream();
					outputStream.write(byteArr);
					outputStream.flush();
					Platform.runLater(()->displayText("[보내기 완료]"));
				} catch(Exception e) {
					Platform.runLater(()->displayText("[서버 통신 안됨]"));
					stopClient();
				}				
			}
		};
		thread.start();
	}

	//클라이언트 로그창에 메시지 기록하는 메소드
	void displayText(String text) {
	
		clientLog.appendText(text + "\n");
	}	
	
	
}
