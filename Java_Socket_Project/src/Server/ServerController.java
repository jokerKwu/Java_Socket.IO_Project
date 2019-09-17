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
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import  javafx.scene.control.Button;
import  javafx.scene.control.TextArea;

public class ServerController implements Initializable{
	@FXML private TextArea serverLog;
	@FXML private Button serverBtn;
	@FXML private TextArea connectionList;
	
	ExecutorService executorService;
	ServerSocket serverSocket;
	List<Client> connections = new Vector<Client>();
	HashMap<String,OutputStream> hm=new HashMap<String,OutputStream>();
	
	
	@Override
	public void initialize(URL location,ResourceBundle resources) {
		
		serverBtn.setOnAction(event->handleServerBtnAction(event));
		
		
	}
	
	public void handleServerBtnAction(ActionEvent event) {
		if(serverBtn.getText().equals("start")) {
			startServer();
		}else if(serverBtn.getText().equals("stop")) {
			stopServer();
		}
	}
	
	//������ �������� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
	void startServer() {
		executorService = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors()
	    );
		
		try {
			serverSocket = new ServerSocket();		
			serverSocket.bind(new InetSocketAddress("localhost", 5001));
		} catch(Exception e) {
			if(!serverSocket.isClosed()) { stopServer(); }
			return;
		}
		
		// Ŭ���̾�Ʈ�� ������ ������ ��� ��ٸ��� ������
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Platform.runLater(()->{
					serverBtn.setText("stop");
					serverLogText("[���� ����]");
				});		
				while(true) {
					try {
						//Ŭ���̾�Ʈ ������ ��ٸ����ִ�.
						Socket socket = serverSocket.accept();
						String message = "[���� ����: " + socket.getRemoteSocketAddress()  + ": " + Thread.currentThread().getName() + "]";
						Platform.runLater(()->serverLogText(message));
					
						Client client = new Client(socket);
						connections.add(client);
						Platform.runLater(()->serverLogText("[���� ����: " + connections.size() + "]"));
					} catch (Exception e) {
						if(!serverSocket.isClosed()) { stopServer(); }
						break;
					}
				}
			}
 
		};
		executorService.submit(runnable);
	}
	
	//������ �۵��� ������Ű�� �޼ҵ�
	void stopServer() {
		try {
			//���� �۵� ���� ��� ���� �ݱ�
			Iterator<Client> iterator = connections.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// ���� ���� ��ü �ݱ�
			if(serverSocket!=null && !serverSocket.isClosed()) { 
				serverSocket.close(); 
			}
			// ������ Ǯ �����ϱ�
			if(executorService!=null && !executorService.isShutdown()) { 
				executorService.shutdown(); 
			}
			Platform.runLater(()->{
				serverLogText("[���� ����]");
				serverBtn.setText("start");
			});
		} catch (Exception e) { }
	}	
	
	class Client {
		Socket socket;	//������ ����� ����
		String userID;
		Client(Socket socket) {
			this.socket = socket; //������ ����� ���� ����
			send("�׽�Ʈ2");
			receive();			//�޽��� �޴´�.
			
		}
		
		void receive() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while(true) {
							byte[] byteArr = new byte[100];
							InputStream inputStream = socket.getInputStream();
							
							//Ŭ���̾�Ʈ�� ������ ���Ḧ ���� ��� IOException �߻�
							int readByteCount = inputStream.read(byteArr);
							
							//Ŭ���̾�Ʈ�� ���������� Socket�� close()�� ȣ������ ���
							if(readByteCount == -1) {  throw new IOException(); }
							
							String message = "[��û ó��: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
							Platform.runLater(()->serverLogText(message));
							
							String data = new String(byteArr, 0, readByteCount, "UTF-8");
							userID=data;
							
							//���� ����� ���ؼ� ����
							hm.put(data, socket.getOutputStream());
							
							Platform.runLater(()->serverLogText(data+"���� �����ϼ̽��ϴ�."));
							Platform.runLater(()->connectedListText(hm));
							for(Client client : connections) {
								client.send(data+"���� �����ϼ̽��ϴ�."); 
							}
						}
					} catch(Exception e) {
						try {
							hm.remove(Client.this.userID);
							connections.remove(Client.this);
							String message = "[Ŭ���̾�Ʈ ��� �ȵ�: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
							Platform.runLater(()->serverLogText(message));
							Platform.runLater(()->connectedListText(hm));
							
							socket.close();
						} catch (IOException e2) {}
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
					} catch(Exception e) {
						try {
							String message = "[Ŭ���̾�Ʈ ��� �ȵ�: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";
							Platform.runLater(()->serverLogText(message));
							connections.remove(Client.this);
							socket.close();
						} catch (IOException e2) {}
					}
				}
			};
			executorService.submit(runnable);
		}

	}
	
	// ���� �α�â ��� �޼ҵ�
	void serverLogText(String text) {
		serverLog.appendText(text + "\n");
	}	
	
	// ���� ������ ��� �޼ҵ�
	void connectedListText(HashMap<String,OutputStream> cl) {
		connectionList.clear();
		Iterator<String> keys = hm.keySet().iterator();
		while( keys.hasNext() ) {
			String userID = keys.next();
			connectionList.appendText(userID + "\n");
		}
	}
}
