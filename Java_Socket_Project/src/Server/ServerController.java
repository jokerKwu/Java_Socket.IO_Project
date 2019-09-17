package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ServerController implements Initializable {
	@FXML
	private TextArea serverLog;
	@FXML
	private Button serverBtn;
	@FXML
	private TextArea connectionList;

	ExecutorService executorService;
	ServerSocket serverSocket;
	List<Client> connections = new Vector<Client>();
	HashMap<String, OutputStream> hm = new HashMap<String, OutputStream>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		serverBtn.setOnAction(event -> handleServerBtnAction(event));

	}

	public void handleServerBtnAction(ActionEvent event) {
		if (serverBtn.getText().equals("start")) {
			startServer();
		} else if (serverBtn.getText().equals("stop")) {
			stopServer();
		}
	}

	// 서버를 구동시켜 클라이언트의 연결을 기다리는 메소드
	void startServer() {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost", 5001));
		} catch (Exception e) {
			if (!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}

		// 클라이언트가 접속할 때까지 계속 기다리는 쓰레드
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					serverBtn.setText("stop");
					serverLogText("[서버 시작]");
				});
				while (true) {
					try {
						// 클라이언트 연결을 기다리고있다.
						Socket socket = serverSocket.accept();
						String message = "[연결 수락: " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName() + "]";
						Platform.runLater(() -> serverLogText(message));

						Client client = new Client(socket);
						connections.add(client);
						Platform.runLater(() -> serverLogText("[연결 개수: " + connections.size() + "]"));
					} catch (Exception e) {
						if (!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}

		};
		executorService.submit(runnable);
	}

	// 서버의 작동을 중지시키는 메소드
	void stopServer() {
		try {
			// 현재 작동 중인 모든 소켓 닫기
			Iterator<Client> iterator = connections.iterator();
			while (iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// 서버 소켓 객체 닫기
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			// 쓰레드 풀 종료하기
			if (executorService != null && !executorService.isShutdown()) {
				executorService.shutdown();
			}
			Platform.runLater(() -> {
				serverLogText("[서버 멈춤]");
				serverBtn.setText("start");
			});
		} catch (Exception e) {
		}
	}

	class Client {
		Socket socket; // 서버와 통신할 소켓
		String userID;
		Queue<String> db = new LinkedList<String>();

		Client(Socket socket) {
			this.socket = socket; // 서버와 통신할 소켓 저장
			receive(); // 메시지 받는다.
			db.clear();
		}

		void receive() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							byte[] byteArr = new byte[100];
							InputStream inputStream = socket.getInputStream();

							// 클라이언트가 비저상 종료를 했을 경우 IOException 발생
							int readByteCount = inputStream.read(byteArr);

							// 클라이언트가 정상적으로 Socket의 close()를 호출했을 경우
							if (readByteCount == -1) {
								throw new IOException();
							}

							String message = "[요청 처리: " + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName() + "]";
							Platform.runLater(() -> serverLogText(message));

							String data = new String(byteArr, 0, readByteCount, "UTF-8");

							String[] strArr = data.split("//");
							if (strArr[0].equals("id")) {
								setUserID(strArr[1]);
							}

							// 클라이언트한테 전달받은 메시지 처리
							receiveMessageProcess(data);

						}
					} catch (Exception e) {
						try {

							hm.remove(Client.this.userID);
							connections.remove(Client.this);

							Platform.runLater(() -> connectedListText());
							String message = "[2.클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName() + "]";
							Platform.runLater(() -> serverLogText(message));



							socket.close();
						} catch (IOException e2) {
						}
					}
				}
			};
			executorService.submit(runnable);
		}

		void send(String data) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						byte[] byteArr = data.getBytes("UTF-8");
						OutputStream outputStream = socket.getOutputStream();
						outputStream.write(byteArr);
						outputStream.flush();
					} catch (Exception e) {
						try {
							String message = "[3.클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName() + "]";
							Platform.runLater(() -> serverLogText(message));
							connections.remove(Client.this);
							socket.close();
						} catch (IOException e2) {
						}
					}
				}
			};
			executorService.submit(runnable);
		}

		void setUserID(String userID) {
			this.userID = userID;
		}

		String getUserID() {
			return this.userID;
		}

		void receiveMessageProcess(String data) throws IOException {
			String[] strArr = data.split("//");
			switch (strArr[0]) {
			// 접속자리스트 생성
			case "id":
				hm.put(strArr[1], socket.getOutputStream());
				Platform.runLater(() -> serverLogText(strArr[1] + "님이 접속하셨습니다."));
				Platform.runLater(() -> connectedListText());
				for (Client client : connections) {
					client.send(strArr[1] + "님이 접속하셨습니다.");
				}

				break;
			// 클라이언트가 메시지 전달
			case "send":
				if (strArr[1].equals("모두에게")) {
					for (Client client : connections)
						client.db.add(strArr[2]);
				} else {
					for (Client client : connections) {
						if (strArr[1].equals(client.getUserID()))
							client.db.add(strArr[2]);
					}
				}
				break;
			// 클라이언트 메시지 받는다.
			case "receive":

				// 자신이 받은 메시지를 전송한다.
				if (db.isEmpty())
					send("전달받은 메시지가 없습니다.");
				else {
					String res = db.poll();
					send(res);
				}

				break;
			}
		}

	}

	// 서버 로그창 기록 메소드
	void serverLogText(String text) {
		serverLog.appendText(text + "\n");
	}

	// 서버 접속자 기록 메소드
	void connectedListText() {
		connectionList.clear();
		Iterator<String> keys = hm.keySet().iterator();
		while (keys.hasNext()) {
			String userID = keys.next();
			connectionList.appendText(userID + "\n");
		}
	}


	String getConnectedList() {
		String res = "connList//";
		Iterator<String> keys = hm.keySet().iterator();
		while (keys.hasNext()) {
			String userID = keys.next();
			res += userID + "//";
		}
		return res;
	}
}
