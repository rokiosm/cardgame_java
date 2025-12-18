package network_game;

import javax.swing.*;
import javax.swing.Timer;

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
    
    private long gameStartTime;
    private Timer repaintTimer;
    private static final int GAME_TIME = 30; // seconds

 // 선택된 카드 문자열
    private String selectedCard = null;

    // L/R 선택 대기 중 여부
    private boolean choosingSide = false;


    // ===== 카드 상태 =====
    private final List<String> myHand = new ArrayList<>();

    // CENTER 좌 / 우
    private String centerLeft;
    private String centerRight;

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

    private Image loadCardImage(String card) {
        return cardImages.computeIfAbsent(card, c ->
                loadImage("cardPng/" + c + ".png")
        );
    }

    private Image loadImage(String path) {
        try {
            return new ImageIcon(
                    Objects.requireNonNull(
                            getClass().getClassLoader().getResource(path)
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
    private void drawTimer(Graphics g) {
        if (!gameStarted) return;

        long elapsed = (System.currentTimeMillis() - gameStartTime) / 1000;
        long remain = Math.max(0, GAME_TIME - elapsed);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("TIME : " + remain, getWidth() / 2 - 40, 25);
    }

    public void startGame() {
        if (gameStarted) return;   // ⭐ 중복 방지 핵심

        gameStarted = true;
        gameStartTime = System.currentTimeMillis();

        repaintTimer = new Timer(100, e -> repaint());
        repaintTimer.start();
    }



    public void setHand(String data) {
        myHand.clear();
        resetSelection();

        if (!data.isEmpty())
            myHand.addAll(Arrays.asList(data.split(",")));

        repaint();
    }
    

    // CENTER 메시지 처리
    public void setCenter(String side, String card) {
        if ("L".equals(side))
            centerLeft = "NONE".equals(card) ? null : card;
        else
            centerRight = "NONE".equals(card) ? null : card;

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
    
    private void resetSelection() {
        selectedIndex = -1;
        selectedCard = null;
        choosingSide = false;
        repaint();
    }


    // ===== 카드 클릭 =====
    private void handleClick(int x, int y) {
    	// ===== L / R 선택 단계 =====
    	if (choosingSide && selectedCard != null) {
    	    int yCenter = getHeight() / 2 - CARD_H / 2;

    	    Rectangle left = new Rectangle(
    	        getWidth() / 2 - CARD_W - 20,
    	        yCenter,
    	        CARD_W,
    	        CARD_H
    	    );
    	    Rectangle right = new Rectangle(
    	        getWidth() / 2 + 20,
    	        yCenter,
    	        CARD_W,
    	        CARD_H
    	    );

    	    if (left.contains(x, y)) {
    	        sender.accept("PLAY " + selectedCard + " L");
    	        resetSelection();
    	        return;
    	    }

    	    if (right.contains(x, y)) {
    	        sender.accept("PLAY " + selectedCard + " R");
    	        resetSelection();
    	        return;
    	    }
    	}

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
            	    selectedCard = myHand.get(i);
            	    choosingSide = true;   // 이제 L/R 고르기 단계
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
        drawTimer(g);
    }

    private void drawEnemies(Graphics g) {
        drawBackStack(g, getWidth() / 4 - CARD_W / 2, 30, enemyLeftCount);
        drawBackStack(g, getWidth() * 3 / 4 - CARD_W / 2, 30, enemyRightCount);
    }

    // CENTER 좌 / 우 분리
    private void drawCenter(Graphics g) {
        int y = getHeight() / 2 - CARD_H / 2;

        int leftX = getWidth() / 2 - CARD_W - 20;
        int rightX = getWidth() / 2 + 20;

        if (centerLeft != null)
            drawCard(g, centerLeft, leftX, y, false);

        if (centerRight != null)
            drawCard(g, centerRight, rightX, y, false);

        g.setColor(Color.WHITE);
        g.drawString("L", leftX + CARD_W / 2 - 4, y - 5);
        g.drawString("R", rightX + CARD_W / 2 - 4, y - 5);
        
        if (choosingSide) {
            g.setColor(Color.YELLOW);
            g.drawString(
                "놓을 위치 선택 (L / R)",
                getWidth() / 2 - 70,
                getHeight() / 2 - CARD_H / 2 - 15
            );
        }

    }
    
    public void stopGame() {
        if (repaintTimer != null) {
            repaintTimer.stop();
            repaintTimer = null;
        }
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

        if (img != null)
            g.drawImage(img, x, y, CARD_W, CARD_H, this);

        if (selected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x + 1, y + 1, CARD_W - 3, CARD_H - 3, 12, 12);
        }
    }

    private void drawBackStack(Graphics g, int x, int y, int count) {
        if (count <= 0 || backImage == null) return;

        for (int i = 0; i < Math.min(5, count); i++)
            g.drawImage(backImage, x + i * 5, y + i * 5, CARD_W, CARD_H, this);

        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(count), x + CARD_W / 2 - 4, y + CARD_H / 2);
    }
}
