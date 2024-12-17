package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;

    public UserPoint getPoint(long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);
        if (userPoint == null) {
            throw new IllegalArgumentException("존재하지 않는 사람압니다.");
        }
        return userPointTable.selectById(userId);
    }
}
