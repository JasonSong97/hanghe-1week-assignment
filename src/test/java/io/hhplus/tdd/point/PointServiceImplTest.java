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
}
