package network_game;

import java.io.*;
import java.net.*;
import java.util.*;

public class RoomServer {

    private static final int PORT = 5001;

    private static final Set<String> usedNames =
            Collections.synchronizedSet(new HashSet<>());
    private static final List<ClientHandler> allHandlers =
            Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, RoomInfo> rooms =
            Collections.synchronizedMap(new LinkedHashMap<>());

    private static final List<String> BAD_WORDS = new ArrayList<>();
    private static final int MAX_WARNING = 3;
    private static final long MUTE_TIME = 30_000;

    public static void main(String[] args) {
        loadBadWords();
        System.out.println("RoomServer 시작 — 포트 " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket sock = serverSocket.accept();
                ClientHandler handler = new ClientHandler(sock);
                allHandlers.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadBadWords() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        RoomServer.class.getClassLoader()
                                .getResourceAsStream("badwords.txt")
                )
        )) {
            String line;
            while ((line = br.readLine()) != null)
                if (!line.isBlank())
                    BAD_WORDS.add(line.trim().toLowerCase());
        } catch (Exception e) {
            System.out.println("[WARN] badwords.txt 로딩 실패");
        }
    }

    static class RoomInfo {
        final String name;
        final List<ClientHandler> users =
                Collections.synchronizedList(new ArrayList<>());

        boolean gameStarted = false;
        final Object gameLock = new Object();
        GameState game;

        RoomInfo(String name) {
            this.name = name;
        }

        boolean isFull() {
            return users.size() >= 4;
        }
    }

    static class ClientHandler extends Thread {

        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        private String name;
        private String joinedRoom;
        private String team;

        private int badCount = 0;
        private long muteUntil = 0;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // 닉네임 입력 & 중복 검사
                out.println("ENTER_NAME");

                while (true) {
                    name = in.readLine();
                    if (name == null) return;

                    name = name.trim();
                    if (name.isEmpty()) {
                        out.println("NAME_INVALID");
                        continue;
                    }

                    synchronized (usedNames) {
                        if (!usedNames.contains(name)) {
                            usedNames.add(name);
                            break; 
                        }
                    }

                    out.println("NAME_INVALID");
                }

                // 메인 메시지 루프
                String line;
                while ((line = in.readLine()) != null) {

                    if (line.startsWith("ENTER_ROOM ")) {
                        handleEnterRoom(line.substring(11));
                    }

                    else if (joinedRoom == null) {
                        if (line.equals("GET_ROOMS")) {
                            sendRoomList();
                        }
                        else if (line.startsWith("CREATE ")) {
                            createRoom(line.substring(7));
                        }
                    }

                    else {
                        if (line.startsWith("PLAY ")) {
                            handlePlay(line.substring(5));
                        }
                        else if (line.startsWith("ALL ")) {
                            handleChat(line.substring(4), false);
                        }
                        else if (line.startsWith("TEAM ")) {
                            handleChat(line.substring(5), true);
                        }
                    }
                }

            } catch (IOException e) {
                // 연결 종료
            } finally {
                cleanup();
            }
        }


        private void sendRoomList() {
            synchronized (rooms) {
                for (String rn : rooms.keySet())
                    out.println("ROOM " + rn);
            }
            out.println("ROOM_END");
        }

        private void createRoom(String roomName) {
            synchronized (rooms) {
                if (rooms.containsKey(roomName)) {
                    out.println("MSG [SYSTEM] 이미 존재하는 방입니다.");
                    return;
                }
                rooms.put(roomName, new RoomInfo(roomName));
            }

            handleEnterRoom(roomName);
        }

        private void handleEnterRoom(String roomName) {
            RoomInfo r;
            synchronized (rooms) {
                r = rooms.get(roomName);
                if (r == null) {
                    out.println("MSG 방 입장 실패");
                    return;
                }
                if (r.isFull()) {
                    out.println("MSG 이미 방에 입장");
                    return;
                }

                team = (r.users.size() % 2 == 0) ? "A" : "B";
                joinedRoom = roomName;
                r.users.add(this);
            }

            broadcast(r, "MSG [SYSTEM] " + name + " 입장 (" + team + ")");

            if (r.users.size() == 4)
                startGame(r);
        }


        private void startGame(RoomInfo r) {
            synchronized (r.gameLock) {
                if (r.gameStarted) return;
                r.gameStarted = true;

                List<String> players = new ArrayList<>();
                for (ClientHandler u : r.users)
                    players.add(u.name);

                r.game = new GameState(players);

                for (ClientHandler u : r.users) {
                    for (ClientHandler v : r.users) {
                        u.out.println("PLAYER " + v.name + " " + v.team);
                    }
                }

                broadcast(r, "GAME_START");

                for (ClientHandler u : r.users) {
                    u.out.println("HAND " + r.game.getHandString(u.name));
                    u.out.println(makeCountsMessageFor(u));
                }

                broadcastCenter(r);
            }
        }


        private void handlePlay(String cardStr) {
            RoomInfo r = rooms.get(joinedRoom);
            if (r == null) return;

            synchronized (r.gameLock) {
                boolean ok = r.game.playCard(name, Card.fromString(cardStr));
                if (!ok) return;

                broadcast(r, "PLAY_OK " + name + " " + cardStr);
                broadcastCenter(r);

                for (ClientHandler u : r.users)
                    u.out.println(makeCountsMessageFor(u));

                if (r.game.isFinished())
                	broadcast(r, "GAME_OVER " + r.game.getWinnerTeam());
            }
        }
        
        

        // ================== COUNTS 메시지 ==================
        private String makeCountsMessageFor(ClientHandler viewer) {
            RoomInfo r = rooms.get(viewer.joinedRoom);
            GameState g = r.game;

            int teammate = 0;
            int enemyL = 0;
            int enemyR = 0;

            for (ClientHandler u : r.users) {
                if (u == viewer) continue;

                int size = g.getHandCount(u.name);
                if (u.team.equals(viewer.team))
                    teammate = size;
                else if (enemyL == 0)
                    enemyL = size;
                else
                    enemyR = size;
            }

            return "COUNTS "
	            + teammate + " "
	            + enemyL + " "
	            + enemyR + " "
	            + g.getSideLeftCount() + " "
	            + g.getSideRightCount();

        }

        private void handleChat(String msg, boolean teamOnly) {
            RoomInfo r = rooms.get(joinedRoom);
            if (r == null) return;

            long now = System.currentTimeMillis();
            if (muteUntil > now) {
                out.println("MSG [SYSTEM] 채팅 제한 중");
                return;
            }

            if (containsBadWord(msg)) {
                badCount++;
                msg = filterBadWords(msg);
                if (badCount >= MAX_WARNING)
                    muteUntil = now + MUTE_TIME;
            }

            String outMsg = "MSG [" + name + "] " + msg;
            if (teamOnly)
                broadcastTeam(r, team, outMsg);
            else
                broadcast(r, outMsg);
        }

        private boolean containsBadWord(String msg) {
            String lower = msg.toLowerCase();
            for (String w : BAD_WORDS)
                if (lower.contains(w)) return true;
            return false;
        }

        private String filterBadWords(String msg) {
            for (String w : BAD_WORDS)
                msg = msg.replaceAll("(?i)" + w, "*".repeat(w.length()));
            return msg;
        }

        private void broadcast(RoomInfo r, String msg) {
            synchronized (r.users) {
                for (ClientHandler u : r.users)
                    u.out.println(msg);
            }
        }

        private void broadcastTeam(RoomInfo r, String team, String msg) {
            synchronized (r.users) {
                for (ClientHandler u : r.users)
                    if (team.equals(u.team))
                        u.out.println(msg);
            }
        }

        private void broadcastCenter(RoomInfo r) {
            Card c = r.game.getCenterTop();
            broadcast(r, "CENTER " + (c == null ? "NONE" : c));
        }

        private void cleanup() {
            try { 
            	if (name != null) {
                    usedNames.remove(name);   
                }
            	socket.close(); 
            	} catch (Exception ignored) {}
            allHandlers.remove(this);
            usedNames.remove(name);
            if (joinedRoom != null) {
                RoomInfo r = rooms.get(joinedRoom);
                if (r != null) r.users.remove(this);
            }
        }
    }
}
