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
	private Button lastBtn; // 마지막 대화 가져오기

	private ObservableList<String> comboBoxList = FXCollections.observableArrayList("모두에게");

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

		opponentUserID = "모두에게";

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
		System.out.println("마지막 문장 가져오기");
		String lastSentence = null;
		lastSentence = odao.DAO_select_lastReply(odb.getConnection(), session_id); // 해당 유저에 마지막 대화 가져오기
		if (lastSentence == null) {
			Platform.runLater(() -> displayText("[마지막 문장 가져오기 실패] 대화 목록이 없습니다."));
		} else {
			Platform.runLater(() -> displayText("[마지막 문장 가져오기 성공] "));
			clientInput.setText(lastSentence);
		}
	}

	public void handleClientMessageSaveAction(ActionEvent event) throws SQLException {
		System.out.println("메시지 저장 테스트");
		saveBtn.setDisable(true);
		odao.DAO_insert_lastReceiveSaveConversation(odb.getConnection(), session_id);
		Platform.runLater(() -> displayText("[마지막 문장 가져오기 성공] "));

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

	// 채팅 메시지 보내기
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

	// 클라이언트 프로그램의 작동을 시작하는 메소드
	void startClient() {

		Thread thread = new Thread() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					socket = new Socket();

					String userId;

					// userId = stringProcess("id", userIdInput.getText());
					// 서버 연결 시도 서버 연결 성공
					if (userIdInput.getText().length() != 0 && !userIdInput.getText().equals("아이디 입력")) {
						socket.connect(new InetSocketAddress("localhost", 5001)); // 서버와 연결을 하고

						// 세션값을 저장하기 위해서 db에서 셀렉트
						setSessionID(odao.DAO_select_UserId(odb.getConnection(), userIdInput.getText()));

						System.out.println("현재 클라이언트에 세션 값 : " + session_id);

						Platform.runLater(() -> {
							displayText("[연결 완료: " + socket.getRemoteSocketAddress() + "]");
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

					// 서버 연결 실패
					else {
						Platform.runLater(() -> displayText("[연결 실패] 아이디 입력해주세요."));
					}
				} catch (Exception e) {
					Platform.runLater(() -> displayText("[서버 통신 안됨]"));
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

	// 클라이언트 프로그램의 작동을 종료하는 메소드
	void stopClient() {
		try {
			Platform.runLater(() -> {
				displayText("[연결 끊음]");
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

	// 서버로부터 메시지를 전달받는 메소드
	// JSON 방식으로 데이터를 받기
	void receive() {
		while (true) {
			try {

				System.out.println("클라이언트가 데이터를 받았습니다.!!");

				InputStream is = socket.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				JSONObject json = (JSONObject) ois.readObject();
				typeProcess(json);

			} catch (Exception e) {
				Platform.runLater(() -> displayText("[서버 통신 안됨]"));
				stopClient();
				break;
			}
		}

	}

	// 서버로 메시지를 전송하는 메소드
	// JSON 방식으로 보내기

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
					Platform.runLater(() -> displayText("[보내기 완료]"));

				} catch (Exception e) {
					Platform.runLater(() -> displayText("[서버 통신 안됨]"));
					stopClient();
				}
			}
		};
		thread.start();
	}

	void typeProcess(JSONObject json) {
		String typeData = json.get("type").toString();
		System.out.println(typeData+"어서오게");
		if (typeData.equals("connList")) {
			String contentData = json.get("content").toString();
			comboBoxUpdate(contentData);
		} else if (typeData.equals("event")) {
			String[] dataArr = json.get("content").toString().split("//");
			eventBtnUpdate(dataArr);
			if (dataArr[2] != null) {
				Platform.runLater(() -> displayText("[받기 완료]" + dataArr[2]));
				save_last_sentence = dataArr[2];
			}
		} else if (typeData.equals("serverMsg")) {
			Platform.runLater(() -> displayText("[받기 완료]" + json.get("content").toString()));
		} else {
			Platform.runLater(() -> displayText("[받기 완료]"));

		}
	}

	// 클라이언트 로그창에 메시지 기록하는 메소드
	void displayText(String text) {
		clientLog.appendText(text + "\n");
	}

	// 버튼 활성화 & 비활성화 나타내기
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

	// 콤보박스 업데이트하는 메소드
	void comboBoxUpdate(String data) {

		userIDs = new HashSet<String>();
		uidComboBox.setItems(comboBoxList); // 모두에게
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

	// 콤보박스 전부 지우는 메소드
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
