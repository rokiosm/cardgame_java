package network_game;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class Badge extends JDialog {

    private String selectedBadge;

    public Badge(JFrame parent, String currentBadge) {
        super(parent, "배지 선택", true);
        this.selectedBadge = currentBadge;

        setSize(350, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel imagePanel = new JPanel(new GridLayout(2, 4, 10, 10));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] badgeNames = {
                "bronze.png", "silver.png", "gold.png",
                "platinum.png", "emerald.png", "diamond.png",
                "master.png", "grandmaster.png"
        };

        for (String badge : badgeNames) {
            JButton btn = new JButton();

            URL url = getClass().getResource("/badge/" + badge);
            if (url == null) {
                System.out.println("배지 이미지 로드 실패: " + badge);
                continue;
            }

            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));

            btn.addActionListener(e -> {
                selectedBadge = badge;
                JOptionPane.showMessageDialog(this, badge + " 선택됨");
            });

            imagePanel.add(btn);
        }

        add(imagePanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton okBtn = new JButton("확인");
        JButton cancelBtn = new JButton("닫기");

        okBtn.addActionListener(e -> dispose());
        cancelBtn.addActionListener(e -> dispose());

        bottom.add(cancelBtn);
        bottom.add(okBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    public String getSelectedBadge() {
        return selectedBadge;
    }
}
