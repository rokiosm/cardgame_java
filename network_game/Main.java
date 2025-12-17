package network_game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    private boolean isReady = false; // 준비 상태
    private JTextArea userList;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("게임 대기실");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // 상단 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(60, 122, 65)); // #3C7A41
        JLabel title = new JLabel("대기실 | 방 제목: 테스트 방");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        topPanel.add(title);
        frame.add(topPanel, BorderLayout.NORTH);

        // 중앙 대기실 패널
        JPanel lobbyPanel = new JPanel(new BorderLayout());
        lobbyPanel.setBackground(Color.WHITE);

        userList = new JTextArea();
        userList.setEditable(false);
        userList.setText("현재 접속 중인 플레이어:\n\n1. Player1\n2. Player2\n3. Player3");
        lobbyPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        // 준비 버튼 + 시작 버튼
        JPanel bottomLobby = new JPanel();
        JButton readyBtn = new JButton("준비");
        JButton startBtn = new JButton("게임 시작");

        readyBtn.setPreferredSize(new Dimension(100, 40));
        startBtn.setPreferredSize(new Dimension(120, 40));

        bottomLobby.add(readyBtn);
        bottomLobby.add(startBtn);
        lobbyPanel.add(bottomLobby, BorderLayout.SOUTH);

        frame.add(lobbyPanel, BorderLayout.CENTER);

        // 우측 채팅 패널
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(250, 0));

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField chatInput = new JTextField();
        JButton sendBtn = new JButton("전송");

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(chatPanel, BorderLayout.EAST);

        // 이벤트 처리

        // 준비 버튼 클릭 → 토글
        readyBtn.addActionListener(e -> {
            isReady = !isReady;
            readyBtn.setText(isReady ? "준비 완료" : "준비");
            updateUserListStatus();
        });

        // 시작 버튼 클릭 → 간단 메시지
        startBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "게임을 시작합니다!");
            // 나중에 서버로 시작 신호 보내는 부분 연결 가능
        });

        // 채팅 전송
        sendBtn.addActionListener(e -> {
            String msg = chatInput.getText().trim();
            if (!msg.isEmpty()) {
                chatArea.append("나: " + msg + "\n");
                chatInput.setText("");
            }
        });

        frame.setVisible(true);
    }

    // 준비 상태 표시 업데이트
    private void updateUserListStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("현재 접속 중인 플레이어:\n\n");
        sb.append("1. Player1 ").append(isReady ? "[준비]" : "").append("\n");
        sb.append("2. Player2 \n");
        sb.append("3. Player3 \n");
        userList.setText(sb.toString());
    }
}
