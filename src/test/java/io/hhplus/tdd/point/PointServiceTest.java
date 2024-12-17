package io.hhplus.tdd.point;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.hhplus.tdd.database.UserPointTable;

public class PointServiceTest {
    
    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // 테스트별 어노테이션 초기화
    }

    @Test
    @DisplayName(value = "[성공] 포인트 조회 성공")
    void 포인트_조회_성공() throws Exception {
        // given
        final long userId = 1L;
        final UserPoint expectedPoint = new UserPoint(userId, 5000L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(expectedPoint);

        // when
        UserPoint result = pointService.getPoint(userId);

        // then
        assertThat(result).isEqualTo(expectedPoint);
        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    @DisplayName(value = "[실패] 존재하지 않는 사용자 ID를 조회")
    void 존재하지_않는_사용자_ID_조회() throws Exception {
        // given
        final long nonExistUserId = 1L;

        // stub, when
        when(userPointTable.selectById(nonExistUserId)).thenReturn(null);

        // then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.getPoint(nonExistUserId);
        });

        verify(userPointTable, times(1)).selectById(nonExistUserId);
    }
}
