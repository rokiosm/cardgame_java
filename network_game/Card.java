package network_game;

import java.util.Objects;

public class Card {

    public final int number;   // 1~13
    public final char suit;    // 'C','D','H','S'

    // ===== 추가: 카드 공개 여부 =====
    private boolean faceUp = false;

    public Card(int number, char suit) {
        if (number < 1 || number > 13)
            throw new IllegalArgumentException("Invalid card number: " + number);

        if ("CDHS".indexOf(suit) == -1)
            throw new IllegalArgumentException("Invalid card suit: " + suit);

        this.number = number;
        this.suit = suit;
    }

    // ===== 문자열 → 카드 =====
    // 예: "11C"
    public static Card fromString(String s) {
        // 예: "1D", "13S"
        int len = s.length();
        int number = Integer.parseInt(s.substring(0, len - 1));
        char suit = s.charAt(len - 1);
        return new Card(number, suit);
    }

    // ===== 카드 공개 상태 =====
    public boolean isFaceUp() {
        return faceUp;
    }

    public void flipUp() {
        faceUp = true;
    }

    public void flipDown() {
        faceUp = false;
    }

    // ===== 값 비교 (문제 5 핵심) =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card c = (Card) o;
        return number == c.number && suit == c.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, suit);
    }

    @Override
    public String toString() {
        return number + String.valueOf(suit);
    }
}
