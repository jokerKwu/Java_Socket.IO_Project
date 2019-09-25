package Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import org.json.simple.JSONObject;

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
	private Button lastBtn; // ������ ��ȭ ��������

	private ObservableList<String> comboBoxList = FXCollections.observableArrayList("��ο���");

	private String opponentUserID;

	Socket socket;

	private Set<String> userIDs;
	private DAOFactory daoFactory = null;
	private int session_id;
	private String save_last_sentence = null;

	private OracleDBConnection odb;
	private OracleDAO odao;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		opponentUserID = "��ο���";

		uidComboBox.setItems(comboBoxList);

		connBtn.setOnAction(event -> handleClientBtnAction(event));

		sendBtn.setOnAction(event -> handleClientMessageSendAction(event));

		receiveBtn.setOnAction(event -> handleClientMessageReceiveAction(event));

		saveBtn.setOnAction(event -> {
			try {
				handleClientMessageSaveAction(event);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		lastBtn.setOnAction(event -> {
			try {
				handleClientMessageLastAction(event);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		odb = OracleDBConnection.getInstance();
		odao = OracleDAO.getInstance();

	}

	public void handleClientMessageLastAction(ActionEvent event) throws SQLException {
		System.out.println("������ ���� ��������");
		String lastSentence = null;
		lastSentence = odao.DAO_select_lastReply(odb.getConnection(), session_id); // �ش� ������ ������ ��ȭ ��������
		if (lastSentence == null) {
			Platform.runLater(() -> displayText("[������ ���� �������� ����] ��ȭ ����� �����ϴ�."));
		} else {
			Platform.runLater(() -> displayText("[������ ���� �������� ����] "));
			clientInput.setText(lastSentence);
		}
	}

	public void handleClientMessageSaveAction(ActionEvent event) throws SQLException {
		System.out.println("�޽��� ���� �׽�Ʈ");
		saveBtn.setDisable(true);
		odao.DAO_insert_lastReceiveSaveConversation(odb.getConnection(), session_id);
		Platform.runLater(() -> displayText("[������ ���� �������� ����] "));

	}

	@SuppressWarnings("unchecked")
	public void handleClientMessageReceiveAction(ActionEvent event) {
		String data = "";
		saveBtn.setDisable(false);
		JSONObject json = new JSONObject();
		json.put("type", "receive");
		json.put("content", session_id);
		send(json);

	}

	// ä�� �޽��� ������
	@SuppressWarnings("unchecked")
	public void handleClientMessageSendAction(ActionEvent event) {

		String text = "";
		JSONObject json = new JSONObject();
		json.put("type", "send");

		if (clientInput.getText() != null) {
			text += Integer.toString(session_id) + "//";
			text += opponentUserID + "//";
			text += clientInput.getText();
			json.put("content", text);
			send(json);
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
		opponentUserID = uidComboBox.getValue().toString();
	}

	// Ŭ���̾�Ʈ ���α׷��� �۵��� �����ϴ� �޼ҵ�
	void startClient() {

		Thread thread = new Thread() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					socket = new Socket();

					String userId;

					// userId = stringProcess("id", userIdInput.getText());
					// ���� ���� �õ� ���� ���� ����
					if (userIdInput.getText().length() != 0 && !userIdInput.getText().equals("���̵� �Է�")) {
						socket.connect(new InetSocketAddress("localhost", 5001)); // ������ ������ �ϰ�

						// ���ǰ��� �����ϱ� ���ؼ� db���� ����Ʈ
						setSessionID(odao.DAO_select_UserId(odb.getConnection(), userIdInput.getText()));

						System.out.println("���� Ŭ���̾�Ʈ�� ���� �� : " + session_id);

						Platform.runLater(() -> {
							displayText("[���� �Ϸ�: " + socket.getRemoteSocketAddress() + "]");
							connBtn.setText("disconnect");
							sendBtn.setDisable(false);
							clientInput.setDisable(false);
						});

						JSONObject json = new JSONObject();
						json.put("type", "join");
						json.put("content", userIdInput.getText());
						System.out.println(json.get("type"));
						System.out.println(json.get("content"));
						System.out.println(json.toString());
						send(json);
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
	// JSON ������� �����͸� �ޱ�
	void receive() {
		while (true) {
			try {

				System.out.println("Ŭ���̾�Ʈ�� �����͸� �޾ҽ��ϴ�.!!");

				InputStream is = socket.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				JSONObject json = (JSONObject) ois.readObject();
				typeProcess(json);

			} catch (Exception e) {
				Platform.runLater(() -> displayText("[���� ��� �ȵ�]"));
				stopClient();
				break;
			}
		}

	}

	// ������ �޽����� �����ϴ� �޼ҵ�
	// JSON ������� ������

	void send(JSONObject data) {
		Thread thread = new Thread() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {

					OutputStream outputStream = socket.getOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(outputStream);
					oos.writeObject(data);
					oos.flush();
					Platform.runLater(() -> displayText("[������ �Ϸ�]"));

				} catch (Exception e) {
					Platform.runLater(() -> displayText("[���� ��� �ȵ�]"));
					stopClient();
				}
			}
		};
		thread.start();
	}

	void typeProcess(JSONObject json) {
		String typeData = json.get("type").toString();
		System.out.println(typeData+"�����");
		if (typeData.equals("connList")) {
			String contentData = json.get("content").toString();
			comboBoxUpdate(contentData);
		} else if (typeData.equals("event")) {
			String[] dataArr = json.get("content").toString().split("//");
			eventBtnUpdate(dataArr);
			if (dataArr[2] != null) {
				Platform.runLater(() -> displayText("[�ޱ� �Ϸ�]" + dataArr[2]));
				save_last_sentence = dataArr[2];
			}
		} else if (typeData.equals("serverMsg")) {
			Platform.runLater(() -> displayText("[�ޱ� �Ϸ�]" + json.get("content").toString()));
		} else {
			Platform.runLater(() -> displayText("[�ޱ� �Ϸ�]"));

		}
	}

	// Ŭ���̾�Ʈ �α�â�� �޽��� ����ϴ� �޼ҵ�
	void displayText(String text) {
		clientLog.appendText(text + "\n");
	}

	// ��ư Ȱ��ȭ & ��Ȱ��ȭ ��Ÿ����
	void eventBtnUpdate(String[] data) {
		if (data[0].equals("saveBtn")) {
			if (data[1].equals("false")) {
				saveBtn.setDisable(false);
			} else {
				saveBtn.setDisable(true);
			}
		} else if (data[0].equals("receiveBtn")) {
			if (data[1].equals("false")) {
				receiveBtn.setDisable(false);
			} else {
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
		this.session_id = session_id;
	}
}
