package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

@ExtendWith(MockitoExtension.class) // Mockito 확장을 통해 Mockito가 테스트에서 사용할 목업 객체를 주입할 수 있도록 설정
public class PointServiceImplTest {
    
    @Mock private PointHistoryTable pointHistoryTable;
    @Mock private UserPointTable userPointTable;

    @InjectMocks private PointServiceImpl pointServiceImpl; // 목 객체를 주입 받을 구현체, 실제 테스트 대상

    @Test
    @DisplayName(value = "Impl [성공] 포인트 충전에 성공한다.")
    void 포인트_충전_성공케이스() throws Exception {
        // given
        long userId = 1L;
        long currentAmount = 1_000L;
        long chargeAmount = 4_000L;

        UserPoint userPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
        UserPoint updatedPoint = new UserPoint(userId, currentAmount + chargeAmount, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(eq(userId)))
            .thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(eq(userId), eq(currentAmount + chargeAmount)))
            .thenReturn(updatedPoint);
        UserPoint result = pointServiceImpl.chargeUserPoint(userId, chargeAmount);

        // then
        assertNotNull(result);
        assertEquals(currentAmount + chargeAmount, result.point());

        verify(userPointTable, times(1)).selectById(eq(userId));
        verify(userPointTable).insertOrUpdate(eq(userId), eq(currentAmount + chargeAmount));
        verify(pointHistoryTable).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName(value = "Impl [실패] 존재하지 않는 유저가 포인트를 충전하면 실패한다.")
    void 존재하지_않는_유저가_포인트를_충전하면_실패케이스() throws Exception {
        // given
        long userId = 999L;
        long amount = 1_000L;

        // when
        when(userPointTable.selectById(userId))
            .thenReturn(null);
        Exception result = assertThrows(IllegalArgumentException.class, () -> 
            pointServiceImpl.chargeUserPoint(userId, amount));

        // then
        assertEquals("존재하지 않는 유저입니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());

        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName(value = "Impl [실패] 유저가 1,000 미만의 포인트를 충전하면 실패한다.")
    void 유저가_1000_미만의_포인트를_충전하면_실패케이스() throws Exception {
        // given
        long userId_1 = 1L;
        long userId_2 = 2L;
        long userId_3 = 3L;
        long amount_1 = 999L;
        long amount_2 = 0L;
        long amount_3 = -1L;

        UserPoint userPoint_1 = new UserPoint(userId_1, amount_1, System.currentTimeMillis());
        UserPoint userPoint_2 = new UserPoint(userId_2, amount_2, System.currentTimeMillis());
        UserPoint userPoint_3 = new UserPoint(userId_3, amount_3, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(eq(userId_1)))
            .thenReturn(userPoint_1);
        when(userPointTable.selectById(eq(userId_2)))
            .thenReturn(userPoint_2);
        when(userPointTable.selectById(eq(userId_3)))
            .thenReturn(userPoint_3);

        Exception result_1 = assertThrows(IllegalArgumentException.class, () -> 
            pointServiceImpl.chargeUserPoint(userId_1, amount_1));
        Exception result_2 = assertThrows(IllegalArgumentException.class, () -> 
            pointServiceImpl.chargeUserPoint(userId_2, amount_2));
        Exception result_3 = assertThrows(IllegalArgumentException.class, () -> 
            pointServiceImpl.chargeUserPoint(userId_3, amount_3));

        // then
        assertEquals("포인트 충전 금액은 1_000 이상 100_000 이하여야 합니다.", result_1.getMessage());
        assertEquals(IllegalArgumentException.class, result_1.getClass());
        assertEquals("포인트 충전 금액은 1_000 이상 100_000 이하여야 합니다.", result_2.getMessage());
        assertEquals(IllegalArgumentException.class, result_2.getClass());
        assertEquals("포인트 충전 금액은 1_000 이상 100_000 이하여야 합니다.", result_3.getMessage());
        assertEquals(IllegalArgumentException.class, result_3.getClass());

        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName(value = "Impl [실패] 유저가 100,000 초과의 포인트를 충전하면 실패한다.")
    void 유저가_100000_초과의_포인트를_충전하면_실패케이스() throws Exception {
        // given
        long userId_1 = 1L;
        long userId_2 = 2L;
        long amount_1 = 100_001L;
        long amount_2 = 150_000L;

        UserPoint userPoint_1 = new UserPoint(userId_1, amount_1, System.currentTimeMillis());
        UserPoint userPoint_2 = new UserPoint(userId_2, amount_2, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(eq(userId_1)))
            .thenReturn(userPoint_1);
        when(userPointTable.selectById(eq(userId_2)))
            .thenReturn(userPoint_2);

        Exception result_1 = assertThrows(IllegalArgumentException.class, () -> 
            pointServiceImpl.chargeUserPoint(userId_1, amount_1));
        Exception result_2 = assertThrows(IllegalArgumentException.class, () -> 
            pointServiceImpl.chargeUserPoint(userId_2, amount_2));

        // then
        assertEquals("포인트 충전 금액은 1_000 이상 100_000 이하여야 합니다.", result_1.getMessage());
        assertEquals(IllegalArgumentException.class, result_1.getClass());
        assertEquals("포인트 충전 금액은 1_000 이상 100_000 이하여야 합니다.", result_2.getMessage());
        assertEquals(IllegalArgumentException.class, result_2.getClass());

        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName(value = "Impl [성공] 포인트 조회에 성공한다.")
    void 포인트_조회에_성공케이스() throws Exception {
        // given
        long userId = 1L;
        long amount = 1_000L;

        UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(userId))
            .thenReturn(userPoint);
        UserPoint result = pointServiceImpl.findUserPoint(userId);

        // then
        assertEquals(1L, result.id());
        assertEquals(1_000L, result.point());
        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    @DisplayName(value = "Impl [실패] 존재하지 않는 유저가 포인트를 조회하면 실패한다.")
    void 존재하지_않는_유저가_포인트를_조회하면_실패케이스() throws Exception {
        // given
        long userId = 999L;

        // when
        when(userPointTable.selectById(userId))
            .thenReturn(null);
        Exception result = assertThrows(IllegalArgumentException.class, () ->
            pointServiceImpl.findUserPoint(userId));

        // then
        assertEquals("존재하지 않는 유저입니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());

        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    @DisplayName(value = "Impl [성공] 포인트 충전과 포인트 사용내역 조회에 성공한다.")
    void 포인트_충전과_포인트_사용내역_조회에_성공케이스() throws Exception {
        // given
        long userId = 1L;
        List<PointHistory> pointHistoryList = List.of(
            new PointHistory(1L, userId, 3_000L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(2L, userId, 1_000L, TransactionType.USE, System.currentTimeMillis()),
            new PointHistory(3L, userId, 3_000L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(4L, userId, 3_000L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(5L, userId, 5_000L, TransactionType.USE, System.currentTimeMillis())
        );
    
        // when
        when(pointHistoryTable.selectAllByUserId(userId))
            .thenReturn(pointHistoryList);
        List<PointHistory> result = pointServiceImpl.findUserHistory(userId);
        
        // then
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(5_000L, result.get(4).amount());
        assertEquals(TransactionType.CHARGE, result.get(2).type());

        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    @Test
    @DisplayName(value = "Impl [실패] 존재하지 않는 유저가 포인트 충전과 포인트 사용내역을 조회하면 실패한다.")
    void 존재하지_않는_유저가_포인트_충전과_포인트_사용내역을_조회하면_실패케이스() throws Exception {
        // given
        long userId = 999L;
    
        // when
        when(pointHistoryTable.selectAllByUserId(userId))
            .thenReturn(Collections.emptyList());
        Exception result = assertThrows(IllegalArgumentException.class, () ->
            pointServiceImpl.findUserHistory(userId));
        
        // then
        assertEquals("존재하지 않는 유저입니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());

        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    @Test
    @DisplayName(value = "Impl [성공] 포인트 사용에 성공한다.")
    void 포인트_사용_성공케이스() throws Exception {
        // given
        long userId = 1L;
        long amount_1 = 2_000L;
        long useAmount_1 = 1_000L;

        UserPoint userPoint_1 = new UserPoint(userId, amount_1, System.currentTimeMillis());
    
        // when
        when(userPointTable.selectById(userId))
            .thenReturn(userPoint_1);
        UserPoint result = pointServiceImpl.useUserPoint(userId, useAmount_1);
    
        // then
        assertNotNull(result);
        assertEquals(1_000L, result.point());

        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    @DisplayName(value = "Impl [실패] 존재하지 않는 유저가 포인트를 사용하면 실패한다.")
    void 존재하지_않는_유저가_포인트를_사용하면_실패케이스() throws Exception {
        // given
        long userId = 999L;
        long amount = 1_000L;
    
        // when
        when(userPointTable.selectById(userId))
            .thenReturn(null);
        Exception result = assertThrows(IllegalArgumentException.class, () ->
            pointServiceImpl.useUserPoint(userId, amount));
        
        // then
        assertEquals("존재하지 않는 유저입니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
        
        verify(userPointTable, times(1)).selectById(userId);
    }
}
