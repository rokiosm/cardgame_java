package network_game;

import javax.swing.*;
import java.awt.*;

public class Rule extends JFrame {
	
	public static boolean canPlace(Card top, Card next) {
	    int a = top.number;
	    int b = next.number;

	    if (a == 13 && b == 1) return true;
	    if (a == 1 && b == 13) return true;
	    return Math.abs(a - b) == 1;
	}

	
    private JPanel bg;
    private JButton closeBtn;
    private JLabel img;
    private JTextArea text;
    private JScrollPane scroll;

    public Rule() {
        super("룰 설명");
        buildGUI();
        setVisible(true);
    }

    private void buildGUI() {
        setSize(650, 400);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        bg = new JPanel(null);
        bg.setBounds(0, 0, 700, 430);
        bg.setBackground(new Color(36, 73, 39));
        add(bg);

        // 닫기 버튼
        closeBtn = new JButton(new ImageIcon("images/close.png"));
        closeBtn.setBounds(410, 10, 40, 30);
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(e -> dispose());
        bg.add(closeBtn);

        // 중앙 이미지
        img = new JLabel(new ImageIcon("images/rule_image.png"));
        img.setBounds(-5, -7, 470, 380);
        bg.add(img);

        // 룰 텍스트
        text = new JTextArea();
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setText(
                "스피드 카드 게임은 자신이 가지고 있는 카드를 상대방보다 빠르게 없애면 승리하는 게임입니다."
                + "\n\n'내 카드'에 있는 카드를 '중앙 더미'에 내려놓습니다."
                + "\n\n카드를 놓는 규칙은 다음과 같습니다."
                + "\nK-Q-J-10-9-8-7-...-4-3-2-A-K"
                + "\n\n무늬에 상관없이 중앙 더미의 맨 위 카드보다 숫자가 하나 작거나 큰 카드를 내려놓을 수 있습니다."
                + "\n\n만약 상대와 나 모두 내려놓을 수 있는 카드가 없다면, '중앙 더미'의 양 옆 더미 중 하나가 뒤집힙니다."
                + "\n\n양 옆 더미까지 모두 소진되었을 경우 중앙 더미를 섞어 새로 카드를 뒤집습니다."
                + "\n\n상대방보다 먼저 카드를 먼저 소진했을 경우 승리합니다."
        );
        scroll = new JScrollPane(text);
        scroll.setBounds(460, 20, 220, 340);
        bg.add(scroll);
    }
}
