package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        int threadCount = 20;

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

    @Test
    @DisplayName(value = "[동시성테스트] 여러 스레드가 동시에 포인트 사용 요청을 하면 데이터 정합성을 유지한다.")
    void 동시_포인트_사용_테스트() throws Exception {
        // given
        long userId = 1L;
        long amount = 800_000L;
        long useAmount = 3_000L;
        int threadCount = 20;
    
        userPointTable.insertOrUpdate(userId, amount);
    
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
    
        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.useUserPoint(userId, useAmount); // 포인트 사용 요청
                } finally {
                    latch.countDown(); // 스레드 완료 시 latch 감소
                }
            });
        }
    
        latch.await(); // 모든 스레드 작업 완료 대기
        executorService.shutdown();
    
        // then
        UserPoint result = pointService.findUserPoint(userId); // 최종 사용자 포인트 조회
        assertEquals(amount - (threadCount * useAmount), result.point(), "최종 잔고가 모든 사용 요청의 합과 일치해야 합니다.");
    }

    @Test
    @DisplayName(value = "[동시성테스트] 여러 스레드가 동시에 충전 및 사용 요청을 하면 데이터 정합성을 유지한다.")
    void 동시_충전_및_사용_테스트() throws Exception {
        // given
        long userId = 1L;
        long initialAmount = 50_000L; // 초기 잔고
        long chargeAmount = 10_000L; // 충전 요청당 포인트
        long useAmount = 15_000L;    // 사용 요청당 포인트
    
        int threadCount = 20; // 총 스레드 개수 (충전 및 사용 스레드 포함)
        int chargeThreadCount = threadCount / 2; // 절반은 충전 요청
        int useThreadCount = threadCount / 2;    // 나머지 절반은 사용 요청
    
        userPointTable.insertOrUpdate(userId, initialAmount); // 초기 사용자 잔고 설정
    
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
    
        // when
        for (int i = 0; i < chargeThreadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, chargeAmount); // 충전 요청
                } finally {
                    latch.countDown(); // 스레드 작업 완료
                }
            });
        }
    
        for (int i = 0; i < useThreadCount; i++) {
            executorService.submit(() -> {
                try {
                    try {
                        pointService.useUserPoint(userId, useAmount); // 사용 요청
                    } catch (IllegalArgumentException e) {
                        // 사용 요청 실패 시 예외 처리 (잔고 부족 등)
                    }
                } finally {
                    latch.countDown(); // 스레드 작업 완료
                }
            });
        }
    
        latch.await(); // 모든 스레드 작업 완료 대기
        executorService.shutdown();
    
        // then
        UserPoint result = pointService.findUserPoint(userId); // 최종 사용자 포인트 조회
    
        // 순차 처리 결과 예상
        long expectedFinalAmount = calculateExpectedFinalAmount(
            initialAmount,
            chargeAmount * chargeThreadCount,
            useAmount,
            useThreadCount
        );
    
        assertEquals(expectedFinalAmount, result.point(), "최종 잔고가 순차 처리 결과와 동일해야 합니다.");
    }
    
    private long calculateExpectedFinalAmount(long initialAmount, long totalCharged, long useAmount, int useThreadCount) {
        long availableAmount = initialAmount + totalCharged; // 총 사용 가능 포인트
        long totalSuccessfulUses = Math.min(useThreadCount, availableAmount / useAmount); // 최대 성공한 사용 횟수
        return availableAmount - (totalSuccessfulUses * useAmount);
    }
    
}