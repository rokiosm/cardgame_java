package network_game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class GamePanel extends JPanel {

    // ===== 카드 이미지 =====
    private final Map<String, Image> cardImages = new HashMap<>();
    private Image backImage;

    // ===== 서버 송신 =====
    private final Consumer<String> sender;

    // ===== 내 정보 =====
    private final String myName;
    private String myTeam;
    private boolean gameStarted = false;

    // ===== 플레이어 구성 =====
    private String teammateName;
    private String enemyLeftName;
    private String enemyRightName;

    // ===== 카드 상태 =====
    private final List<String> myHand = new ArrayList<>();
    private final Deque<String> center = new ArrayDeque<>();

    // 카드 개수
    private int teammateCount = 0;
    private int enemyLeftCount = 0;
    private int enemyRightCount = 0;
    private int sideLeftCount = 0;
    private int sideRightCount = 0;

    // ===== UI 상수 =====
    private static final int CARD_W = 80;
    private static final int CARD_H = 120;
    private static final int CARD_OVERLAP = 30;

    // 선택된 카드
    private int selectedIndex = -1; 

    public GamePanel(String myName, Consumer<String> sender) {
        this.myName = myName;
        this.sender = sender;

        setBackground(new Color(40, 120, 40));
        loadImages();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameStarted)
                    handleClick(e.getX(), e.getY());
            }
        });
    }
    
    

    // ===== 이미지 로딩 =====
    private void loadImages() {
        backImage = loadImage("cardPng/card_back.png");
    }

    //  핵심 수정 부분
    private Image loadCardImage(String card) {
        return cardImages.computeIfAbsent(card, c ->
                loadImage("cardPng/" + c + ".png")
        );
    }


    private Image loadImage(String path) {
        try {
            return new ImageIcon(
                    Objects.requireNonNull(
                            getClass().getClassLoader().getResource(path) //안 배운부분 
                    )
            ).getImage();
        } catch (Exception e) {
            System.out.println("이미지 로드 실패: " + path);
            return null;
        }
    }
    
    

    // ===== 서버 메시지 =====
    public void handlePlayer(String msg) {
        String[] p = msg.split(" ");
        String name = p[1];
        String team = p[2];

        if (name.equals(myName)) {
            myTeam = team;
            return;
        }
        if (myTeam == null) return;

        if (team.equals(myTeam))
            teammateName = name;
        else if (enemyLeftName == null)
            enemyLeftName = name;
        else
            enemyRightName = name;
    }

    public void startGame() {
        gameStarted = true;
        repaint();
    }

    public void setHand(String data) {
        myHand.clear();
        selectedIndex = -1;

        if (!data.isEmpty())
            myHand.addAll(Arrays.asList(data.split(",")));

        repaint();
    }

    public void setCenter(String data) {
        center.clear();
        if (!data.equals("NONE") && !data.isEmpty())
            center.push(data);
        repaint();
    }

    public void setCountsFromMessage(String data) {
        String[] p = data.split(" ");
        teammateCount = Integer.parseInt(p[0]);
        enemyLeftCount = Integer.parseInt(p[1]);
        enemyRightCount = Integer.parseInt(p[2]);
        sideLeftCount = Integer.parseInt(p[3]);
        sideRightCount = Integer.parseInt(p[4]);
        repaint();
    }

    // ===== 카드 클릭 =====
    private void handleClick(int x, int y) {
        int startX = getWidth() / 2 - (myHand.size() * CARD_OVERLAP) / 2;
        int yPos = getHeight() - CARD_H - 30;

        for (int i = myHand.size() - 1; i >= 0; i--) {
            Rectangle r = new Rectangle(
                    startX + i * CARD_OVERLAP,
                    yPos,
                    CARD_W,
                    CARD_H
            );

            if (r.contains(x, y)) {
                if (selectedIndex == i) {
                    sender.accept("PLAY " + myHand.get(i));
                    selectedIndex = -1;
                } else {
                    selectedIndex = i;
                }
                repaint();
                return;
            }
        }
    }

    // ===== 렌더링 =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawEnemies(g);
        drawCenter(g);
        drawSideDecks(g);
        drawTeammate(g);
        drawMyHand(g);
    }

    private void drawEnemies(Graphics g) {
        drawBackStack(g, getWidth() / 4 - CARD_W / 2, 30, enemyLeftCount);
        drawBackStack(g, getWidth() * 3 / 4 - CARD_W / 2, 30, enemyRightCount);
    }

    private void drawCenter(Graphics g) {
        int x = getWidth() / 2 - CARD_W / 2;
        int y = getHeight() / 2 - CARD_H / 2;
        for (String c : center)
            drawCard(g, c, x, y, false);
    }

    private void drawSideDecks(Graphics g) {
        int y = getHeight() / 2 - CARD_H / 2;
        drawBackStack(g, 60, y, sideLeftCount);
        drawBackStack(g, getWidth() - 60 - CARD_W, y, sideRightCount);
    }

    private void drawTeammate(Graphics g) {
        drawBackStack(
                g,
                getWidth() / 4 - CARD_W / 2,
                getHeight() - CARD_H - 40,
                teammateCount
        );
    }

    private void drawMyHand(Graphics g) {
        int x = getWidth() / 2 - (myHand.size() * CARD_OVERLAP) / 2;
        int y = getHeight() - CARD_H - 30;

        for (int i = 0; i < myHand.size(); i++) {
            boolean selected = (i == selectedIndex);
            drawCard(g, myHand.get(i), x, y, selected);
            x += CARD_OVERLAP;
        }
    }

    private void drawCard(Graphics g, String card, int x, int y, boolean selected) {
        Image img = loadCardImage(card);

        if (img != null) {
            g.drawImage(img, x, y, CARD_W, CARD_H, this);
        } else {
            g.setColor(Color.WHITE);
            g.fillRoundRect(x, y, CARD_W, CARD_H, 12, 12);
            g.setColor(Color.BLACK);
            g.drawString(card, x + 10, y + 25);
        }

        if (selected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x + 1, y + 1, CARD_W - 3, CARD_H - 3, 12, 12);
        }
    }

    private void drawBackStack(Graphics g, int x, int y, int count) {
        if (count <= 0 || backImage == null) return;

        for (int i = 0; i < Math.min(5, count); i++) {
            g.drawImage(backImage, x + i * 5, y + i * 5, CARD_W, CARD_H, this);
        }

        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(count), x + CARD_W / 2 - 4, y + CARD_H / 2);
    }
}
