package io.hhplus.tdd;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component // 스프링 빈으로 등록해서 싱글톤으로 관리, 따라서 모든 요청은 동일한 인스턴스 사용, 락 관리 중앙 집중화
public class LockRegistry {
    
    // ConcurrentHashMap<Long, ReentrantLock>: userId로 ReentrantLock 객체를 값으로 저장하는 맵
    // 동기화된 방식으로 데이터를 관리
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public ReentrantLock getLock(long userId) {
        // computeIfAbsent()를 사용해 userId에 대한 락이 없는 경우 새로 생성, 이미 존재하면 기존 락 반환
        // 사용자별 고유한 락이 생성되고 재사용
        return lockMap.computeIfAbsent(userId, id -> new ReentrantLock());
    }
}

/**
 * ReetrantLock
 * - 동기화와 스레드 안전성을 보장하기 위해 사용하는 객체
 * - 고급 잠금 매커니즘
 * - 동시성 문제가 발생할 가능성이 있는 코드 영역을 보호하기 위해서 사용
 * 
 * ConcurrentHashMap<Long, ReentrantLock>
 * - 이렇게 관리하는 이유는 사용자별로 고유한 락을 부여하기 위해
 * - 또한 동시에 여러 사용자에 대한 요청이 들어와도 서로 간섭하지 않도록 만들려고
 * - Long(사용자 ID), 사용자별로 생성된 ReetrantLock 객체 -> 사용자 ID에만 영향을 미치는 세분화된 락 관리가 가능
 * - computeIfAbsent(): 필요한 경우에만 새로운 ReetrantLock 객체를 생성
 *   - 락이 이미 존재하면 재사용
 *   - 락이 없으면 새로운 락 생
 *   - 메모리 낭비 방지, 필요한 만큼의 ReetrantLock 객체 생성
 */