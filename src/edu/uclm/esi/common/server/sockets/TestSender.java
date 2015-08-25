package edu.uclm.esi.common.server.sockets;

import tests.UserTest;
import edu.uclm.esi.common.server.domain.User;

public class TestSender extends Sender {

	@Override
	void send(User user, String msg) {
		UserTest tu=(UserTest) user;
		tu.receive(msg);
	}

}
