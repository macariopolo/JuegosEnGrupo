package tests;

import java.sql.Connection;
import java.sql.SQLException;

import edu.uclm.esi.common.server.domain.User;

public class UserTest extends User {

	private String lastMessage;

	public UserTest(Connection bdA, String email) throws SQLException {
		super(bdA, email, "USER_TEST");
	}

	public void receive(String msg) {
		this.lastMessage=msg;
	}

	public String getLastMessage() {
		return lastMessage;
	}
	
	public void showLastMessage() {
		System.out.println(this.getEmail() + ">> " + this.lastMessage);
	}
}
