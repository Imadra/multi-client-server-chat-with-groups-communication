package trychat3;


import java.io.*;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server.
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no
 * need to count bytes or to wait for a line feed at the end of the frame
 * author Rassul
 */

public class ChatMessage implements Serializable {

	// The different types of message sent by the Client
	// WHOISIN to receive the list of the users connected
	// MESSAGE an ordinary text message
	// LOGOUT to disconnect from the Server
	static final int GROUPLIST = 0, MESSAGE = 1, LOGOUT = 2, JOIN = 3, MEMBERS = 4, LEAVE = 5;
	private int type;
	private String message;

	// constructor
	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}

	int getType() {
            return type;
	}

        String getTypeString() {
            if(type == 0)
                return "0";
            if(type == 1)
                return "1";
            if(type == 2)
                return "2";
            return "-1";
        }

	String getMessage() {
		return message;
	}
}
