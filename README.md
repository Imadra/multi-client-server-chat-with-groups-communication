# multi-client-server-chat-with-groups-communication

Multi threaded client-server chat written in java. All the communication between clients goes through the server only. Client can join groups to communicate only with the members of group.

When client connects to the server it should send: server hello 'username' without quotes for username. Server replies back: hi 'username'. Usernames are reserved during joining, so the server should handle conflicts appropriately.
  
- Server offers several pre-existing groups for clients to join. After connection was established, client can request the list of available groups: server groupslist.
Server replies with group names and members of each group who are currently online:
<groupname1>: <username1>, <username2> | <groupname2>:
<username3>; <username4>.

- Client can join one of the groups if it has not already: server join <groupname>

- Client can request the list of members of the group it has joined: server members

- After joining a group, client can send either a public (toall <message>) or a private message (<receiver> <message>). Public message should be received only by the group members. Private message can be sent only to a group member. If the receiver has disconnected before receiving the message, the server should notify the sender.

- Messages arrive to the clients in the following format: <sender>: <message>.

- Client can leave a group: server leave <groupname>

- Client can disconnect from the server: server exit
