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

## WhatsApp-Style Frontend Redesign
Client UI now includes:
- Sidebar with connection badge and active users panel.
- Header with online status indicator and read-only chip.
- Message list with bubble rendering:
- Incoming, outgoing, system, and command-response visual styles.
- Composer with `TextArea`, `SEND` button, `Enter` to send, `Shift+Enter` newline.

Server UI now includes:
- Dashboard-like header with online/offline status and bind endpoint.
- Live connected users card using random pastel user badges.
- Log card showing server activity in real time.

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

## Commands and Required Behaviors
- `allUsers`: server replies only to requester with active users list.
- `bye` or `end`: disconnect client and close socket.
- Empty username: connects in `READ-ONLY MODE`, send blocked except `allUsers`.
- Client UI shows online label + visual circle indicator.

## Validation Checklist
Functional:
- Connect with username and send messages between multiple clients.
- Connect with empty username and verify read-only restrictions.
- Request `allUsers` and verify user list response.
- Disconnect with `bye` and `end`.
- Verify server log and connected users list updates.

UI/UX:
- Message bubbles align correctly (incoming/outgoing).
- Timestamp and sender metadata render correctly.
- Auto-scroll keeps bottom when user is already near bottom.
- Layout is usable at `900x600` and above.

## Demo Video Script (3 minutes)
1. Start `TCPServer`, show online status and waiting log.
2. Start 2-3 `TCPClient` instances.
3. Show normal messaging between users.
4. Show read-only login with empty username.
5. Run `allUsers` and display returned user list.
6. Run `bye`/`end` and show disconnect updates on server UI.
7. Briefly show architecture files (`model` vs `ui`) and UML diagrams.

## UML
- Class diagram: `uml/class-diagram.puml`
- Deployment diagram: `uml/deployment-diagram.puml`

## Notes
- JavaFX dependency classifier is currently set for Windows in both `pom.xml` files.
- If running on another OS, update `javafx.platform` in each `pom.xml`.

## GitHub Publishing (Private Monorepo)
- Repository model: single monorepo containing `TCPServer`, `TCPClient`, `uml`, and root docs.
- Visibility: private repository.
- Recommended creation flow:
1. Create empty private GitHub repo (do not auto-add README/.gitignore/license).
2. Push local `main` branch.
3. Verify no `target/` or `.idea/` files are present in remote.
