package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.hhplus.tdd.LockRegistry;
import io.hhplus.tdd.TddApplication;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

@ExtendWith(SpringExtension.class) // Mockito 확장을 통해 Mockito가 테스트에서 사용할 목업 객체를 주입할 수 있도록 설정
@SpringBootTest(classes = TddApplication.class) // Spring 컨텍스트를 로드해서 통합 테스트 수행
public class PointServiceIntegrationTest {
    
    private PointService pointService;
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private LockRegistry lockRegistry;

    /**
     * AutoWired
     * - 스프링 컨텍스트에서 관리하는 서비스, 리포지토리, 컨트롤러 등을 테스트할 때
     * - 실제 스프링 환경과 유사한 상태에서 통합 테스트를 실행해야 할 때
     * - 의존성 주입을 간단히 처리하려고 할 때
     * 생성자 주입
     * - 테스트 간 독립성이 중요한 경우
     * - 스프링 컨텍스트를 로드하지 않고, 객체를 직접 생성해 테스트 속도를 높이고 싶을 때
     * - 테스트의 주된 관심사가 비즈니스 로직이나 특정 객체의 동작일 때(스프링의 설정이나 DI 기능이 필요하지 않은 경우)
     */
    @BeforeEach
    void setUp() {
        // @AutoWired -> 생성자 주입
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        lockRegistry = new LockRegistry();
        pointService = new PointServiceImpl(pointHistoryTable, userPointTable, lockRegistry);
    }

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
    
    @Test
    @DisplayName(value = "Integration [성공] 포인트 조회에 성공한다.")
    void 포인트_조회_성공케이스() throws Exception {
        // given
        long userId = 1L;
        long amount = 3_000L;

        userPointTable.insertOrUpdate(userId, amount);
    
        // when
        UserPoint result = pointService.findUserPoint(userId);
        
        // then
        assertEquals(userId, result.id());
        assertEquals(amount, result.point());
    }

    @Test
    @DisplayName(value = "Integration [실패] 존재하지 않는 유저가 포인트를 조회하면 실패한다.")
    void 존재하지_않는_유저가_포인트를_조회하면_실패케이스() throws Exception {
        // given
        long existUserId = 1L;
        long existCurrentAmount = 5_000L;
        long nonExistUserId = 999L;

        userPointTable.insertOrUpdate(existUserId, existCurrentAmount);
    
        // when
        Exception result = assertThrows(IllegalArgumentException.class, () -> 
            pointService.findUserPoint(nonExistUserId));
    
        // then
        assertEquals("존재하지 않는 유저입니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
    }

    @Test
    @DisplayName(value = "Integration [성공] 포인트 충전과 포인트 사용내역 조회에 성공한다.")
    void 포인트_충전과_포인트_사용내역_조회에_성공케이스() throws Exception {
        // given
        long userId = 1L;

        pointHistoryTable.insert(userId, 1_000L, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(userId, 1_000L, TransactionType.USE, System.currentTimeMillis());
        pointHistoryTable.insert(userId, 5_000L, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(userId, 3_000L, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(userId, 8_000L, TransactionType.USE, System.currentTimeMillis());
    
        // when
        List<PointHistory> result = pointService.findUserHistory(userId);
        
        // then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(1_000L, result.get(0).amount());
        assertEquals(TransactionType.CHARGE, result.get(0).type());
        assertEquals(1_000L, result.get(1).amount());
        assertEquals(TransactionType.USE, result.get(1).type());
        assertEquals(5_000L, result.get(2).amount());
        assertEquals(TransactionType.CHARGE, result.get(2).type());
        assertEquals(3_000L, result.get(3).amount());
        assertEquals(TransactionType.CHARGE, result.get(3).type());
        assertEquals(8_000L, result.get(4).amount());
        assertEquals(TransactionType.USE, result.get(4).type());
    }

    @Test
    @DisplayName(value = "Integration [실패] 존재하지 않는 유저가 포인트 충전과 포인트 사용내역을 조회하면 실패한다.")
    void 존재하지_않는_유저가_포인트_충전과_포인트_사용내역을_조회하면_실패케이스() throws Exception {
        // given
        long nonExistUserId = 999L;
    
        // when
        Exception result = assertThrows(IllegalArgumentException.class, () -> 
            pointService.findUserHistory(nonExistUserId));
    
        // then
        assertEquals("존재하지 않는 유저입니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
    }

    @Test
    @DisplayName(value = "Integration [성공] 포인트 사용에 성공한다.")
    void 포인트_사용_성공케이스() throws Exception {
        // given
        long userId = 1L;
        long currentPoint = 2_000L;
        long userPoint = 1_000L;

        userPointTable.insertOrUpdate(userId, currentPoint);
    
        // when
        UserPoint result = pointService.useUserPoint(userId, userPoint);
    
        // then
        assertEquals(currentPoint - userPoint, result.point());

        UserPoint PSuserPoint = userPointTable.selectById(userId);
        assertEquals(currentPoint - userPoint, PSuserPoint.point());

        List<PointHistory> PSpointHistoryList = pointHistoryTable.selectAllByUserId(userId);
        assertEquals(userId, PSpointHistoryList.get(0).id());
        assertEquals(userPoint, PSpointHistoryList.get(0).amount());
        assertEquals(TransactionType.USE, PSpointHistoryList.get(0).type());
    }

    @Test
    @DisplayName(value = "Integration [실패] 존재하지 않는 유저가 포인트를 사용하면 실패한다.")
    void 존재하지_않는_유저가_포인트를_사용하면_실패케이스() throws Exception {
        // given
        long existUserId = 1L;
        long existCurrentAmount = 5_000L;
        long nonExistUserId = 999L;
        long nonExistCurrentAmount = 4_000L;

        userPointTable.insertOrUpdate(existUserId, existCurrentAmount);
    
        // when
        Exception result = assertThrows(IllegalArgumentException.class, () ->
            pointService.useUserPoint(nonExistUserId, nonExistCurrentAmount));
    
        // then
        assertEquals("존재하지 않는 유저입니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
    }

    @Test
    @DisplayName(value = "Integration [실패] 유저가 1,000 미만의 포인트를 사용하면 실패한다.")
    void 유저가_1000_미만의_포인트를_사용하면_실패케이스() throws Exception {
        // given
        long userId = 1L;
        long amount = 5_000L;
        long useAmount_1 = 300L;
        long useAmount_2 = -100L;
        
        userPointTable.insertOrUpdate(userId, amount);

        // when
        Exception result_1 = assertThrows(IllegalArgumentException.class, () ->
            pointService.useUserPoint(userId, useAmount_1));
        Exception result_2 = assertThrows(IllegalArgumentException.class, () ->
            pointService.useUserPoint(userId, useAmount_2));
    
        // then
        assertEquals("포인트 사용 금액은 1_000 이상 500_000 이하여야 합니다.", result_1.getMessage());
        assertEquals(IllegalArgumentException.class, result_1.getClass());
        assertEquals("포인트 사용 금액은 1_000 이상 500_000 이하여야 합니다.", result_2.getMessage());
        assertEquals(IllegalArgumentException.class, result_2.getClass());
    }

    @Test
    @DisplayName(value = "Integration [실패] 유저가 500,000 초과의 포인트를 사용하면 실패한다.")
    void 유저가_5000000_초과의_포인트를_충전하면_실패케이스() throws Exception {
        // given
        long userId = 1L;
        long amount = 5_000L;
        long useAmount = 500_001L;
        
        userPointTable.insertOrUpdate(userId, amount);

        // when
        Exception result = assertThrows(IllegalArgumentException.class, () ->
            pointService.useUserPoint(userId, useAmount));
    
        // then
        assertEquals("포인트 사용 금액은 1_000 이상 500_000 이하여야 합니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
    }

    @Test
    @DisplayName(value = "Integration [실패] 유저가 현재 가지고 있는 포인트보다 많은 포인트를 사용하면 실패한다.")
    void 유저가_현재_가지고_있는_포인트보다_많은_포인트를_사용하면_실패케이스() throws Exception {
        // given
        long userId = 1L;
        long currentAmount = 5_000L;
        long useAmount = 5_001L;
        
        userPointTable.insertOrUpdate(userId, currentAmount);
    
        // when
        Exception result = assertThrows(IllegalArgumentException.class, () -> 
            pointService.useUserPoint(userId, useAmount));
    
        // then
        assertEquals("현재 가지고 있는 포인트보다 많이 사용할 수 없습니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
        
        UserPoint PSuserPoint = userPointTable.selectById(userId);
        assertEquals(currentAmount, PSuserPoint.point());
    }
}
