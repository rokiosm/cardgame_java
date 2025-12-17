package network_game;

class FlyingCard {
    Card card;
    int x, y, tx, ty;
    boolean done = false;

    FlyingCard(Card c, int sx, int sy, int tx, int ty) {
        card = c;
        x = sx; y = sy;
        this.tx = tx; this.ty = ty;
    }

    void update() {
        x += (tx - x) * 0.2;
        y += (ty - y) * 0.2;
        if (Math.abs(x - tx) < 3 && Math.abs(y - ty) < 3)
            done = true;
    }
}
