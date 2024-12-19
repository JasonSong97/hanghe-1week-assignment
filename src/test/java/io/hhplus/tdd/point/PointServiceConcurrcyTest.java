package io.hhplus.tdd.point;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PointServiceConcurrcyTest {
    
    @Autowired private PointService pointService;
}
