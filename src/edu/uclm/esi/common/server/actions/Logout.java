package edu.uclm.esi.common.server.actions;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;

import edu.uclm.esi.common.jsonMessages.ErrorMessage;
import edu.uclm.esi.common.jsonMessages.JSONMessage;
import edu.uclm.esi.common.jsonMessages.OKMessage;
import edu.uclm.esi.common.server.domain.Manager;
import edu.uclm.esi.common.server.domain.User;


@SuppressWarnings("serial")
public class Logout extends JSONAction {
	private int id;
	private String email;
	
	@Override
	public String postExecute() {
		try {
			Manager manager=Manager.get();
			User user=null;
			if (this.email!=null)
				user=manager.findUserByEmail(email);
			else
				user=manager.findUserById(id);
			if (user!=null) {
				user.getDB().close();
				manager.remove(user);
			}
			ServletActionContext.getRequest().getSession().removeAttribute("user");
			return SUCCESS;
		} catch (Exception e) {
			ActionContext.getContext().getSession().put("exception", e);
			this.exception=e;
			return ERROR;
		}
	}

	public String getResultado() {
		JSONMessage jso;
		if (this.exception!=null)
			jso=new ErrorMessage(this.exception.getMessage());
		else
			jso=new OKMessage();
		return jso.toJSONObject().toString();
	}

	@Override
	public void setCommand(String cmd) {
		// TODO Auto-generated method stub
		
	}
	
	public void setEmail(String email) {
		this.email=email;
	}
	
	public void setId(int id) {
		this.id=id;
	}
}
