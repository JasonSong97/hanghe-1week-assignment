package io.hhplus.tdd.point;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    
    @Override
    public UserPoint chargeUserPoint(long id, long amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'chargeUserPoint'");
    }

    @Override
    public UserPoint useUserPoint(long userId, long amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'useUserPoint'");
    }

    @Override
    public UserPoint findUserPoint(long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findUserPoint'");
    }

    @Override
    public List<PointHistory> findUserHistory(long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findUserHistory'");
    }
    
}
