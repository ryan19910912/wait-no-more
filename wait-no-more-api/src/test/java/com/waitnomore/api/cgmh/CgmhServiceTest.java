package com.waitnomore.api.cgmh;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
class CgmhServiceTest {
    static class MutableClock extends Clock {
        Instant now=Instant.parse("2026-10-19T09:00:00Z");
        public ZoneId getZone(){return ZoneOffset.UTC;}
        public Clock withZone(ZoneId zone){return this;}
        public Instant instant(){return now;}
    }
    String html() throws Exception {
        try(var in=getClass().getResourceAsStream("/cgmh-progress.html")) {
            return new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
        }
    }
    @Test void reusesCacheAndMarksFailedRefreshStaleWithoutChangingTimestamp() throws Exception {
        var clock=new MutableClock(); var calls=new AtomicInteger(); String html=html();
        var service=new CgmhService(url->{if(calls.incrementAndGet()>1)throw new java.io.IOException();return html;},clock);
        var first=service.get("1","00","1");
        assertSame(first,service.get("1","00","1")); assertEquals(1,calls.get());
        clock.now=clock.now.plusSeconds(61);
        var stale=service.get("1","00","1");
        assertTrue(stale.stale()); assertEquals(first.fetchedAt(),stale.fetchedAt());
        service.get("1","00","1"); assertEquals(2,calls.get());
    }
    @Test void failedInitialFetchReturns502AndBacksOff() {
        var calls=new AtomicInteger();
        var service=new CgmhService(url->{calls.incrementAndGet();throw new java.io.IOException();},new MutableClock());
        var ex=assertThrows(ResponseStatusException.class,()->service.get("1","00","1"));
        assertEquals(502,ex.getStatusCode().value());
        assertThrows(ResponseStatusException.class,()->service.get("1","00","1"));
        assertEquals(1,calls.get());
    }
}
