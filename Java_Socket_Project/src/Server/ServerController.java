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

	// ������ �������� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
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

		// Ŭ���̾�Ʈ�� ������ ������ ��� ��ٸ��� ������
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					serverBtn.setText("stop");
					serverLogText("[���� ����]");
				});
				while (true) {
					try {
						// Ŭ���̾�Ʈ ������ ��ٸ����ִ�.
						Socket socket = serverSocket.accept();
						String message = "[���� ����: " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName() + "]";
						Platform.runLater(() -> serverLogText(message));

						Client client = new Client(socket);
						connections.add(client);
						Platform.runLater(() -> serverLogText("[���� ����: " + connections.size() + "]"));
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

	// ������ �۵��� ������Ű�� �޼ҵ�
	void stopServer() {
		try {
			// ���� �۵� ���� ��� ���� �ݱ�
			Iterator<Client> iterator = connections.iterator();
			while (iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// ���� ���� ��ü �ݱ�
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			// ������ Ǯ �����ϱ�
			if (executorService != null && !executorService.isShutdown()) {
				executorService.shutdown();
			}
			Platform.runLater(() -> {
				serverLogText("[���� ����]");
				serverBtn.setText("start");
			});
		} catch (Exception e) {
		}
	}

	class Client {
		Socket socket; // ������ ����� ����
		String userID;
		Queue<String> db = new LinkedList<String>();

		Client(Socket socket) {
			this.socket = socket; // ������ ����� ���� ����
			receive(); // �޽��� �޴´�.
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

							// Ŭ���̾�Ʈ�� ������ ���Ḧ ���� ��� IOException �߻�
							int readByteCount = inputStream.read(byteArr);

							// Ŭ���̾�Ʈ�� ���������� Socket�� close()�� ȣ������ ���
							if (readByteCount == -1) {
								throw new IOException();
							}

							String message = "[��û ó��: " + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName() + "]";
							Platform.runLater(() -> serverLogText(message));

							String data = new String(byteArr, 0, readByteCount, "UTF-8");

							String[] strArr = data.split("//");
							if (strArr[0].equals("id")) {
								setUserID(strArr[1]);
							}

							// Ŭ���̾�Ʈ���� ���޹��� �޽��� ó��
							receiveMessageProcess(data);

						}
					} catch (Exception e) {
						try {

							hm.remove(Client.this.userID);
							connections.remove(Client.this);

							Platform.runLater(() -> connectedListText());
							String message = "[2.Ŭ���̾�Ʈ ��� �ȵ�: " + socket.getRemoteSocketAddress() + ": "
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
							String message = "[3.Ŭ���̾�Ʈ ��� �ȵ�: " + socket.getRemoteSocketAddress() + ": "
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
			// �����ڸ���Ʈ ����
			case "id":
				hm.put(strArr[1], socket.getOutputStream());
				Platform.runLater(() -> serverLogText(strArr[1] + "���� �����ϼ̽��ϴ�."));
				Platform.runLater(() -> connectedListText());
				for (Client client : connections) {
					client.send(strArr[1] + "���� �����ϼ̽��ϴ�.");
				}

				break;
			// Ŭ���̾�Ʈ�� �޽��� ����
			case "send":
				if (strArr[1].equals("��ο���")) {
					for (Client client : connections)
						client.db.add(strArr[2]);
				} else {
					for (Client client : connections) {
						if (strArr[1].equals(client.getUserID()))
							client.db.add(strArr[2]);
					}
				}
				break;
			// Ŭ���̾�Ʈ �޽��� �޴´�.
			case "receive":

				// �ڽ��� ���� �޽����� �����Ѵ�.
				if (db.isEmpty())
					send("���޹��� �޽����� �����ϴ�.");
				else {
					String res = db.poll();
					send(res);
				}

				break;
			}
		}

	}

	// ���� �α�â ��� �޼ҵ�
	void serverLogText(String text) {
		serverLog.appendText(text + "\n");
	}

	// ���� ������ ��� �޼ҵ�
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
