package network_game;

public class Card {
    public final int number;   // 1~13
    public final char suit;    // 'C','D','H','S'

    public Card(int number, char suit) {
        this.number = number;
        this.suit = suit;
    }

    public static Card fromString(String s) {
        // 예: "11C"
        try {
            int num = Integer.parseInt(s.substring(0, s.length() - 1));
            char suit = s.charAt(s.length() - 1);
            return new Card(num, suit);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid card: " + s);
        }
    }

    @Override
    public String toString() {
        return number + String.valueOf(suit);
    }
}
