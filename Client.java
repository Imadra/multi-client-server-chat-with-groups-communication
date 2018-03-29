package trychat3;


import java.net.*;
import java.io.*;
import java.util.*;


//The Client that can be run as a console
public class Client  {
	
	// notification
	private String notif = " *** ";

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;					// socket object
	
	private String server, username;	// server and username
	private int port;	

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
        }
        

	/*
	 *  Constructor to set below things
	 *  server: the server address
	 *  port: the port number
	 *  username: the username
	 */
	
	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}
	
	/*
	 * To start the chat
	 */
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		} 
		// exception handler if it failed
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		/* Creating both Data Stream */
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// creates the Thread to listen from the server 
		new ListenFromServer().start();
		// Send our username to the server this is the only message that we
		// will send as a String. All other messages will be ChatMessage objects
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// success we inform the caller that it worked
		return true;
	}

	/*
	 * To send a message to the console
	 */
	private void display(String msg) {

		System.out.println(msg);
		
	}
	
	/*
	 * To send a message to the server
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect
	 */
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {}
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
                try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
			
	}
	/*
	 * To start the Client in console mode use one of the following command
	 * > java Client
	 * > java Client username
	 * > java Client username portNumber
	 * > java Client username portNumber serverAddress
	 * at the console prompt
	 * If the portNumber is not specified 1500 is used
	 * If the serverAddress is not specified "localHost" is used
	 * If the username is not specified "Anonymous" is used
	 */
	public static void main(String[] args) {
		// default values if not entered
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = "Anonymous";
		Scanner scan = new Scanner(System.in);
		
		//System.out.println("Enter the username: ");
                while(true) {
                    int word = 1;
                    String cmdLine = "", firstWord = "", secondWord = "", thirdWord = "", four = "";
                    cmdLine = scan.nextLine();
                    for (int i = 0; i < cmdLine.length(); i++){
                        char c = cmdLine.charAt(i); 
                        if(c != ' ') {
                            if(word == 1)
                                firstWord = firstWord + c;
                            if(word == 2)
                                secondWord = secondWord + c;
                            if(word == 3)
                                thirdWord = thirdWord + c;
                            if(word == 4)
                                four = four + c;
                        }
                        else {
                            word++;
                        }
                        //Process char
                    }
                    if(firstWord.equals("server") && secondWord.equals("exit") && thirdWord.equals("")) {
                        scan.close();
                        return;
                    }
                    if(!firstWord.equals("server") || !secondWord.equals("hello") || thirdWord.equals("") || !four.equals("")) {
                        System.out.println("You did not correctly write the first command");
                        System.out.println("Form to write the first command: server hello <username>");
                        continue;
                    }
                    userName = thirdWord;
                    break;
                }
		
		Client client = new Client(serverAddress, portNumber, userName);
		// try to connect to the server and return if not connected
		if(!client.start())
			return;
		
                System.out.println("\nhi " + userName);
                
		/*System.out.println("\nHello.! Welcome to the chatroom.");
		System.out.println("Instructions:");
		System.out.println("1. Simply type the message to send broadcast to all active clients");
		System.out.println("2. Type '@username<space>yourmessage' without quotes to send message to desired client");
		System.out.println("3. Type 'WHOISIN' without quotes to see list of active clients");
		System.out.println("4. Type 'LOGOUT' without quotes to logoff from server");*/
		
		// infinite loop to get the input from the user
		while(true) {
                        System.out.print("> ");
                        int word = 0;
                        String cmdLine;
                        ArrayList<String> nword;
                        nword = new ArrayList<>();
                        String curWord = new String();
                        cmdLine = scan.nextLine();
                        for (int i = 0; i < cmdLine.length(); i++){
                            char c = cmdLine.charAt(i); 
                            if(c != ' ') {
                                curWord = curWord + c;
                            }
                            else {
                                nword.add(curWord);
                                word++;
                                curWord = "";
                            }
                            //Process char
                        }
                        nword.add(curWord);
                        word++;
                        nword.add("");
                        if(nword.get(0).equals("server") && nword.get(1).equals("exit") && nword.get(2).equals("")) {
                            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
                            break;
                        }
			
                        if(nword.get(0).equals("server") && nword.get(1).equals("grouplist") && nword.get(2).equals("")) {
                            //System.out.println("OK!\n");
                            client.sendMessage(new ChatMessage(ChatMessage.GROUPLIST, ""));
                            continue;
                        }
                        if (nword.get(0).equals("server") && nword.get(1).equals("join") && !nword.get(2).equals("") && nword.get(3).equals("")){
                            //join the group
                            client.sendMessage(new ChatMessage(ChatMessage.JOIN, nword.get(2)));
                            continue;
                        }
                        if (nword.get(0).equals("server") && nword.get(1).equals("members") && nword.get(2).equals("")) {
                            client.sendMessage(new ChatMessage(ChatMessage.MEMBERS, ""));
                            continue;
                        }
                        if (nword.get(0).equals("toall")) {
                            String msg = new String();
                            for(int i=1;i<word;i++) {
                                msg = msg + nword.get(i);
                                msg = msg + " ";
                            }
                            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
                            continue;
                        }
                        if(nword.get(0).equals("server") && nword.get(1).equals("leave") && !nword.get(2).equals("") && nword.get(3).equals("")) {
                            client.sendMessage(new ChatMessage(ChatMessage.LEAVE, nword.get(2)));
                            continue;
                        }
                        if(!nword.get(1).equals("")) {
                            String msg = new String();
                            msg = msg + "@";
                            msg = msg + nword.get(0);
                            msg = msg + " ";
                            for(int i=1;i<word;i++) {
                                msg = msg + nword.get(i);
                                msg = msg + " ";
                            }
                            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
                        }
                        /*else {
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}*/
		}
		// close resource
		scan.close();
		// client completed its job. disconnect client.
		client.disconnect();	
	}
        

	/*
	 * a class that waits for the message from the server
	 */
	class ListenFromServer extends Thread {

                @Override
		public void run() {
			while(true) {
				try {
					// read the message form the input datastream
					String msg = (String) sInput.readObject();
					// print the message
					System.out.println(msg);
					System.out.print("> ");
				}
				catch(IOException e) {
					display(notif + "Server has closed the connection: " + e + notif);
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}

