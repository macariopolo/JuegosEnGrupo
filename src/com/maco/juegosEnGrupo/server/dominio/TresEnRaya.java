package com.maco.juegosEnGrupo.server.dominio;

import java.io.IOException;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.maco.tresenraya.jsonMessages.TresEnRayaBoardMessage;
import com.maco.tresenraya.jsonMessages.TresEnRayaMatchReadyMessage;
import com.maco.tresenraya.jsonMessages.TresEnRayaMovement;
import com.maco.tresenraya.jsonMessages.TresEnRayaWaitingMessage;

import edu.uclm.esi.common.jsonMessages.ErrorMessage;
import edu.uclm.esi.common.jsonMessages.JSONMessage;
import edu.uclm.esi.common.jsonMessages.OKMessage;
import edu.uclm.esi.common.server.domain.User;
import edu.uclm.esi.common.server.sockets.Notifier;


public class TresEnRaya extends Match {
	public static int TRES_EN_RAYA = 1;
	public static char X='X', O='O', WHITE = ' ';
	private User userWithTurn;
	
	private char[][] squares;
	
	public TresEnRaya(Game game) {
		super(game);
		squares=new char[3][3];
		for (int row=0; row<3; row++)
			for (int col=0; col<3; col++)
				squares[row][col]=WHITE;
	}

	@Override
	public String toString() {
		String r="";
		for (int row=0; row<3; row++)
			for (int col=0; col<3; col++)
				r+=this.squares[row][col];
		r+="#" + this.players.get(0).getEmail() + "#";
		if (this.players.size()==2) {
			r+=this.players.get(1).getEmail() + "#";
			r+=this.userWithTurn.getEmail();
		}
		return r;
	}

	@Override
	public void postMove(User user, JSONObject jsoMovement) throws Exception {
		if (!jsoMovement.get("type").equals(TresEnRayaMovement.class.getSimpleName())) {
			throw new Exception("Unexpected type of movement");
		}
		int row=jsoMovement.getInt("row");
		int col=jsoMovement.getInt("col");
		JSONMessage result=null;
		if (this.squares[row][col]!=WHITE) {
			result=new ErrorMessage("Square busy");
			Notifier.get().post(user, result);
		} else if (!this.isTheTurnOf(user)) {
			result=new ErrorMessage("It's not your turn");
			Notifier.get().post(user, result);
		} 
		updateBoard(row, col, result);
	}

	@Override
	protected void updateBoard(int row, int col, JSONMessage result)
			throws JSONException, IOException {
		if (result==null) {
			if (this.userWithTurn.equals(this.players.get(0))) {
				this.squares[row][col]=X;
				this.userWithTurn=this.players.get(1);
			} else {
				this.squares[row][col]=O;
				this.userWithTurn=this.players.get(0);
			}
			result=new TresEnRayaBoardMessage(this.toString());
			Notifier.get().post(this.players, result);
		}
	}

	@Override
	protected boolean isTheTurnOf(User user) {
		return this.userWithTurn.equals(user);
	}

	@Override
	protected void postAddUser(User user) {
		if (this.players.size()==2) {
			Random dado=new Random();
			JSONMessage jsTurn=new TresEnRayaWaitingMessage("Match ready. You have the turn.");
			JSONMessage jsNoTurn=new TresEnRayaWaitingMessage("Match ready. Wait for the opponent to move.");
			if (dado.nextBoolean()) {
				this.userWithTurn=this.players.get(0);
				try {
					Notifier.get().post(this.players.get(0), jsTurn);
					Notifier.get().post(this.players.get(1), jsNoTurn);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			} else {
				this.userWithTurn=this.players.get(1);
				try {
					Notifier.get().post(this.players.get(1), jsTurn);
					Notifier.get().post(this.players.get(0), jsNoTurn);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				JSONMessage jsBoard=new TresEnRayaBoardMessage(this.toString());
				Notifier.get().post(this.players.get(0), jsBoard);
				Notifier.get().post(this.players.get(1), jsBoard);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			JSONMessage jsm=new TresEnRayaWaitingMessage("Waiting for one more player");
			try {
				Notifier.get().post(this.players.get(0), jsm);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
