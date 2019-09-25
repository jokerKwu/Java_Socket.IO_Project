package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONObject;

import OracleDAO.OracleDAO;
import OracleDAO.OracleDBConnection;
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
	Set<String> ids = new HashSet<String>();
	
	private OracleDBConnection odb;
	private OracleDAO odao;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		serverBtn.setOnAction(event -> handleServerBtnAction(event));
		
		odb=OracleDBConnection.getInstance();
		odao=OracleDAO.getInstance();

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
		executorService = Executors.newFixedThreadPool(10);

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
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				Platform.runLater(() -> {
					serverBtn.setText("stop");
					serverLogText("[���� ����]");
				});
				while (true) {
					try {
						// Ŭ���̾�Ʈ ������ ��ٸ����ִ�. ���� ����
						Socket socket = serverSocket.accept();
						String message = "[���� ����: " + socket.getRemoteSocketAddress() + ": "
								+ Thread.currentThread().getName() + "]";
						Platform.runLater(() -> serverLogText(message));
						Client client = new Client(socket);
						
						connections.add(client);
						
						//���� ����Ʈ �ѷ���ߵ�
						JSONObject sendJson=new JSONObject();
						sendJson.put("type", "connList");
						sendJson.put("content", getConnectedList());
						System.out.println(getConnectedList());
						for(Client c:connections) {
							c.send(sendJson);
						}
						
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
		String userName;
		Queue<String> db = new LinkedList<String>();
		
		
		Client(Socket socket) {
			this.socket = socket; // ������ ����� ���� ����
			receive(); // �޽��� �޴´�.
			db.clear();

		}

		void receive() {
			Runnable runnable = new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					try {
						while (true) {
							InputStream is=socket.getInputStream();
							ObjectInputStream ois=new ObjectInputStream(is);
							JSONObject json=(JSONObject) ois.readObject();
							
							String typeCheck=null;
							typeProcess(json);
							
							// Ŭ���̾�Ʈ���� ���޹��� �޽��� ó��
							//receiveMessageProcess(json);
						
						}
					} catch (Exception e) {
						try {

							//Ŭ���̾�Ʈ�� ���������

							hm.remove(Client.this.getUserName());
							ids.remove(Client.this.getUserName());
							

							connections.remove(Client.this);

							Platform.runLater(() -> connectedListText());
							String message = "[2.Ŭ���̾�Ʈ ��� �ȵ�: " + socket.getRemoteSocketAddress() + ": "
									+ Thread.currentThread().getName() + "]";
							Platform.runLater(() -> serverLogText(message));
				
							//���� ����Ʈ �ѷ���ߵ�
							JSONObject sendJson=new JSONObject();
							sendJson.put("type", "connList");
							sendJson.put("content", getConnectedList());
							System.out.println(getConnectedList());
							for(Client c:connections) {
								c.send(sendJson);
							}

							socket.close();
						} catch (IOException e2) {
						}
					}
				}
			};
			executorService.submit(runnable);
		}

		void send(JSONObject data) {
			Runnable runnable = new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					try {
						OutputStream outputStream = socket.getOutputStream();
						ObjectOutputStream oos=new ObjectOutputStream(outputStream);
						System.out.println(data.toString());
						oos.writeObject(data);
						oos.flush();
						
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

		void setUserName(String userName) {
			this.userName = userName;
		}

		String getUserName() {
			return this.userName;
		}
		@SuppressWarnings("unchecked")
		void typeProcess(JSONObject json) throws IOException, SQLException {
			String typeCheck=json.get("type").toString();
			if(typeCheck.equals("join")) {
				String username=json.get("content").toString();
				setUserName(username);
				ids.add(username);
				Platform.runLater(() -> connectedListText());
			
				JSONObject sendJson=new JSONObject();
				sendJson.put("type","serverMsg");
				sendJson.put("content",username+"�� �����̽��ϴ�.");
				System.out.println(sendJson.toString());
				send(sendJson);
				
				
				//���� ����Ʈ �ѷ���ߵ�
				JSONObject sendJson2=new JSONObject();
				sendJson2.put("type", "connList");
				sendJson2.put("content", getConnectedList());
				System.out.println(getConnectedList());
				for(Client c:connections) {
					c.send(sendJson2);
				}
				
			}
			else if(typeCheck.equals("send")) {
				String[] dataArr=json.get("content").toString().split("//");
				for(int i=0;i<dataArr.length;i++) {
					System.out.println("�ε��� üũ : "+dataArr[i]);
				}
				if (dataArr[1].equals("��ο���")) {
					for (Client client : connections) {
						String oUsername=client.getUserName();
						int oSID=odao.DAO_select_UserId(odb.getConnection(), oUsername);
						
						//conn ���� , �������� , �޽���
						odao.DAO_insert_Reply(odb.getConnection(), Integer.parseInt(dataArr[0]), oSID, dataArr[2]);
						client.db.add(dataArr[2]);
						JSONObject sendJson=new JSONObject();
						sendJson.put("type", "event");
						sendJson.put("content", "receiveBtn//false//[ �޽����� �����Ͽ����ϴ�. ]");
						
						client.send(sendJson);
					}
				} else {
					for (Client client : connections) {
						if (dataArr[1].equals(client.getUserName())) {
							int oSID=odao.DAO_select_UserId(odb.getConnection(), dataArr[1]);
							odao.DAO_insert_Reply(odb.getConnection(), Integer.parseInt(dataArr[0]), oSID, dataArr[2]);
								
							client.db.add(dataArr[2]);
							
							//client.send("event//receiveBtn//false//[ �޽����� �����Ͽ����ϴ�. ]");
							
							JSONObject sendJson=new JSONObject();
							sendJson.put("type", "event");
							sendJson.put("content", "receiveBtn//false//[ �޽����� �����Ͽ����ϴ� ]");
							client.send(sendJson);
						}
					}
				}
			}else if(typeCheck.equals("receive")) {

				// �ڽ��� ���� �޽����� �����Ѵ�.
				if (db.isEmpty()) {
					JSONObject sendJson=new JSONObject();
					sendJson.put("type", "event");
					sendJson.put("content", "receiveBtn//true//���޹��� �޽����� �����ϴ�.");
					send(sendJson);
				}
				else {
					String res = db.poll();
					JSONObject sendJson=new JSONObject();
					sendJson.put("type", "event");
					sendJson.put("content", "receiveBtn//true//"+res);
					
					if(db.isEmpty()) send(sendJson);
					else send(sendJson);
				}
				
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
		Iterator<String> keys = ids.iterator();
		while (keys.hasNext()) {
			String userID = keys.next();
			connectionList.appendText(userID + "\n");
		}
	}
	//���� ������ ��������
		String getConnectedList() {
			String res = "connList//";
			Iterator<String> keys = ids.iterator();
			while (keys.hasNext()) {
				String userID = keys.next();
				res += userID + "//";
			}
			return res;
		}
}
