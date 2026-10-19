package com.waitnomore.api.cgmh;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class CgmhService {
    static final Set<String> HOSPITALS = Set.of("1", "2", "E", "B", "V", "3", "5", "N", "M", "6", "8", "T");
    @FunctionalInterface interface Fetcher { String fetch(String url) throws Exception; }
    private final Fetcher fetcher;
    private final java.time.Clock clock;
    public CgmhService() {
        this(CgmhService::fetchPage, java.time.Clock.systemUTC());
    }
    CgmhService(Fetcher fetcher, java.time.Clock clock) {
        this.fetcher = fetcher;
        this.clock = clock;
    }
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static String fetchPage(String url) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15))
                .header("User-Agent", "WaitNoMore/1.0 (clinic progress query)").GET().build();
        var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        if (response.statusCode() != 200) throw new IllegalStateException("HTTP " + response.statusCode());
        return response.body();
    }
    private final Map<String, Entry> cache = new ConcurrentHashMap<>();
    private record Entry(Instant attemptedAt, CgmhProgress value, boolean failed) {}
    // Serializes refreshes so concurrent callers cannot multiply requests to the source.
    public synchronized CgmhProgress get(String hospital, String dept, String session) {
        if (!HOSPITALS.contains(hospital) || (dept == null || !dept.matches("[0-9]{2}")) || !Set.of("1", "2", "3").contains(session))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支援的院區、科別或午別");
        String key = hospital + ":" + dept + ":" + session;
        Entry old = cache.get(key);
        Instant now = clock.instant();
        if (old != null && Duration.between(old.attemptedAt(), now).getSeconds() < 60) {
            if (old.value() == null) throw unavailable();
            return old.failed() ? old.value().asStale() : old.value();
        }
        String url = "https://register.cgmh.org.tw/Progress/" + hospital + "?dept=" + dept + "&time=" + session;
        try {
            var result = CgmhParser.parse(fetcher.fetch(url), hospital, dept, session, url, clock.instant());
            cache.put(key, new Entry(now, result, false));
            return result;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            CgmhProgress previous = old == null ? null : old.value();
            cache.put(key, new Entry(now, previous, true));
            if (previous != null) return previous.asStale();
            throw unavailable();
        }
    }
    private record DepartmentEntry(Instant fetchedAt, java.util.List<CgmhParser.Department> values) {}
    private final Map<String, DepartmentEntry> departmentCache = new ConcurrentHashMap<>();
    public synchronized java.util.List<CgmhParser.Department> departments(String hospital) {
        if (!HOSPITALS.contains(hospital))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支援的院區");
        var old = departmentCache.get(hospital);
        Instant now = clock.instant();
        if (old != null && Duration.between(old.fetchedAt(), now).toMinutes() < 60) return old.values();
        try {
            var result = CgmhParser.departments(fetcher.fetch("https://register.cgmh.org.tw/Progress/" + hospital), hospital);
            departmentCache.put(hospital, new DepartmentEntry(now, result));
            return result;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw unavailable();
        }
    }
    private ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "長庚看診進度暫時無法取得，請稍後重試");
    }
}
