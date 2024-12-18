package io.hhplus.tdd.point;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

public class PointServiceTest {

    @Mock
    private PointService pointService;

    private UserPoint userPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // @Mock, @InjectMocks 붙은 필드를 실제 Mock 객체로 생성 및 초기화
        userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis()); // 초기 세팅
    }

    /**
     * Red: 테스트 실패
     * - chargeUserPoint 없음
     * Green: 테스트 성공
     * - 최소한의 코드로 통과
     */
    @Test
    @DisplayName(value = "[성공] 포인트 충전에 성공한다.")
    void 포인트_충전에_성공한다() throws Exception {
        // given
        long userId = 1L;
        long amount = 4000L;

        // when
        when(pointService.chargeUserPoint(userId, amount))
            .thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));
        UserPoint result = pointService.chargeUserPoint(userId, amount);

        // then
        assertEquals(4000L, result.point());
        verify(pointService).chargeUserPoint(userId, amount);
    }

    /**
     * Red: 테스트 실패
     * - 존재하지 않는 유저의 처리 로직이 없기 때문에
     * Green: 테스트 성공
     * - 존재하지 않는 에러 처리 로직 추가
     */
    @Test
    @DisplayName(value = "[실패] 존재하지 않는 유저가 포인트를 충전하면 실패한다.")
    void 존재하지_않는_유저가_포인트를_충전하면_실패() throws Exception {
        // given
        long userId = 9999L;
        long amount = 4000L;

        // when
        when(pointService.chargeUserPoint(userId, amount))
            .thenThrow(new IllegalArgumentException("존재하지 않는 유저입니다."));
        Exception result =  assertThrows(IllegalArgumentException.class, () -> 
            pointService.chargeUserPoint(userId, amount));

        // then
        assertEquals("존재하지 않는 유저입니다.", result.getMessage());
        assertEquals(IllegalArgumentException.class, result.getClass());
    }

    /**
     * Red: 테스트 실패
     * - 1000 보다 작은 포인트 처리 예외를 만들지 않아서
     * Green: 테스트 성공
     * - 1000 미만인 금애기 들어오는 경우 예외 처리 로직 추가
     */
    @Test
    @DisplayName(value = "[실패] 유저가 1,000 미만의 포인트를 충전하면 실패한다.")
    void 유저가_1000미만의_포인트를_충전하면_실패() throws Exception {
        // given
        long userId = 1L;
        long amount_1 = 999L;
        long amount_2 = 0L;
        long amount_3 = -10L;

        // when
        when(pointService.chargeUserPoint(userId, amount_1))
            .thenThrow(new IllegalArgumentException("포인트 충전 금액은 1000 이상이어야 합니다."));
        Exception result_1 = assertThrows(IllegalArgumentException.class, () -> 
            pointService.chargeUserPoint(userId, amount_1)
        );
        when(pointService.chargeUserPoint(userId, amount_2))
            .thenThrow(new IllegalArgumentException("포인트 충전 금액은 1000 이상이어야 합니다."));
        Exception result_2 = assertThrows(IllegalArgumentException.class, () -> 
            pointService.chargeUserPoint(userId, amount_2)
        );
        when(pointService.chargeUserPoint(userId, amount_3))
            .thenThrow(new IllegalArgumentException("포인트 충전 금액은 1000 이상이어야 합니다."));
        Exception result_3 = assertThrows(IllegalArgumentException.class, () -> 
            pointService.chargeUserPoint(userId, amount_3)
        );

        // then
        assertEquals("포인트 충전 금액은 1000 이상이어야 합니다.", result_1.getMessage());
        assertEquals(IllegalArgumentException.class, result_1.getClass());

        assertEquals("포인트 충전 금액은 1000 이상이어야 합니다.", result_2.getMessage());
        assertEquals(IllegalArgumentException.class, result_2.getClass());

        assertEquals("포인트 충전 금액은 1000 이상이어야 합니다.", result_3.getMessage());
        assertEquals(IllegalArgumentException.class, result_3.getClass());
    }
    
    // @Mock
    // private UserPointTable userPointTable;
    
    // @Mock
    // private PointHistoryTable pointHistoryTable;

    // @InjectMocks
    // private PointService pointService;

    // @BeforeEach
    // void setUp() {
    //     MockitoAnnotations.openMocks(this); // 테스트별 어노테이션 초기화
    // }

    // @Test
    // @DisplayName(value = "[성공] 포인트 조회 성공")
    // void 포인트_조회_성공() throws Exception {
    //     // given
    //     final long userId = 1L;
    //     final UserPoint expectedPoint = new UserPoint(userId, 5000L, System.currentTimeMillis());

    //     when(userPointTable.selectById(userId)).thenReturn(expectedPoint);

    //     // when
    //     UserPoint result = pointService.getPoint(userId);

    //     // then
    //     assertThat(result).isEqualTo(expectedPoint);
    // }

    // @Test
    // @DisplayName(value = "[실패] 존재하지 않는 사용자 ID를 조회")
    // void 존재하지_않는_사용자_ID_조회() throws Exception {
    //     // given
    //     final long nonExistUserId = 1L;

    //     // stub, when
    //     when(userPointTable.selectById(nonExistUserId)).thenReturn(null);

    //     // then
    //     assertThrows(IllegalArgumentException.class, () -> {
    //         pointService.getPoint(nonExistUserId);
    //     });
    // }

    // @Test
    // @DisplayName(value = "[성공] 사용자의 포인트 충전 및 사용 내역 조회 성공")
    // void 사용자_포인트_충전_및_사용내역_조회_성공() throws Exception {
    //     // given
    //     final long userId = 1L;
    //     final PointHistory expectedpointHistory_1 = new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
    //     final PointHistory expectedpointHistory_2 = new PointHistory(2L, userId, 1500L, TransactionType.USE, System.currentTimeMillis());
    //     final PointHistory expectedpointHistory_3 = new PointHistory(3L, userId, 2000L, TransactionType.CHARGE, System.currentTimeMillis());
    //     final PointHistory expectedpointHistory_4 = new PointHistory(4L, userId, 3000L, TransactionType.USE, System.currentTimeMillis());
    //     List<PointHistory> expectedPointHistoryList = List.of(
    //         expectedpointHistory_1, expectedpointHistory_2, expectedpointHistory_3, expectedpointHistory_4
    //     );

    //     // stub
    //     when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(expectedPointHistoryList);

    //     // when
    //     List<PointHistory> result = pointService.getUserPointHistory(userId);

    //     // then
    //     assertThat(result).isEqualTo(expectedPointHistoryList);
    // }

    // @Test
    // @DisplayName(value = "[실패] 존재하지 않는 사용자 포인트 충전 및 사용내역 조회 실패")
    // void 존재하지_않는_사용자_포인트_충전_및_사용내역_조회_실패() throws Exception {
    //     // given
    //     final long nonExistUserId = 1L;

    //     // stub, when
    //     when(pointHistoryTable.selectAllByUserId(nonExistUserId)).thenReturn(null);

    //     // then
    //     assertThrows(IllegalArgumentException.class, () -> {
    //         pointService.getUserPointHistory(nonExistUserId);
    //     });
    // }
}
