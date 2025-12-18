package network_game;

import java.awt.*;
import javax.swing.*;

public class Home extends JFrame {

    // 버튼, 입력 필드, 레이블
    private JButton b_enter, b_setting, b_rule;
    private JTextField t_nickname;
    private JLabel l_nickname;
    
    private String selectedBadge = null; 

    public Home() {
        super("Home");
        buildGUI();

        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(60, 122, 65));
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);

        add(mainPanel);
    }

    // 상단 설정/룰 버튼 패널
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        b_setting = createIconButton("images/setting.png");
        b_setting.addActionListener(e -> {
            Setting setting = new Setting(Home.this, selectedBadge);
            String badge = setting.getSelectedBadge();
            if (badge != null) {
                selectedBadge = badge;
                System.out.println("선택된 배지: " + selectedBadge);
            }
        });
        topPanel.add(b_setting, BorderLayout.WEST);
        b_rule = createIconButton("images/rule.png");
        b_rule.addActionListener(e -> new Rule());
        topPanel.add(b_rule, BorderLayout.EAST);

        return topPanel;
    }

    // 투명 버튼 생성 헬퍼
    private JButton createIconButton(String path) {
        java.net.URL url = getClass().getResource("/" + path);
        if (url == null) {
            System.out.println("아이콘 로드 실패: " + path);
            return new JButton(path);
        }

        JButton btn = new JButton(new ImageIcon(url));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        return btn;
    }


    // 중앙 닉네임 입력 및 로비 버튼 패널
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 70));
        centerPanel.setOpaque(false);

        JPanel whiteBox = new JPanel();
        whiteBox.setBackground(Color.WHITE);
        whiteBox.setPreferredSize(new Dimension(240, 130));
        whiteBox.setLayout(new GridLayout(3, 1));

        whiteBox.add(createNicknameLabelPanel());
        whiteBox.add(createNicknameInputPanel());
        whiteBox.add(createEnterButtonPanel());

        centerPanel.add(whiteBox);
        return centerPanel;
    }

    private JPanel createNicknameLabelPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panel.setOpaque(false);
        l_nickname = new JLabel("닉네임 :");
        panel.add(l_nickname);
        return panel;
    }

    private JPanel createNicknameInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panel.setOpaque(false);
        t_nickname = new JTextField(10);
        panel.add(t_nickname);
        return panel;
    }

    private JPanel createEnterButtonPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        b_enter = new JButton("로비 가기");
        b_enter.setBackground(new Color(255, 190, 0));
        b_enter.setFocusPainted(false);
        b_enter.addActionListener(e -> enterLobby());
        panel.add(b_enter);

        return panel;
    }

    private void enterLobby() {
        String nickname = t_nickname.getText().trim();
        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "닉네임을 입력하세요!");
            return;
        }
        new Lobby(nickname, selectedBadge);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Home::new);
    }
}
