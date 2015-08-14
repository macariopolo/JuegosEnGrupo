package edu.uclm.esi.common.server.sockets;

import edu.uclm.esi.common.jsonMessages.JSONMessage;
import edu.uclm.esi.common.server.domain.User;

public abstract class Sender {

	public static Sender build(String userType) {
		if (userType.equals(JSONMessage.USER_ANDROID))
			return new AndroidSender();
		else if (userType.equals(JSONMessage.USER_GWT))
			return new GWTSender();
		else if (userType.equals(JSONMessage.USER_WEB))
			return new GWTSender();
		return null;
	}

	abstract void send(User user, String msg);
}
