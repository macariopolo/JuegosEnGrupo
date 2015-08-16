package edu.uclm.esi.common.server.websockets;

import java.io.IOException;

import javax.websocket.Session;

import edu.uclm.esi.common.server.domain.Manager;
import edu.uclm.esi.common.server.domain.User;

public class Client {
	private Session session;
	private User user;
	
	public Client(Session session) {
		super();
		this.session = session;
		this.user = null;
	}

	public void setUser(User user) {
		this.user=user;
	}

	public User getUser() {
		return user;
	}
	
	public Session getSession() {
		return session;
	}

	public void send(final String msg) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					session.getAsyncRemote().sendText(msg);
				}
				catch (Exception e) {
					try {
						Thread.sleep(1000);
						session.getAsyncRemote().sendText(msg);
					}
					catch(Exception e2) {
						try {
							Manager.get().remove(user);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		}).start();
	}
}
