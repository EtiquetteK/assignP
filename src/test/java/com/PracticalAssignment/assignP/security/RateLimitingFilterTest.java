package com.PracticalAssignment.assignP.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
    }

    @Test
    void testFilterIsNotNull() {
        assertNotNull(rateLimitingFilter);
    }

    @Test
    void testFilterCanBeInstantiated() {
        RateLimitingFilter filter = new RateLimitingFilter();
        assertNotNull(filter);
    }

    @Test
    void testMultipleFilterInstances() {
        RateLimitingFilter filter1 = new RateLimitingFilter();
        RateLimitingFilter filter2 = new RateLimitingFilter();
        
        assertNotNull(filter1);
        assertNotNull(filter2);
        assertNotSame(filter1, filter2);
    }

    @Test
    void testRateLimitingFilterExtendOncePerRequestFilter() {
        assertTrue(rateLimitingFilter instanceof org.springframework.web.filter.OncePerRequestFilter);
    }

    @Test
    void testRateLimitingFilterIsComponent() {
        // Verify the class has @Component annotation (which makes it a Spring bean)
        assertTrue(rateLimitingFilter.getClass().isAnnotationPresent(
            org.springframework.stereotype.Component.class
        ));
    }
}

