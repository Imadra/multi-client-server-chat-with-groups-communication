package trychat3;


import java.io.*;
import static java.lang.Double.max;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// the server that can be run as a console
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
    // groups
        public ArrayList<String> groupNames;
        public int groupNum;
        private ArrayList<ArrayList<String>> groupMembers;
	// to display time
	private SimpleDateFormat sdf;
	// the port number to listen for connection
	private int port;
	// to check if server is running
	private boolean keepGoing;
	// notification
	private String notif = " *** ";
	
	//constructor that receive the port to listen to for connection as parameter
	
	public Server(int port) {
                this.groupNum = 3;
                this.groupNames = new ArrayList<String>();
                this.groupNames.add("A");
                this.groupNames.add("B");
                this.groupNames.add("C");
                this.groupMembers = new ArrayList<ArrayList<String>>();
                for(int i=0;i<this.groupNum;i++) {
                    ArrayList<String> tmp = new ArrayList<String>();
                    groupMembers.add(tmp);
                }
		// the port
		this.port = port;
		// to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// an ArrayList to keep the list of the Client
		al = new ArrayList<ClientThread>();
	}
	
	public void start() {
		keepGoing = true;
		//create socket server and wait for connection requests 
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections ( till server is active )
			while(keepGoing) 
			{
				display("Server waiting for Clients on port " + port + ".");
				
				// accept connection if requested from client
				Socket socket = serverSocket.accept();
				// break if server stoped
				if(!keepGoing)
					break;
				// if client is connected, create its thread
				ClientThread t = new ClientThread(socket);
				//add this client to arraylist
				al.add(t);
				
				t.start();
			}
			// try to stop the server
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					// close all data streams and socket
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
                        String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
	// to stop the server
	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}
	
	// Display an event to the console
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
	}
	
	// to broadcast a message to all Clients
	private synchronized boolean broadcast(String message) {
		// add timestamp to the message
		String time = sdf.format(new Date());
		
		// to check if message is private i.e. client to client message
		String[] w = message.split(" ",3);
                String user;
                user = w[0].substring(0, (int) max(w[0].length()-1, 0));
		
		boolean isPrivate = false;
		if(w[1].charAt(0)=='@') 
			isPrivate=true;
		int groupN = -1;
                for(int i=0; i < groupNum; i++) {
                    for(int j=0; j<groupMembers.get(i).size(); j++) {
                        String curMember = (groupMembers.get(i).get(j));
                        if(curMember.equals(user)) {
                            groupN = i;
                            break;
                        }
                    }
                    if(groupN != -1)
                        break;
                }

                if(groupN == -1)
                {
                    for(int y=al.size(); --y>=0;)
                    {
                            ClientThread ct1=al.get(y);
                            String check=ct1.getUsername();
                            if(check.equals(user))
                            {
                                ct1.writeMsg("You are not a member of any group, so you can not write messages");
                                break;
                            }
                    }
                    return true;
                }
                else {
		
                    // if private message, send message to mentioned username only
                    if(isPrivate==true)
                    {
                            String tocheck=w[1].substring(1, w[1].length());

                            message=w[0]+w[2];
                            String messageLf = time + " " + message + "\n";
                            boolean found=false;
                            // we loop in reverse order to find the mentioned username
                            for(int y=al.size(); --y>=0;)
                            {
                                    ClientThread ct1=al.get(y);
                                    String check=ct1.getUsername();
                                    if(check.equals(tocheck))
                                    {
                                        boolean p = false;
                                        for(int j=0; j<groupMembers.get(groupN).size(); j++) {
                                            String curMember = (groupMembers.get(groupN).get(j));
                                            if(curMember.equals(tocheck)) {
                                                p = true;
                                                break;
                                            }
                                        }
                                        if(p == false) {
                                            for(int yy=al.size(); --yy>=0;)
                                            {
                                                    ClientThread ct2=al.get(yy);
                                                    String check2=ct2.getUsername();
                                                    if(check2.equals(user))
                                                    {
                                                        ct2.writeMsg("Receiver and you are not on the same group");
                                                        break;
                                                    }
                                            }
                                            return true;
                                        }
                                        // try to write to the Client if it fails remove it from the list
                                        if(!ct1.writeMsg(messageLf)) {
                                                al.remove(y);
                                                display("Disconnected Client " + ct1.username + " removed from list.");
                                        }
                                        // username found and delivered the message
                                        found=true;
                                        break;
                                    }
                            }
                            // mentioned user not found, return false
                            if(found!=true)
                            {
                                return false; 
                            }
                    }
                    // if message is a broadcast message
                    else
                    {
                                String ms = groupNames.get(groupN);
                                /*for(int j=0; j<groupMembers.get(groupN).size(); j++) {
                                    String curMember = (groupMembers.get(groupN).get(j));
                                }*/


                                String messageLf = time + " " + message + "\n";
                                // display message
                                for(int j=0; j<groupMembers.get(groupN).size(); j++) {
                                    String curMember = (groupMembers.get(groupN).get(j));
                                    for(int y=al.size(); --y>=0;)
                                    {
                                            ClientThread ct1=al.get(y);
                                            String check=ct1.getUsername();
                                            if(check.equals(curMember))
                                            {
                                                ct1.writeMsg(messageLf);
                                                break;
                                            }
                                    }
                                }
                    }
                    return true;
		
                }
	}

	// if client sent LOGOUT message to exit
	synchronized void remove(int id) {
		
		String disconnectedClient = "";
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// if found remove it
			if(ct.id == id) {
				disconnectedClient = ct.getUsername();
				al.remove(i);
				break;
			}
		}
		broadcast(notif + disconnectedClient + " has left the chat room." + notif);
	}
	
	/*
	 *  To run as a console application
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 
	public static void main(String[] args) {
		// start server on port 1500 unless a PortNumber is specified 
		int portNumber = 1500;
		/*switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}*/
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	// One instance of this thread will run for each client
	class ClientThread extends Thread {
		// the socket to get messages from client
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// message object to recieve message and its type
		ChatMessage cm;
		// timestamp
		String date;

		// Constructor
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			//Creating both Data Stream
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				broadcast(notif + username + " has joined the chat room." + notif);
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
                        date = new Date().toString() + "\n";
		}
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}
                

		// infinite loop to read and forward message
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// get the message from the ChatMessage object received
				String message = cm.getMessage();

				// different actions based on type message
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					boolean confirmation =  broadcast(username + ": " + message);
					if(confirmation==false){
						String msg = notif + "Sorry. No such user exists." + notif;
						writeMsg(msg);
					}
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				case ChatMessage.GROUPLIST:
                                        String mes = "";
                                        for(int i=0; i < groupNum; i++) {
                                            mes = mes + groupNames.get(i);
                                            mes = mes + ": ";
                                            for(int j=0; j<groupMembers.get(i).size(); j++) {
                                                mes = mes + (groupMembers.get(i).get(j));
                                                if(j != groupMembers.get(i).size() - 1)
                                                    mes = mes + ", ";
                                            }
                                            mes = mes + " | ";
                                        }
					writeMsg(mes);
                                        break;
                                case ChatMessage.JOIN:
                                    boolean p = false;
                                    for(int i=0; i < groupNum; i++) {
                                        for(int j=0; j<groupMembers.get(i).size(); j++) {
                                            String curMember = (groupMembers.get(i).get(j));
                                            if(curMember.equals(username))
                                            {
                                                p = true;
                                                break;
                                            }
                                        }
                                        if(p == true) {
                                            break;
                                        }
                                    }
                                    if(p == true) {
                                        writeMsg("You are already a member of one group!");
                                    }
                                    else {
                                        int gr = -1;
                                        for(int i=0; i < groupNum; i++) {
                                            String curName = groupNames.get(i);
                                            if(message.equals(curName))
                                            {
                                                groupMembers.get(i).add(username);
                                                gr = i;
                                                break;
                                            }
                                        }
                                        if(gr == -1) {
                                            writeMsg("There is no such group in the grouplist!");
                                        }
                                        else {
                                            writeMsg("You entered group " + groupNames.get(gr));
                                        }
                                    }
                                    break;
                                case ChatMessage.MEMBERS:
                                    int groupN = -1;
                                    for(int i=0; i < groupNum; i++) {
                                        for(int j=0; j<groupMembers.get(i).size(); j++) {
                                            String curMember = (groupMembers.get(i).get(j));
                                            if(curMember.equals(username)) {
                                                groupN = i;
                                                break;
                                            }
                                        }
                                        if(groupN != -1)
                                            break;
                                    }
                                    if(groupN != -1) {
                                        String ms = groupNames.get(groupN);
                                        ms = ms + ": ";
                                        for(int j=0; j<groupMembers.get(groupN).size(); j++) {
                                            String curMember = (groupMembers.get(groupN).get(j));
                                            if(j != 0)
                                                ms = ms + ", ";
                                            ms = ms + curMember;
                                        }
                                        writeMsg(ms);
                                    }
                                    else {
                                        writeMsg("You are not a member of any groups!");
                                    }
                                    break;
                                case ChatMessage.LEAVE:
                                    int gr = -1;
                                    for(int i=0; i < groupNum; i++) {
                                        String curName = groupNames.get(i);
                                        if(message.equals(curName))
                                        {
                                            gr = i;
                                            break;
                                        }
                                    }
                                    if(gr == -1) {
                                        writeMsg("There is no such group in the grouplist!");
                                    }
                                    else {
                                        p = false;
                                        for(int j=0; j<groupMembers.get(gr).size(); j++) {
                                            String curMember = (groupMembers.get(gr).get(j));
                                            if(curMember.equals(username)) {
                                                groupMembers.get(gr).remove(j);
                                                p = true;
                                                break;
                                            }
                                        }
                                        if(p == false) {
                                            writeMsg("You are not a member of this group!");
                                        }
                                    }
                                    break;
                            }
			}
			// if out of the loop then disconnected and remove from client list
			remove(id);
			close();
		}
		
		// close everything
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// write a String to the Client output stream
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display(notif + "Error sending message to " + username + notif);
				display(e.toString());
			}
			return true;
		}
	}
}

