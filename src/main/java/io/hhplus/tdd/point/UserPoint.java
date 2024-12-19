package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return null;
    }

    public UserPoint increaseUserPoints(long amount) {
        if (amount < 1_000L || amount > 100_000L) {
            throw new IllegalArgumentException("포인트 충전 금액은 1_000 이상 100_000 이하여야 합니다.");
        }
        long newPoint = this.point + amount;
        return new UserPoint(this.id, newPoint, System.currentTimeMillis());
    }

    public UserPoint decreaseUserPoints(long amount) {
        if (amount < 1_000L) {
            throw new IllegalArgumentException("포인트 사용 금액은 1_000 이상 500_000 이하여야 합니다.");
        }
        long newPoint = this.point - amount;
        return new UserPoint(this.id, newPoint, System.currentTimeMillis());
    }
}
