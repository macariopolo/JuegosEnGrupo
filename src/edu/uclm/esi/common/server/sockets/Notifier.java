package edu.uclm.esi.common.server.sockets;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONObject;

import edu.uclm.esi.common.jsonMessages.JSONMessage;
import edu.uclm.esi.common.jsonMessages.JSONMessagesBuilder;
import edu.uclm.esi.common.server.domain.User;

public class Notifier extends Thread {
	private static Notifier yo;
	
	private int port=3000;
	private ServerSocket server;

	private boolean started;
	
	private Notifier() throws IOException {
		server=new ServerSocket(port);
	}
	
	public static Notifier get() throws IOException {
		if (yo==null)
			yo=new Notifier();
		return yo;
	}
	
	public void run() {
		this.started=true;
		System.out.println("Notifier started");
		while (true) {
			try {
				Socket client=server.accept();
				DataInputStream in = new DataInputStream(client.getInputStream());
				String msg=in.readUTF();
				JSONObject jso = new JSONObject(msg);
				JSONMessage jsm=JSONMessagesBuilder.build(jso);
				//proceedWith(client.getRemoteSocketAddress(), jsm);
				System.out.println(msg);
				client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void post(User user, JSONMessage jsm) {
		String msg=jsm.toString();
		Sender sender=Sender.build(user.getUserType());
		sender.send(user, msg);
	}

	public void post(Vector<User> users, JSONMessage jsm) {
		String msg=jsm.toString();
		for (User user : users) {
			Sender sender=Sender.build(user.getUserType());
			sender.send(user, msg);
		}
	}

	public void startListening() {
		if (!started)
			this.start();
	}

	public void post(Hashtable<String, User> users, final JSONMessage jsm) {
		String msg=jsm.toString();
		Enumeration<User> e=users.elements();
		while (e.hasMoreElements()) {
			User user=e.nextElement();
			Sender sender=Sender.build(user.getUserType());
			if (sender!=null)
				sender.send(user, msg);
		}
	}
}
