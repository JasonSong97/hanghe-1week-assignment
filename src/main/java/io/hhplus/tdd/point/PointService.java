package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {

    UserPoint chargeUserPoint(long id, long amount);

    UserPoint useUserPoint(long userId, long amount);

    UserPoint findUserPoint(long id);

    List<PointHistory> findUserHistory(long id);
}
