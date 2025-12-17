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

    // 🔹 닉네임 → 배지 파일명
    private final Map<String, String> userBadges = new HashMap<>();
    private final BiConsumer<String, String> sendHandler;

    public ChatPanel(BiConsumer<String, String> sendHandler) {
        this.sendHandler = sendHandler;
        buildUI();
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

                // MSG [닉네임][팀] 메시지
                if (raw.startsWith("MSG ")) {
                    String body = raw.substring(4); // "MSG " 제거
                    if (body.startsWith("[")) {
                        int nickEnd = body.indexOf("]");
                        int teamStart = body.indexOf("[", nickEnd);
                        int teamEnd = body.indexOf("]", teamStart);
                        if (nickEnd > 0 && teamStart > 0 && teamEnd > teamStart) {
                            String nickname = body.substring(1, nickEnd);
                            String team = body.substring(teamStart + 1, teamEnd);
                            String text = body.substring(teamEnd + 1).trim();

                            String badgeFile = userBadges.get(nickname);
                            System.out.println("[BADGE] " + nickname + " → " + badgeFile);
                            if (badgeFile != null && !"null".equals(badgeFile)) {
                                ImageIcon icon = new ImageIcon("images/" + badgeFile);
                                Image scaled = icon.getImage()
                                        .getScaledInstance(14, 14, Image.SCALE_SMOOTH);
                                chatPane.insertIcon(new ImageIcon(scaled));
                                doc.insertString(doc.getLength(), " ", null);
                            }

                            doc.insertString(
                                    doc.getLength(),
                                    "[" + nickname + "][" + team + "] " + text + "\n",
                                    null
                            );
                            return;
                        }
                    }
                }

                // 시스템 메시지
                doc.insertString(doc.getLength(), raw + "\n", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ==========================
    // 🔹 유저 / 배지 등록
    // ==========================
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
