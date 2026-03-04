# Real-Time Group Chat (Java Sockets + JavaFX)

Real-time multi-client group chat application built with Java TCP sockets and JavaFX.  
The project demonstrates backend concurrency, clean architecture, and a WhatsApp-style dark chat UI.

## Highlights
- Multi-client TCP server that distributes messages in real time.
- JavaFX client with bubble-based chat interface and dark theme.
- Strict separation of concerns: socket logic in model layer, UI in controller/view layer.
- Read-only mode, command handling, live user monitoring, and activity logging.
- Delivered as two Maven modules (`TCPServer`, `TCPClient`) with UML diagrams.

## Architecture
- **TCPServer**: accepts client connections, handles concurrent sessions, broadcasts formatted messages, logs activity, and exposes connected users in UI.
- **TCPClient**: connects to server, sends/receives messages, handles commands (`allUsers`, `bye`, `end`), and renders chat UI.
- **Decoupling**: model classes are independent from JavaFX; communication between model and UI is through listener contracts.

## Feature Set
- Username-based access to full chat mode.
- Automatic **READ-ONLY MODE** when username is empty.
- Send via **SEND** button or **Enter** key (`Shift+Enter` for newline).
- `allUsers` command returns active users to requesting client.
- `bye` / `end` disconnects client cleanly.
- Online/offline status indicators in both server and client UI.
- Server-side connected users list with distinct visual badges.

## Tech Stack
- Java 17+
- Java Sockets (TCP)
- JavaFX (`GridPane` + CSS)
- Maven

## Repository Structure
- `TCPServer/src/main/java/com/chat/server/model` - server socket logic
- `TCPServer/src/main/java/com/chat/server/ui` - server JavaFX UI
- `TCPClient/src/main/java/com/chat/client/model` - client socket logic
- `TCPClient/src/main/java/com/chat/client/ui` - client JavaFX UI + message rendering helpers
- `TCPServer/src/main/resources/config.properties` - server host/bind/port defaults
- `TCPClient/src/main/resources/config.properties` - client host/port defaults
- `uml/` - class and deployment diagrams

## Build
```bash
cd TCPServer
mvn package
cd ../TCPClient
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
- `TCPServer` (`java TCPServer`)
- `TCPClient` (`java TCPClient <ServerIPAddress> <PortNumber>`)

## Validation Checklist
- Connect multiple clients and verify real-time group messaging.
- Connect without username and verify read-only restrictions.
- Run `allUsers` and validate user list response.
- Disconnect with `bye` and `end`.
- Confirm server logs and connected-users panel update correctly.

## UML
- `uml/class-diagram.puml`
- `uml/deployment-diagram.puml`

## Notes
- JavaFX dependency classifier is currently set for Windows in both `pom.xml` files.
- If running on another OS, update `javafx.platform` in each `pom.xml`.
