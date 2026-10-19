package com.waitnomore.api.cgmh;
import java.time.Instant;
import java.util.List;
public record CgmhProgress(String hospitalCode, String departmentCode, String session,
        String sourceUrl, String sourceUpdatedAt, Instant fetchedAt, boolean stale,
        List<Clinic> clinics) {
    public record Clinic(String department, String location, String doctor,
            String currentNumber, String nextNumber, String followingNumber) {}
    public CgmhProgress asStale() {
        return new CgmhProgress(hospitalCode, departmentCode, session, sourceUrl,
                sourceUpdatedAt, fetchedAt, true, clinics);
    }
}
