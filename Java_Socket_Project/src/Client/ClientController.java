package Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ClientController implements Initializable {

	@FXML
	private Button connBtn;
	@FXML
	private Button receiveBtn;
	@FXML
	private ComboBox uidComboBox;
	@FXML
	private TextArea clientLog;
	@FXML
	private TextField clientInput;
	@FXML
	private Button sendBtn;
	@FXML
	private TextField userIdInput;

	Socket socket;

	private ObservableList<String> comboBoxList = FXCollections.observableArrayList("��ο���");
	private String opponentUserID;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		opponentUserID="��ο���";
		
		uidComboBox.setItems(comboBoxList);

		connBtn.setOnAction(event -> handleClientBtnAction(event));

		sendBtn.setOnAction(event -> handleClientMessageSendAction(event));

		receiveBtn.setOnAction(event -> handleClientMessageReceiveAction(event));
	}

	public void handleClientMessageReceiveAction(ActionEvent event) {
		String data = "";
		data = stringProcess("receive", data);
		send(data);
	}

	public void handleClientMessageSendAction(ActionEvent event) {
		String text = "";
		if (clientInput.getText() != null) {
			text = clientInput.getText();
			String data = stringProcess("send", text);
			send(data);
		}
	}

	public void handleComboChange(ActionEvent event) {
		opponentUserID = uidComboBox.getValue().toString();
	}

	public void handleClientBtnAction(ActionEvent event) {
		if (connBtn.getText().equals("connect")) {
			startClient();
		} else if (connBtn.getText().equals("disconnect")) {
			stopClient();
		}
	}

	// Ŭ���̾�Ʈ ���α׷��� �۵��� �����ϴ� �޼ҵ�
	void startClient() {

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					socket = new Socket();

					String userId;

					userId = stringProcess("id", userIdInput.getText());
					// ���� ���� �õ� ���� ���� ����
					if (userIdInput.getText().length() != 0 && !userIdInput.getText().equals("���̵� �Է�")) {
						socket.connect(new InetSocketAddress("localhost", 5001));
						Platform.runLater(() -> {
							displayText("[���� �Ϸ�: " + socket.getRemoteSocketAddress() + "]");
							connBtn.setText("disconnect");
							sendBtn.setDisable(false);
							receiveBtn.setDisable(false);
							clientInput.setDisable(false);
						});
						send(userId);
						userIdInput.setDisable(true);
					}
					// ���� ���� ����
					else {
						Platform.runLater(() -> displayText("[���� ����] ���̵� �Է����ּ���."));
					}
				} catch (Exception e) {
					Platform.runLater(() -> displayText("[���� ��� �ȵ�]"));
					if (!socket.isClosed()) {
						stopClient();
					}
					return;
				}
				receive();
			}
		};
		thread.start();
	}

	// Ŭ���̾�Ʈ ���α׷��� �۵��� �����ϴ� �޼ҵ�
	void stopClient() {
		try {
			Platform.runLater(() -> {
				displayText("[���� ����]");
				connBtn.setText("connect");
				sendBtn.setDisable(true);
			});
			userIdInput.setDisable(false);
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (IOException e) {
		}
	}

	// �����κ��� �޽����� ���޹޴� �޼ҵ�
	void receive() {
		while (true) {
			try {
				byte[] byteArr = new byte[100];
				InputStream inputStream = socket.getInputStream();

				// ������ ������������ �������� ��� IOException �߻�
				int readByteCount = inputStream.read(byteArr);

				// ������ ���������� Socket�� close()�� ȣ������ ���
				if (readByteCount == -1) {
					throw new IOException();
				}

				String data = new String(byteArr, 0, readByteCount, "UTF-8");

				Platform.runLater(() -> displayText("[�ޱ� �Ϸ�] " + data));
			} catch (Exception e) {
				Platform.runLater(() -> displayText("[���� ��� �ȵ�]"));
				stopClient();
				break;
			}
		}
	}

	// ������ �޽����� �����ϴ� �޼ҵ�
	void send(String data) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					byte[] byteArr = data.getBytes("UTF-8");
					OutputStream outputStream = socket.getOutputStream();
					outputStream.write(byteArr);
					outputStream.flush();
					Platform.runLater(() -> displayText("[������ �Ϸ�]"));
				} catch (Exception e) {
					Platform.runLater(() -> displayText("[���� ��� �ȵ�]"));
					stopClient();
				}
			}
		};
		thread.start();
	}

	// Ŭ���̾�Ʈ �α�â�� �޽��� ����ϴ� �޼ҵ�
	void displayText(String text) {

		clientLog.appendText(text + "\n");
	}

	String stringProcess(String cmd, String msg) {
		String res = new String();
		switch (cmd) {
		case "id":
			res = "id//";
			break;
		case "send":
			res = "send//";
			res += opponentUserID + "//";
			break;
		case "receive":
			res = "receive//";
			break;
		}
		return res + msg;
	}
}
