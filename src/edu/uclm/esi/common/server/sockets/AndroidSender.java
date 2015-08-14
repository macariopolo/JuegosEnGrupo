package edu.uclm.esi.common.server.sockets;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import edu.uclm.esi.common.server.domain.User;

public class AndroidSender extends Sender {

	@Override
	void send(final User user, final String msg) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Socket socket=new Socket(user.getIp(), 4000);
					OutputStream outToServer = socket.getOutputStream();
					DataOutputStream out = new DataOutputStream(outToServer);
					out.writeUTF(msg);
					out.flush();
					socket.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

}
