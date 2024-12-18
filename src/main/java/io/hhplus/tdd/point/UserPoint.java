package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint increaseUserPoints(long amount) {
        long newPoint = this.point + amount;
        return new UserPoint(this.id, newPoint, System.currentTimeMillis());
    }
}
