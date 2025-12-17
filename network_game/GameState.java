package network_game;

import java.util.*;

public class GameState {

    // 플레이어 손패
    private final Map<String, Deque<Card>> hands = new HashMap<>();

    // 팀 정보
    private final Map<String, String> teamMap = new HashMap<>();

    // 덱 / 보조 더미
    private final Deque<Card> centerDeck = new ArrayDeque<>();
    private final Deque<Card> sideLeft = new ArrayDeque<>();
    private final Deque<Card> sideRight = new ArrayDeque<>();

    private Card centerTop;
    private String winnerTeam = null;

    public GameState(List<String> players) {

        // 팀 배정 (앞 2명 A, 뒤 2명 B)
        for (int i = 0; i < players.size(); i++) {
            teamMap.put(players.get(i), i < 2 ? "A" : "B");
        }

        // 카드 생성
        List<Card> deck = new ArrayList<>();
        char[] suits = {'C','D','H','S'};
        for (char s : suits)
            for (int n = 1; n <= 13; n++)
                deck.add(new Card(n, s));

        Collections.shuffle(deck);

        // 손패 5장
        for (String p : players) {
            Deque<Card> h = new ArrayDeque<>();
            for (int i = 0; i < 5; i++)
                h.add(deck.remove(0));
            hands.put(p, h);
        }

        // 중앙 / 보조 더미
        centerTop = deck.remove(0);
        sideLeft.push(deck.remove(0));
        sideRight.push(deck.remove(0));

        centerDeck.addAll(deck);
    }

    // 카드 내려놓기
    public synchronized boolean playCard(String player, Card card) {
        Deque<Card> hand = hands.get(player);
        if (hand == null || !hand.contains(card)) return false;
        if (!canPlay(card, centerTop)) return false;

        hand.remove(card);
        centerTop = card;

        if (hand.isEmpty())
            winnerTeam = teamMap.get(player);

        return true;
    }

    // ±1 규칙 (A-K 순환)
    private boolean canPlay(Card c, Card center) {
        int a = c.number;
        int b = center.number;
        if (Math.abs(a - b) == 1) return true;
        return (a == 1 && b == 13) || (a == 13 && b == 1);
    }

    // 보조 더미 뒤집기
    public synchronized boolean flipSide(boolean left) {
        Deque<Card> side = left ? sideLeft : sideRight;
        if (side.isEmpty()) return false;
        centerTop = side.pop();
        return true;
    }

    // ===== 조회 메서드 =====
    public Card getCenterTop() {
        return centerTop;
    }

    public String getHandString(String name) {
        Deque<Card> h = hands.get(name);
        if (h == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Card c : h) sb.append(c).append(",");
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public int getHandCount(String name) {
        Deque<Card> h = hands.get(name);
        return (h == null) ? 0 : h.size();
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
