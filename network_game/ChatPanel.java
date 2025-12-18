package network_game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ChatPanel extends JPanel {
    private JTextPane chatPane;
    private StyledDocument doc;
    private JTextField inputField;
    private JButton sendBtn;
    private JRadioButton allBtn;
    private JRadioButton teamBtn;
    private DefaultListModel<String> userModel;
    private JList<String> userList;
    
    private final Map<String, String> userTeams = new HashMap<>();

    // 닉네임 → 배지 파일명
    private final Map<String, String> userBadges = new HashMap<>();
    private final BiConsumer<String, String> sendHandler;

    public ChatPanel(BiConsumer<String, String> sendHandler) {
        this.sendHandler = sendHandler;
        buildUI();
    }
    
    private ImageIcon loadBadgeIcon(String badgeFile) {
        try {
            Image img = new ImageIcon(
                getClass().getResource("/badge/" + badgeFile)
            ).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            System.out.println("[BADGE LOAD FAIL] " + badgeFile);
            return null;
        }
    }
    
    public void handlePlayerMessage(String msg) {
        // PLAYER name team badge
        String[] p = msg.split(" ", 4);
        if (p.length < 3) return;

        String name = p[1];
        String team = p[2];

        userTeams.put(name, team);
    }


    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));
        setBorder(new EmptyBorder(5, 5, 5, 5));

        // ===== 채팅 영역 =====
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        doc = chatPane.getStyledDocument();
        JScrollPane chatScroll = new JScrollPane(chatPane);
        chatScroll.setPreferredSize(new Dimension(300, 380));
        add(chatScroll, BorderLayout.CENTER);

        // ===== 입력 영역 =====
        inputField = new JTextField();
        sendBtn = new JButton("전송");
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        // ===== 채널 선택 =====
        allBtn = new JRadioButton("전체", true);
        teamBtn = new JRadioButton("팀");
        ButtonGroup group = new ButtonGroup();
        group.add(allBtn);
        group.add(teamBtn);
        JPanel channelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        channelPanel.add(allBtn);
        channelPanel.add(teamBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(channelPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // ===== 유저 리스트 =====
        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(300, 120));
        add(userScroll, BorderLayout.NORTH);

        // ===== 이벤트 =====
        ActionListener sendAction = e -> sendChat();
        sendBtn.addActionListener(sendAction);
        inputField.addActionListener(sendAction);
    }
    public void handleEnter(String nickname, String team, String badge) {

        // ===== 1. 유저 리스트 등록 =====
        if (!userModel.contains(nickname)) {
            userModel.addElement(nickname);
        }

        // ===== 2. 팀 정보 저장 =====
        userTeams.put(nickname, team);

        // ===== 3. 배지 정보 저장 (NONE / null 방어) =====
        if (badge == null || "NONE".equals(badge) || badge.isEmpty()) {
            userBadges.remove(nickname);
        } else {
            userBadges.put(nickname, badge);
        }

        // ===== 4. 입장 메시지 출력 =====
        SwingUtilities.invokeLater(() -> {
            try {
                chatPane.setCaretPosition(doc.getLength());

                // 배지 출력
                if (badge != null && !"NONE".equals(badge)) {
                    ImageIcon icon = loadBadgeIcon(badge);
                    if (icon != null) {
                        chatPane.insertIcon(icon);
                        doc.insertString(doc.getLength(), " ", null);
                    }
                }

                // [닉네임][팀] 입장했습니다.
                doc.insertString(
                        doc.getLength(),
                        "[" + nickname + "][" + team + "] 입장했습니다.\n",
                        null
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



    private void sendChat() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        String channel = teamBtn.isSelected() ? "TEAM" : "ALL";
        sendHandler.accept(channel, text);
        inputField.setText("");
    }

    public void addChatMessage(String raw) {
        SwingUtilities.invokeLater(() -> {
            try {
                chatPane.setCaretPosition(doc.getLength());

                // ===== SYSTEM 메시지 (배지 없음) =====
                if (raw.startsWith("SYSTEM ")) {
                    doc.insertString(
                        doc.getLength(),
                        "[" + raw.substring(7) + "]\n",
                        null
                    );
                    return;
                }
                
                if (raw.startsWith("MSG [SYSTEM]")) {
                    doc.insertString(
                        doc.getLength(),
                        raw.substring(4) + "\n",
                        null
                    );
                    return;
                }


             if (raw.startsWith("CHAT ")) {
                 String[] parts = raw.split(" ", 6);
                 if (parts.length < 6) return;

                 String channel = parts[1];    // ALL / TEAM
                 String nickname = parts[2];
                 String team = parts[3];
                 String badgeFile = parts[4];
                 String text = parts[5];

                 // ===== 배지 =====
                 if (badgeFile != null && !"NONE".equals(badgeFile)) {
                     ImageIcon icon = loadBadgeIcon(badgeFile);
                     if (icon != null) {
                         chatPane.insertIcon(icon);
                         doc.insertString(doc.getLength(), " ", null);
                     }
                 }

                 // ===== [닉네임][팀] =====
                 doc.insertString(
                     doc.getLength(),
                     "[" + nickname + "][" + team + "] ",
                     null
                 );

                 // ===== 메시지 =====
                 doc.insertString(
                     doc.getLength(),
                     text + "\n",
                     null
                 );
                 return;
             }


                // ===== 기타 메시지 =====
                doc.insertString(doc.getLength(), raw + "\n", null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    // 유저 / 배지 등록
    public void addUser(String nickname, String badgeFile) {
        System.out.println("[ADD USER] " + nickname + " badge=" + badgeFile);
        SwingUtilities.invokeLater(() -> {
            userModel.addElement(nickname);
            userBadges.put(nickname, badgeFile);
        });
    }

    public void clearUsers() {
        SwingUtilities.invokeLater(() -> {
            userModel.clear();
            userBadges.clear();
        });
    }
}
