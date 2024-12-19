package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hhplus.tdd.database.UserPointTable;

@SpringBootTest
public class PointServiceConcurrcyTest {
    
    @Autowired private PointService pointService;
    @Autowired private UserPointTable userPointTable;

    @Test
    @DisplayName(value = "[동시성테스트] 여러 스레드가 동시에 포인트 충전을 요청하면 데이터 정합성을 유지한다.")
    void 동시_충전_테스트() throws Exception {
        // given
        long userId = 1L;
        long amount = 2_000L;

        long chargeAmount = 1_000L;
        int threadCount = 50; // 50개의 스레드가 동시에 1000 요청

        userPointTable.insertOrUpdate(userId, amount);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount); // 스레드 풀 생성, 스레드 풀은 50개의 스레드를 동시에 생성
        CountDownLatch latch = new CountDownLatch(threadCount); // 스레드 동기화 도구
    
        // when
        for (int count = 0; count < threadCount; count++) {
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, chargeAmount); // 내부적으로 ReetrantLock을 사용해 동시성 문제 방지
                } finally {
                    latch.countDown(); // 각 스레드 작업이 끝날 때마다 latch.countDown()으로 카운트를 줄여 대기를 해제
                }
            });
        }

        latch.await(); // 모든 스레드 완료할 때까지 대기
        executorService.shutdown();        
    
        // then
        UserPoint result = pointService.findUserPoint(userId);
        assertEquals(amount + chargeAmount * threadCount, result.point(), "최종 잔고가 모든 충전 요청의 합과 일치해야 합니다.");
    }
}