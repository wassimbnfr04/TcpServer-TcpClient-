# TCP Group Chat (Java Sockets + JavaFX)

This repository contains two Maven applications:
- `TCPServer`: central TCP distributor + JavaFX monitoring UI.
- `TCPClient`: JavaFX group chat UI connected to the TCP server.

The implementation keeps model and view decoupled. Socket communication is in model classes, while JavaFX controllers handle presentation only.

## Tech Stack
- Java 17+
- Java Sockets (TCP)
- JavaFX (`GridPane` + CSS)
- Maven

## Project Structure
- `TCPServer/src/main/java/com/chat/server/model`: server socket logic.
- `TCPServer/src/main/java/com/chat/server/ui`: server JavaFX UI.
- `TCPClient/src/main/java/com/chat/client/model`: client socket logic.
- `TCPClient/src/main/java/com/chat/client/ui`: client JavaFX UI and message rendering helpers.
- `TCPServer/src/main/resources/config.properties`: server bind/host/port defaults.
- `TCPClient/src/main/resources/config.properties`: client host/port defaults.

## Build
```bash
cd TCPServer
mvn package
```
```bash
cd TCPClient
mvn package
```

Generated artifacts:
- `TCPServer/target/tcp-server-1.0.0.jar`
- `TCPClient/target/tcp-client-1.0.0.jar`

## Run
Server:
```bash
java -jar TCPServer/target/tcp-server-1.0.0.jar
```

Client:
```bash
java -jar TCPClient/target/tcp-client-1.0.0.jar <ServerIPAddress> <PortNumber>
```

Example:
```bash
java -jar TCPClient/target/tcp-client-1.0.0.jar localhost 3000
```

IntelliJ entry points:
- `TCPServer` for server startup (`java TCPServer`)
- `TCPClient` for client startup (`java TCPClient <ServerIPAddress> <PortNumber>`)

## Validation Checklist
Functional:
- Connect with username and send messages between multiple clients.
- Connect with empty username and verify read-only restrictions.
- Request `allUsers` and verify user list response.
- Disconnect with `bye` and `end`.
- Verify server log and connected users list updates.

## Notes
- JavaFX dependency classifier is currently set for Windows in both `pom.xml` files.
- If running on another OS, update `javafx.platform` in each `pom.xml`.
