package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;
    
    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {
        // 사용자 조회
        UserPoint PSuserPoint = userPointTable.selectById(userId);
        if (PSuserPoint == null) throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        // 조회된 사용자 포인트 증가
        UserPoint updatedUserPoint = PSuserPoint.increaseUserPoints(amount);
        // 충전내역 저장
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        // 변경된 사용자 업데이트
        userPointTable.insertOrUpdate(updatedUserPoint.id(), updatedUserPoint.point());
        return updatedUserPoint;
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        // 사용자 조회
        UserPoint PSuserPoint = userPointTable.selectById(userId);
        // 조회된 사용자 포인트 감소
        UserPoint updatedUserPoint = PSuserPoint.decreaseUserPoints(amount);
        // 사용내역 저장
        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
        // 변경된 사용자 업데이트
        userPointTable.insertOrUpdate(updatedUserPoint.id(), updatedUserPoint.point());
        return updatedUserPoint;
    }

    @Override
    public UserPoint findUserPoint(long userId) {
        UserPoint PSuserPoint = userPointTable.selectById(userId);
        if (PSuserPoint == null) throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        return PSuserPoint;
    }

    @Override
    public List<PointHistory> findUserHistory(long userId) {
        List<PointHistory> PSpointHistoryList = pointHistoryTable.selectAllByUserId(userId);
        if (PSpointHistoryList.size() == 0) throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        return PSpointHistoryList;
    }
    
}
