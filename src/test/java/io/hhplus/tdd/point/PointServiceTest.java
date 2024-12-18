package io.hhplus.tdd.point;

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
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

public class PointServiceTest {
    
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
