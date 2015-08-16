<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="org.apache.struts2.ServletActionContext, edu.uclm.esi.common.server.domain.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Juegos en grupo 2.0 - index.jsp</title>
<%
User user=(User) ServletActionContext.getRequest().getSession().getAttribute("user");
%>
</head>
<body>
	<table width="100%" border="1">
		<tr>
			<td>
				<form id="registerForm">
					Registrarse:<br>
					e-mail: <input type="text" id="email"><br>
					password: <input type="password" id="pwd1"><br>
					repite password: <input type="password" id="pwd2"><br>
					<input type="button" value="Aceptar" onclick="register()">
				</form>
			</td>
			<td>
				<form id="identifyForm">
					Identificarse:<br>
					e-mail: <input type="text" id="email" value="a@a.com"><br>
					password: <input type="password" id="pwd" value="a"><br>
					<input type="button" value="Aceptar" onclick="identify()">
				</form>
				<form id="logoutForm" style="display:none">
					Conectado como: <input type="text" id="email" readonly="readonly"><br>
					<input type="button" onclick="desconectar()" value="Desconectar"/>
				</form>
			</td>
		</tr>
		<tr>
			<td colspan="2">
				<textarea id="textareaMensajes" rows="5" cols="200" readonly="readonly"></textarea>
			</td>
		</tr>
		<tr>
			<td id="zonaTablero">
				<div id="mensajesTablero"></div>
			</td>
			<td id="zonaJugadores">
			</td>
		</tr>
	</table>
</body>

<script language="JavaScript">
var formIdentify=document.getElementById("identifyForm");
var formRegister=document.getElementById("registerForm");
var formLogout = document.getElementById("logoutForm");

var textAreaMensajes=document.getElementById("textareaMensajes");
var zonaTablero = document.getElementById("zonaTablero");
var mensajesTablero=document.getElementById("mensajesTablero");

var zonaJugadores = document.getElementById("zonaJugadores");

var idUser;

var game;

var endPointURL = "ws://" + window.location.host + "/JuegosEnGrupo/notificador/notificador";
var socket;

function cargarSocket(jsmLoginMessage) {
	socket = new WebSocket(endPointURL);
	socket.onopen = function(event) {
		socket.send(jsmLoginMessage);
		showMensaje("Websocket abierto y escuchando (readyState=" + this.readyState + ")");
	}
	socket.onmessage = function(event) {
		var jsm=JSON.parse(event.data);
		if (jsm.type=="ErrorMessage") {
			alert(jsm.text);
		} else if (jsm.type=="LoginMessageAnnouncement") {
			showJugador(jsm.email);
		} else if (jsm.type=="LogoutMessageAnnouncement") {
			removeJugador(jsm.email);
		} else if (jsm.type=="TresEnRayaWaitingMessage") {
			mensajesTablero.innerHTML="Esperando a que llegue otro jugador";
		} else if (jsm.type=="TresEnRayaBoardMessage") {
			var oponente=jsm.player1;
			if (formLogout["email"].value==oponente)
				oponente=jsm.player2;
			mensajesTablero.innerHTML="Oponente: " + oponente + "; turno de: " + jsm.userWithTurn;
			game.show(jsm.squares);
		} 
		showMensaje(jsm.type);
	}
	socket.onclose = function(event) {
		showMensaje("Websocket cerrado (readyState=" + this.readyState + ")");
	}
}

function showJugador(email) {
	showMensaje("Ha llegado " + email);
	var btnJugador=document.createElement("input");
	btnJugador.setAttribute("type", "button");
	btnJugador.setAttribute("value", email);
	zonaJugadores.appendChild(btnJugador);
}

function removeJugador(email) {
	for (var i=0; i<zonaJugadores.childNodes.length; i++) {
		var btn=zonaJugadores.childNodes[i];
		if (btn.value==email) {
			zonaJugadores.removeChild(zonaJugadores.childNodes[i]);
			return;
		}
	}
}

function showMensaje(msj) {
	textAreaMensajes.value=msj + String.fromCharCode(13) + textAreaMensajes.value;
}

function showBotonJoin(juego) {
	var btnJuego=document.createElement("input");
	btnJuego.setAttribute("type", "button");
	btnJuego.setAttribute("value", juego.name + " (" + juego.id + ")");
	btnJuego.setAttribute("onclick", "joinGame('" + juego.id + "')");
	zonaTablero.appendChild(btnJuego);
}

function joinGame(idGame) {
	if (idGame==1) {
		game=new TresEnRaya(idUser);
	} else {
		alert("Juego no implementado");
		return;
	}
	game.join();
	game.show(null);
}

/***/
 
function DatosPartida() {
	this.idGame=null;
	this.idMatch=null;
	this.idUser=null;
}

DatosPartida.prototype.getComoParametros = function() {
	var result= "idUser=" + this.idUser + "&idGame=" + this.idGame + "&idMatch=" + this.idMatch;
	return result;
}
  
function Game() {
	this.datosPartida=new DatosPartida();
}

Game.prototype.join = function() {
	var req=new XMLHttpRequest();
	req.open("post", "JoinGame.action");
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	req.onreadystatechange = function() {
		if (req.readyState==4) {
			var respuesta=JSON.parse(req.responseText);
			var resultado=JSON.parse(respuesta.resultado);
			var tipo=resultado.type;
			if (tipo=="OKMessage") {
				game.datosPartida.idMatch=resultado["additionalInfo"][0];
				showMensaje("Unido a la partida con idMatch: " + game.datosPartida.idMatch);
			}
		}
	};
	var pars="idUser=" + this.datosPartida.idUser + "&idGame=" + this.datosPartida.idGame;
	req.send(pars);
}

Game.prototype.enviarMovimiento = function(fila, columna) {
	var req=new XMLHttpRequest();
	req.open("post", "SendMovement.action");
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	req.onreadystatechange = function() {
		if (req.readyState==4) {
			var respuesta=JSON.parse(req.responseText);
			var resultado=JSON.parse(respuesta.resultado);
			var tipo=resultado.type;
			if (tipo=="ErrorMessage") {
				alert("Error: " + resultado.text);
			}
		}
	};
	var movimiento= {
		"row"  : fila,
		"col"  : columna,
		"type" : "TresEnRayaMovement"
	};
	var pars=this.datosPartida.getComoParametros() + "&command=" + JSON.stringify(movimiento); 
	showMensaje("Envío de movimiento. Parámetros: " + pars);
	req.send(pars);
}

TresEnRaya.prototype=new Game();
TresEnRaya.constructor=Game;

function TresEnRaya(idUser) {
	this.datosPartida.idGame=1;
	this.datosPartida.idUser=idUser;
	this.casillas=new Array();
	for (var fila=0; fila<3; fila++) {
		this.casillas[fila]=new Array();
		for (var col=0; col<3; col++) {
			this.casillas[fila][col]=new Casilla("-1");
		}
	}
}

TresEnRaya.prototype.show = function(ristra) {
	while (zonaTablero.firstChild) {
		zonaTablero.removeChild(zonaTablero.firstChild);
	}
	var tabla=document.createElement("table");
	var cont=0;
	for (var fila=0; fila<3; fila++) {
		var tRow=document.createElement("tr");
		tabla.appendChild(tRow);
		for (var col=0; col<3; col++) {
			var casilla=this.casillas[fila][col];
			var tCelda=document.createElement("td");
			var btnCasilla=document.createElement("input");
			btnCasilla.setAttribute("type", "button");
			btnCasilla.setAttribute("value", " ");
			btnCasilla.setAttribute("onclick", "game.enviarMovimiento(" + fila + ", " + col + ")");
			if (ristra!=null) {
				btnCasilla.setAttribute("value", ristra[cont]);
			}
			tCelda.appendChild(btnCasilla);
			tRow.appendChild(tCelda);
			cont++;
		}
	}
	zonaTablero.appendChild(tabla);
	zonaTablero.appendChild(mensajesTablero);
}

function Casilla(idPropietario) {
	this.idPropietario=idPropietario;
}

/***/

function identify() {
	var login = formIdentify["email"].value;
	var pwd1 = formIdentify["pwd"].value;
	var object = {
			email : login,
			pwd : pwd1,
			userType : "USER_WEB",
			type : "LoginMessage"
	};
	var command = JSON.stringify(object);
	
	var req=new XMLHttpRequest();
	req.open("post", "Login.action");
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	req.onreadystatechange = function() {
		if (req.readyState==4) {
			var respuesta=JSON.parse(req.responseText);
			var resultado=respuesta.resultado;
			var tipo=JSON.parse(resultado).type;
			if (tipo=="OKMessage") {
				formIdentify.style.display="none";
				formRegister.style.display="none";
				formLogout.style.display="block";
				formLogout["email"].value=login;
				idUser=JSON.parse(resultado)["additionalInfo"][0];
				showMensaje("idUser=" + idUser);
				cargarSocket(command);
				loadGames();
			} else {
				var text=JSON.parse(resultado).text;
				alert(text);
			}
		}
	};
	var pars="command=" + command;
	req.send(pars);
}

function loadGames() {
	var req=new XMLHttpRequest();
	req.open("post", "GameList.action");
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	req.onreadystatechange = function() {
		if (req.readyState==4) {
			var respuesta=JSON.parse(req.responseText);
			var resultado=JSON.parse(respuesta.resultado);
			var games=resultado.games;
			for (var i=0; i<games.length; i++) {
				var juego=games[i];
				showBotonJoin(juego);
			}
		}
	};
	req.send();
}

function register() {
	var login = formRegister["email"].value;
	var pwd1 = formRegister["pwd1"].value;
	var pwd2 = formRegister["pwd2"].value;
	var object = {
			email : login,
			pwd1 : pwd1,
			pwd2 : pwd2
	};
	var command = JSON.stringify(object);
	
	var req=new XMLHttpRequest();
	req.open("post", "Register.action");
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	req.onreadystatechange = function() {
		if (req.readyState==4) {
			var respuesta=JSON.parse(req.responseText);
			var resultado=respuesta.resultado;
			var tipo=JSON.parse(resultado).type;
			if (tipo=="OKMessage") {
				formIdentify["email"].value=login;
				formIdentify["pwd"].value=pwd1;
			} else {
				var text=JSON.parse(resultado).text;
				alert(text);
			}
		}
	};
	var pars="command=" + command;
	req.send(pars);
}

function desconectar() {
	var req=new XMLHttpRequest();
	req.open("post", "Logout.action");
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	req.onreadystatechange = function() {
		if (req.readyState==4) {
			var respuesta=JSON.parse(req.responseText);
			var resultado=respuesta.resultado;
			var tipo=JSON.parse(resultado).type;
			if (tipo=="OKMessage") {
				formIdentify.style.display="block";
				formRegister.style.display="block";
				formLogout.style.display="none";
				showMensaje("Usuario desconectado");
				socket.close();
				while (zonaTablero.firstChild) {
					zonaTablero.removeChild(zonaTablero.firstChild);
				}
				while (zonaJugadores.firstChild) {
					zonaJugadores.removeChild(zonaJugadores.firstChild);
				}
			} else {
				var text=JSON.parse(resultado).text;
				alert(text);
			}
		}
	};
	var pars="email=" + formLogout["email"].value;
	req.send(pars);
}


</script>
</html>