package edu.uclm.esi.common.server.sockets;

import edu.uclm.esi.common.server.domain.User;
import edu.uclm.esi.common.server.websockets.Client;
import edu.uclm.esi.common.server.websockets.WSServer;

public class GWTSender extends Sender {

	@Override
	void send(User user, String msg) {
		Client cliente=WSServer.findClientByEmail(user.getEmail());
		if (cliente!=null)
			cliente.send(msg);
	}
}
