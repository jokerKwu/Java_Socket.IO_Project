package Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import DB.DAOFactory;
import OracleDAO.OracleDAO;
import OracleDAO.OracleDBConnection;
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
	@FXML
	private Button saveBtn;				
	@FXML
	private Button lastBtn;				// ������ ��ȭ ��������
	
	
	private ObservableList<String> comboBoxList = FXCollections.observableArrayList("��ο���");
	
	private String opponentUserID;
	
	Socket socket;
	
	private Set<String> userIDs;
	private DAOFactory daoFactory=null;
	private int session_id;
	private String save_last_sentence=null;
	
	
	private OracleDBConnection odb;
	private OracleDAO odao;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		opponentUserID = "��ο���";

		uidComboBox.setItems(comboBoxList);

		connBtn.setOnAction(event -> handleClientBtnAction(event));

		sendBtn.setOnAction(event -> handleClientMessageSendAction(event));

		receiveBtn.setOnAction(event -> handleClientMessageReceiveAction(event));
		
		saveBtn.setOnAction(event-> handleClientMessageSaveAction(event));
		
		lastBtn.setOnAction(event->{
			try {
				handleClientMessageLastAction(event);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		
		odb=OracleDBConnection.getInstance();
		odao=OracleDAO.getInstance();
		
	}
	public void handleClientMessageLastAction(ActionEvent event) throws SQLException {
		System.out.println("������ ���� ��������");
		String lastSentence=null;
		lastSentence=odao.DAO_select_lastReply(odb.getConnection(), session_id);	//�ش� ������ ������ ��ȭ ��������
		if(lastSentence==null) {
			Platform.runLater(() -> displayText("[������ ���� �������� ����] ��ȭ ����� �����ϴ�."));
		}
		else {
			Platform.runLater(() -> displayText("[������ ���� �������� ����] "));
			clientInput.setText(lastSentence);
		}
	}
	
	
	public void handleClientMessageSaveAction(ActionEvent event) {
		System.out.println("�޽��� ���� �׽�Ʈ");
		saveBtn.setDisable(true);
		Platform.runLater(() -> displayText("[������ ���� �������� ����] "));
		
	}
	
	public void handleClientMessageReceiveAction(ActionEvent event) {
		String data="";
		data=stringProcess("receive",data);
		saveBtn.setDisable(false);
		send(data);
		
		
		
	}
	
	//ä�� �޽��� 
	public void handleClientMessageSendAction(ActionEvent event) {
		String text = "";
		if (clientInput.getText() != null) {
			text = clientInput.getText();
			String data = stringProcess("send", text);
			send(data);
			clientInput.setText("");
		}
	}


	public void handleClientBtnAction(ActionEvent event) {
		if (connBtn.getText().equals("connect")) {
			startClient();
		} else if (connBtn.getText().equals("disconnect")) {
			stopClient();
		}
	}

	public void handleComboChange(ActionEvent event) {
		opponentUserID=uidComboBox.getValue().toString();
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
						
						//���ǰ� ����
						setSessionID(odao.DAO_select_UserId(odb.getConnection(),userIdInput.getText()));
						
						
						System.out.println("���� Ŭ���̾�Ʈ�� ���� �� : "+session_id);
						
						Platform.runLater(() -> {
							displayText("[���� �Ϸ�: " + socket.getRemoteSocketAddress() + "]");
							connBtn.setText("disconnect");
							sendBtn.setDisable(false);
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
				String[] strArr = data.split("//");
				
				for(int i=0;i<strArr.length;i++) {
					System.out.println(strArr[i]);
				}
				
				if (strArr[0].equals("connList")) {
					comboBoxUpdate(data);
				} 
				else if(strArr[0].equals("event")) {//�޽����� �����ߴµ�
					eventBtnUpdate(strArr);
					if(strArr[3]!=null) {
						Platform.runLater(() -> displayText("[�ޱ� �Ϸ�] " +strArr[3]));
						save_last_sentence=strArr[3];
					}
				}
				else {//������ ť���� ����� �޽����� ������ �� 
					Platform.runLater(() -> displayText("[�ޱ� �Ϸ�] " +strArr[3]));
					save_last_sentence=strArr[3];
				}
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

	// ���� �޽��� ��ó�� ����
	String stringProcess(String cmd, String msg) {
		String res = new String();
		switch (cmd) {
		case "id":				//Ŭ���̾�Ʈ�� �������� �� �������� ����
			res = "id//";
			break;
			
		case "send":			//Ŭ���̾�Ʈ�� �������� �޽����� ������
			res = "send//";
			res+=Integer.toString(session_id)+"//";
			res += opponentUserID + "//";
			break;
		case "receive":			//Ŭ���̾�Ʈ�� �������� �޽��� �޶�� ��û
			res = "receive//";
			break;
		}
		return res + msg;
	}
	//��ư Ȱ��ȭ  & ��Ȱ��ȭ ��Ÿ����
	void eventBtnUpdate(String[] data) {
		if(data[1].equals("saveBtn")) {
			if(data[2].equals("false")) {
				saveBtn.setDisable(false);
			}else {
				saveBtn.setDisable(true);
			}
		}else if(data[1].equals("receiveBtn")) {
			if(data[2].equals("false")) {
				receiveBtn.setDisable(false);
			}else {
				receiveBtn.setDisable(true);
			}
		}
	}
	
	// �޺��ڽ� ������Ʈ�ϴ� �޼ҵ�
	void comboBoxUpdate(String data) {

		userIDs = new HashSet<String>();
		uidComboBox.setItems(comboBoxList); // ��ο���
		String[] item = data.split("//");
		for (int i = 1; i < item.length; i++) {
			userIDs.add(item[i]);
		}
		Iterator<String> iter = userIDs.iterator();

		comboBoxRemove();
		uidComboBox.getItems().add(opponentUserID);
		while (iter.hasNext()) {
			String tmp = iter.next();
			uidComboBox.getItems().add(tmp);
		}

	}

	// �޺��ڽ� ���� ����� �޼ҵ�
	void comboBoxRemove() {
		uidComboBox.getItems().clear();
	}
	int getSessionID() {
		return session_id;
	}
	void setSessionID(int session_id) {
		this.session_id=session_id;
	}
}
