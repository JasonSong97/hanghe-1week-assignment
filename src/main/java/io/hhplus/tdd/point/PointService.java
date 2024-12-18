package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

public interface PointService {

    // private final UserPointTable userPointTable;
    // private final PointHistoryTable pointHistoryTable;

    // public UserPoint getPoint(long userId) {
    //     UserPoint userPoint = userPointTable.selectById(userId);
    //     if (userPoint == null) {
    //         throw new IllegalArgumentException("존재하지 않는 사람압니다.");
    //     }
    //     return userPointTable.selectById(userId);
    // }

    // public List<PointHistory> getUserPointHistory(long userId) {
    //     List<PointHistory> pointHistoryList = pointHistoryTable.selectAllByUserId(userId);
    //     if (pointHistoryList == null) {
    //         throw new IllegalArgumentException("존재하지 않는 사람입니다.");
    //     }
    //     return pointHistoryTable.selectAllByUserId(userId);
    // }
}
