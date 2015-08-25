package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.maco.tresenraya.jsonMessages.TresEnRayaBoardMessage;

import edu.uclm.esi.common.jsonMessages.LoginMessage;
import edu.uclm.esi.common.server.actions.Login;
import edu.uclm.esi.common.server.domain.Manager;
import edu.uclm.esi.common.server.domain.User;

public class TestTER {
	UserTest a, b;
	int idA, idB;
	Connection bdA, bdB;
	Manager manager;
	
	@Before
	public void setUp() {
		try {
			manager=Manager.get();
			manager.findAllGames();
			
			bdA=User.identify("a@a.com", "a");
			a=new UserTest(bdA, "a@a.com");
			idA=a.getId();
			manager.add(a, "127.0.0.1");
			
			bdB=User.identify("a@a.coma", "a");
			b=new UserTest(bdA, "a@a.coma");
			idB=b.getId();
			manager.add(b, "127.0.0.1");
		} catch (Exception e) {
			fail("Se produjo " + e.toString());
		}
	}

	@Test
	public void test1() {
		try {
			manager.add(1, idA);
			a.showLastMessage();
			manager.add(1, idB);
			b.showLastMessage();
			a.showLastMessage();
			UserTest userWithTurn, theOther;
			JSONObject jso=new JSONObject(a.getLastMessage());
			TresEnRayaBoardMessage tbm=new TresEnRayaBoardMessage(jso);
			if (tbm.getUserWithTurn().equals(a.getEmail())) {
				userWithTurn=a;
				theOther=b;
			} else {
				userWithTurn=b;
				theOther=a;
			}
		} catch (Exception e) {
			fail("Se produjo " + e.toString());
		}
	}

}
