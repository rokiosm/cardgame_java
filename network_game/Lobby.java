package network_game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Lobby extends JFrame {

    private JTextField roomTitleInput;
    private JPanel roomListPanel;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final ArrayList<String> rooms = new ArrayList<>();
    private final String userName;

    private Thread receiveThread;
    private volatile boolean enteringRoom = false;

    private String selectedBadge;

    // ★ 추가: 입장 시 선택된 방 이름
    private String enteringRoomName;

    private boolean connected = false;

    // ===== 기존 생성자 (유지) =====
    public Lobby(String userName) {
        super("방 로비 - " + userName);
        this.userName = userName;

        connectServer();
        if (!connected) return;

        buildGUI();
        startReceiveThread();

        sendMessage("GET_ROOMS");
        setVisible(true);
    }

    // ===== ★ 추가 생성자 (최소 수정) =====
    public Lobby(String userName, String selectedBadge) {
        this(userName);
        this.selectedBadge = selectedBadge;
    }

    // ================= 서버 연결 =================
    private void connectServer() {
        try {
            socket = new Socket("127.0.0.1", 5001);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String req = in.readLine();
            if (!"ENTER_NAME".equals(req)) {
                throw new IOException("Invalid handshake: " + req);
            }

            out.println(userName);
            connected = true;

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패: " + e.getMessage());
            cleanup();
        }
    }

    // ================= UI =================
    private void buildGUI() {
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JPanel bg = new JPanel(null);
        bg.setBounds(0, 0, 800, 500);
        bg.setBackground(new Color(60, 122, 65));
        add(bg);

        bg.add(createCreateRoomPanel());
        bg.add(createRoomListPanel());
    }

    private JPanel createCreateRoomPanel() {
        JPanel panel = new JPanel(null);
        panel.setBounds(40, 60, 240, 160);
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("방 만들기", SwingConstants.CENTER);
        title.setBounds(0, 10, 240, 25);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        panel.add(title);

        JLabel rt = new JLabel("방 제목 :");
        rt.setBounds(20, 55, 80, 20);
        panel.add(rt);

        roomTitleInput = new JTextField();
        roomTitleInput.setBounds(80, 55, 130, 22);
        panel.add(roomTitleInput);

        JButton createBtn = new JButton("만들기");
        createBtn.setBounds(70, 100, 100, 30);
        createBtn.addActionListener(e -> createRoom());
        panel.add(createBtn);

        return panel;
    }

    private JScrollPane createRoomListPanel() {
        roomListPanel = new JPanel();
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        roomListPanel.setBackground(new Color(0, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(roomListPanel);
        scrollPane.setBounds(330, 40, 430, 400);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    // ================= 로비 동작 =================
    private void createRoom() {
        String title = roomTitleInput.getText().trim();
        if (!title.isEmpty()) {
            sendMessage("CREATE " + title);
            roomTitleInput.setText("");
        }
    }

    private void requestJoinRoom(String roomName) {
        if (enteringRoom) return;
        enteringRoom = true;
        enteringRoomName = roomName; // ★ 핵심
        sendMessage("ENTER_ROOM " + roomName);
    }

    private void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    // ================= 수신 스레드 =================
    private void startReceiveThread() {
        receiveThread = new Thread(this::receiveLoop, "Lobby-Receive");
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    private void receiveLoop() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {

                if (msg.startsWith("ROOM ")) {
                    String roomName = msg.substring(5);
                    if (!rooms.contains(roomName)) rooms.add(roomName);
                    updateRoomList();
                }

                else if (msg.equals("ROOM_END")) {
                    // ignore
                }

                else if (msg.equals("NAME_INVALID")) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                this,
                                "이미 사용 중인 닉네임입니다.",
                                "닉네임 오류",
                                JOptionPane.ERROR_MESSAGE
                        );
                        cleanup();
                    });
                    return;
                }

                else if (msg.startsWith("MSG 방 입장 실패")
                        || msg.startsWith("MSG 이미 방에 입장")) {
                    enteringRoom = false;
                }

                // ===== ★ 핵심 수정 =====
                else if (msg.startsWith("MSG [SYSTEM]") && msg.contains("입장")) {
                    String roomName = enteringRoomName;

                    SwingUtilities.invokeLater(() -> {
                        dispose();
                        try {
                            new Room(roomName, userName, socket);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    return;
                }

                else if (msg.startsWith("MSG ")) {
                    System.out.println(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================= 방 목록 UI =================
    private void updateRoomList() {
        SwingUtilities.invokeLater(() -> {
            roomListPanel.removeAll();

            for (String r : rooms) {
                JPanel roomBox = new JPanel(null);
                roomBox.setPreferredSize(new Dimension(400, 90));
                roomBox.setBackground(Color.WHITE);

                JLabel name = new JLabel("방 제목: " + r);
                name.setBounds(20, 10, 300, 20);
                roomBox.add(name);

                JButton joinBtn = new JButton("참여하기");
                joinBtn.setBounds(140, 45, 130, 30);
                joinBtn.addActionListener(e -> requestJoinRoom(r));
                roomBox.add(joinBtn);

                roomListPanel.add(roomBox);
            }

            roomListPanel.revalidate();
            roomListPanel.repaint();
        });
    }

    private void cleanup() {
        try { socket.close(); } catch (Exception ignored) {}
        dispose();
    }
}
