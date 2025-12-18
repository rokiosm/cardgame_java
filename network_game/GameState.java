package network_game;

import java.util.*;

public class GameState {

    // ===== 플레이어 데이터 =====
    private final Map<String, Deque<Card>> hands = new HashMap<>();
    private final Map<String, Deque<Card>> personalDecks = new HashMap<>();
    private final Map<String, String> teamMap = new HashMap<>();

    // ===== 중앙 카드 (2장) =====
    private Card centerLeft;
    private Card centerRight;

    // ===== 보조 더미 =====
    private final Deque<Card> sideLeft = new ArrayDeque<>();
    private final Deque<Card> sideRight = new ArrayDeque<>();

    private String winnerTeam = null;

    public GameState(List<String> players) {

        // 팀 배정 (앞 2명 A, 뒤 2명 B)
        for (int i = 0; i < players.size(); i++) {
            teamMap.put(players.get(i), i < 2 ? "A" : "B");
        }

        // ===== 카드 2덱 생성 =====
        List<Card> deck = new ArrayList<>();
        char[] suits = {'C', 'D', 'H', 'S'};

        for (int d = 0; d < 2; d++) {
            for (char s : suits)
                for (int n = 1; n <= 13; n++)
                    deck.add(new Card(n, s));
        }

        Collections.shuffle(deck);

        // ===== 손패 5장 =====
        for (String p : players) {
            Deque<Card> h = new ArrayDeque<>();
            for (int i = 0; i < 5; i++)
                h.add(deck.remove(0));
            hands.put(p, h);
        }

        // ===== 개인 더미 18장 =====
        for (String p : players) {
            Deque<Card> pd = new ArrayDeque<>();
            for (int i = 0; i < 18; i++)
                pd.push(deck.remove(0));
            personalDecks.put(p, pd);
        }

        // ===== 중앙 카드 2장 =====
        centerLeft = deck.remove(0);
        centerRight = deck.remove(0);

        // ===== 보조 더미 =====
        for (int i = 0; i < 5; i++) sideLeft.push(deck.remove(0));
        for (int i = 0; i < 5; i++) sideRight.push(deck.remove(0));
    }

    // ==========================
    // 카드 플레이
    // side = "L" or "R"
    // ==========================
    public synchronized boolean playCard(String player, Card card, String side) {

        Deque<Card> hand = hands.get(player);
        if (hand == null || !hand.contains(card)) return false;

        Card center = side.equals("L") ? centerLeft : centerRight;
        if (!canPlay(card, center)) return false;

        // 카드 내려놓기
        hand.remove(card);
        if (side.equals("L")) centerLeft = card;
        else centerRight = card;

        // 손패 보충
        Deque<Card> pd = personalDecks.get(player);
        if (hand.size() < 5 && !pd.isEmpty())
            hand.add(pd.pop());

        // 승리 조건
        if (hand.isEmpty() && pd.isEmpty())
            winnerTeam = teamMap.get(player);

        return true;
    }
    
    public synchronized String judgeByTimeOver() {
        int teamACount = 0;
        int teamBCount = 0;

        for (String p : hands.keySet()) {
            int count =
                    getHandCount(p) +
                    getPersonalDeckCount(p);

            if ("A".equals(teamMap.get(p)))
                teamACount += count;
            else
                teamBCount += count;
        }

        if (teamACount < teamBCount) return "A";
        if (teamACount > teamBCount) return "B";
        return "DRAW";
    }

    // ±1 규칙 (A-K 순환)
    private boolean canPlay(Card c, Card center) {
        int a = c.number;
        int b = center.number;
        if (Math.abs(a - b) == 1) return true;
        return (a == 1 && b == 13) || (a == 13 && b == 1);
    }

    // ==========================
    // 보조 더미 뒤집기
    // ==========================
    public synchronized boolean flipSide(boolean left) {
        Deque<Card> side = left ? sideLeft : sideRight;
        if (side.isEmpty()) return false;

        if (left) centerLeft = side.pop();
        else centerRight = side.pop();
        return true;
    }

    // ==========================
    // 조회 메서드
    // ==========================
    public Card getCenterLeft() {
        return centerLeft;
    }

    public Card getCenterRight() {
        return centerRight;
    }

    public String getHandString(String name) {
        Deque<Card> h = hands.get(name);
        if (h == null || h.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Card c : h) sb.append(c).append(",");
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public int getHandCount(String name) {
        Deque<Card> h = hands.get(name);
        return h == null ? 0 : h.size();
    }

    public int getPersonalDeckCount(String name) {
        Deque<Card> pd = personalDecks.get(name);
        return pd == null ? 0 : pd.size();
    }

    public int getSideLeftCount() {
        return sideLeft.size();
    }

    public int getSideRightCount() {
        return sideRight.size();
    }

    public boolean isFinished() {
        return winnerTeam != null;
    }

    public String getWinnerTeam() {
        return winnerTeam;
    }
}
