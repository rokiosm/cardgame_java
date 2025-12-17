package network_game;

public class GameResult {

    public enum Type {
        OK,
        WIN,
        INVALID
    }

    public final Type type;
    public final Card card;
    public final int remain;
    public final String winner;

    private GameResult(Type type, Card card, int remain, String winner) {
        this.type = type;
        this.card = card;
        this.remain = remain;
        this.winner = winner;
    }

    public static GameResult ok(Card card, int remain) {
        return new GameResult(Type.OK, card, remain, null);
    }

    public static GameResult win(String player) {
        return new GameResult(Type.WIN, null, 0, player);
    }

    public static GameResult invalid() {
        return new GameResult(Type.INVALID, null, -1, null);
    }
}


