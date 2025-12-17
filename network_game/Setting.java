package network_game;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Setting extends JDialog {

    // 멤버 변수
    private JLabel l_nickname, l_bgm, l_effect, l_badge;
    private JTextField t_nickname;
    private JSlider s_bgm, s_effect;
    private JButton b_badge, b_save, b_close;

    private String selectedBadge;
    
    // 생성자
    public Setting(JFrame parent, String currentBadge) {
        super(parent, "설정", true);
        this.selectedBadge=currentBadge;
        buildGUI();
        setBounds(100, 200, 300, 400);
        setVisible(true);
    }

    // GUI 구성
    private void buildGUI() {
        setLayout(new BorderLayout());
        add(createSettingPanel(), BorderLayout.CENTER);
    }

    private JPanel createSettingPanel() {
        JPanel panel = new JPanel(null);

        // 닉네임
        l_nickname = new JLabel("닉네임 변경:");
        l_nickname.setBounds(20, 20, 100, 30);
        panel.add(l_nickname);

        t_nickname = new JTextField();
        t_nickname.setBounds(130, 20, 150, 30);
        panel.add(t_nickname);

        // 배경음악
        l_bgm = new JLabel("배경음악:");
        l_bgm.setBounds(20, 80, 100, 30);
        panel.add(l_bgm);

        s_bgm = new JSlider(0, 100, 50);
        s_bgm.setBounds(130, 80, 150, 40);
        panel.add(s_bgm);

        // 효과음
        l_effect = new JLabel("효과음:");
        l_effect.setBounds(20, 130, 100, 30);
        panel.add(l_effect);

        s_effect = new JSlider(0, 100, 50);
        s_effect.setBounds(130, 130, 150, 40);
        panel.add(s_effect);

        // 배지 
        l_badge = new JLabel("배지:");
        l_badge.setBounds(20, 180, 100, 30);
        panel.add(l_badge);

        b_badge = new JButton("배지 선택");
        b_badge.setBounds(130, 180, 150, 30);
        b_badge.addActionListener(e -> openBadgeDialog());
        panel.add(b_badge);

        // 저장 / 닫기 버튼 
        b_save = new JButton("저장");
        b_save.setBounds(70, 240, 90, 35);
        b_save.addActionListener(e -> saveSettings());
        panel.add(b_save);

        b_close = new JButton("닫기");
        b_close.setBounds(180, 240, 90, 35);
        b_close.addActionListener(e -> dispose());
        panel.add(b_close);

        return panel;
    }

    public String getSelectedBadge() {
        return selectedBadge;
    }
    
    //  이벤트 처리
    private void openBadgeDialog() {
        Badge dialog = new Badge((JFrame) getParent(), selectedBadge);
        dialog.setVisible(true);

        String newBadge = dialog.getSelectedBadge();
        if (newBadge != null) {
            selectedBadge = newBadge;
        }
    }

    private void saveSettings() {
        // 실제 저장 로직 구현 가능
        dispose();
    }
}
