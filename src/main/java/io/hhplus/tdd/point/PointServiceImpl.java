package io.hhplus.tdd.point;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.LockRegistry;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;
    private final LockRegistry lockRegistry;
    
    @Override
    public UserPoint chargeUserPoint(long userId, long amount) {
        ReentrantLock lock = lockRegistry.getLock(userId); // 각 사용자의 락
        lock.lock(); // 락 획득, 동시성 제어의 핵심, 여러 스레드가 동일한 userId에 대해서 접근 불가능

        try {
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
        } finally {
            lock.unlock(); // 락 해제(필수)
        }
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        ReentrantLock lock = lockRegistry.getLock(userId);
        lock.lock();

        try {
            // 사용자 조회
            UserPoint PSuserPoint = userPointTable.selectById(userId);
            if (PSuserPoint == null) throw new IllegalArgumentException("존재하지 않는 유저입니다.");
            // 조회된 사용자 포인트 감소
            UserPoint updatedUserPoint = PSuserPoint.decreaseUserPoints(amount);
            // 사용내역 저장
            pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
            // 변경된 사용자 업데이트
            userPointTable.insertOrUpdate(updatedUserPoint.id(), updatedUserPoint.point());
            return updatedUserPoint;
        } finally {
            lock.unlock();
        }
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
