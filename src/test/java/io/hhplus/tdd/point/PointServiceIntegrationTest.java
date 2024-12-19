package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.hhplus.tdd.TddApplication;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

@ExtendWith(SpringExtension.class) // Mockito 확장을 통해 Mockito가 테스트에서 사용할 목업 객체를 주입할 수 있도록 설정
@SpringBootTest(classes = TddApplication.class) // Spring 컨텍스트를 로드해서 통합 테스트 수행
public class PointServiceIntegrationTest {
    
    @Autowired private PointService pointService;
    @Autowired private UserPointTable userPointTable;
    @Autowired private PointHistoryTable pointHistoryTable;

    @Test
    @DisplayName(value = "Integration [성공] 포인트 충전에 성공한다.")
    void 포인트_충전_성공케이스() throws Exception {
        // given
        long userId = 1L;
        long currentPoint = 1_000L;
        long chargePoint = 2_000L;

        userPointTable.insertOrUpdate(userId, currentPoint);
    
        // when
        UserPoint result = pointService.chargeUserPoint(userId, chargePoint);
    
        // then
        assertEquals(currentPoint + chargePoint, result.point());

        UserPoint PSuserPoint = userPointTable.selectById(userId);
        assertEquals(currentPoint + chargePoint, PSuserPoint.point());

        List<PointHistory> PSpointHistoryList = pointHistoryTable.selectAllByUserId(userId);
        assertEquals(userId, PSpointHistoryList.get(0).id());
        assertEquals(chargePoint, PSpointHistoryList.get(0).amount());
        assertEquals(TransactionType.CHARGE, PSpointHistoryList.get(0).type());
    }

    @Test
    @DisplayName(value = "Integration [실패] 존재하지 않는 유저가 포인트를 충전하면 실패한다.")
    void 존재하지_않는_유저가_포인트를_충전하면_실패케이스() throws Exception {
        // given
        long existUserId = 1L;
        long existCurrentAmount = 5_000L;
        long nonExistUserId = 999L;
        long nonExistCurrentAmount = 4_000L;

        userPointTable.insertOrUpdate(existUserId, existCurrentAmount);
    
        // when
        Exception result = assertThrows(IllegalArgumentException.class, () ->
            pointService.chargeUserPoint(nonExistUserId, nonExistCurrentAmount));
    
        // then
        assertEquals("존재하지 않는 유저입니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
    }

    @Test
    @DisplayName(value = "Integration [실패] 유저가 1,000 미만의 포인트를 충전하면 실패한다.")
    void 유저가_1000_미만의_포인트를_충전하면_실패케이스() throws Exception {
        // given
        long userId = 1L;
        long amount = 5_000L;
        long chargeAmount = 300L;
        
        userPointTable.insertOrUpdate(userId, amount);

        // when
        Exception result = assertThrows(IllegalArgumentException.class, () ->
            pointService.chargeUserPoint(userId, chargeAmount));
    
        // then
        assertEquals("포인트 충전 금액은 1_000 이상 100_000 이하여야 합니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
    }

    @Test
    @DisplayName(value = "Integration [실패] 유저가 100,000 초과의 포인트를 충전하면 실패한다.")
    void 유저가_100000_초과의_포인트를_충전하면_실패케이스() throws Exception {
        // given
        long userId = 1L;
        long amount = 5_000L;
        long chargeAmount = 100_001L;
        
        userPointTable.insertOrUpdate(userId, amount);

        // when
        Exception result = assertThrows(IllegalArgumentException.class, () ->
            pointService.chargeUserPoint(userId, chargeAmount));
    
        // then
        assertEquals("포인트 충전 금액은 1_000 이상 100_000 이하여야 합니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
    }
}
