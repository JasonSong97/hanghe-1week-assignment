package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
}
