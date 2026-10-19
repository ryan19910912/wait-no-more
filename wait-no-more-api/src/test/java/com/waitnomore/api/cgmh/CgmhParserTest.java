package com.waitnomore.api.cgmh;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
class CgmhParserTest {
    private String fixture() throws Exception {
        try (var in = getClass().getResourceAsStream("/cgmh-progress.html")) {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
    @Test void parsesOfficialPageWithoutLosingOverdueOrBlankNumbers() throws Exception {
        var p=CgmhParser.parse(fixture(),"1","00","1","https://register.cgmh.org.tw/Progress/1",Instant.EPOCH);
        assertEquals(5,p.clinics().size());
        assertEquals("吳宜鴻",p.clinics().getFirst().doctor());
        assertEquals("",p.clinics().getFirst().currentNumber());
        assertEquals("過號 78",p.clinics().getFirst().nextNumber());
        assertFalse(p.stale());
        assertTrue(p.asStale().stale());
        assertEquals(p.fetchedAt(),p.asStale().fetchedAt());
    }
    @Test void rejectsWrongFiltersAndUnexpectedHtml() throws Exception {
        String html=fixture();
        assertThrows(IllegalStateException.class,()->CgmhParser.parse(html,"1","02","1","url",Instant.EPOCH));
        assertThrows(IllegalStateException.class,()->CgmhParser.parse(html,"3","00","1","url",Instant.EPOCH));
        assertThrows(IllegalStateException.class,()->CgmhParser.parse("<html>maintenance</html>","1","00","1","url",Instant.EPOCH));
    }
    @Test void emptyClinicTableIsValidButMissingColumnsAreNot() throws Exception {
        var doc=org.jsoup.Jsoup.parse(fixture());
        doc.select("#progress-tab-1 table tbody").empty();
        assertTrue(CgmhParser.parse(doc.html(),"1","00","1","url",Instant.EPOCH).clinics().isEmpty());
        doc.select("#progress-tab-1 th").remove();
        assertThrows(IllegalStateException.class,()->CgmhParser.parse(doc.html(),"1","00","1","url",Instant.EPOCH));
    }
    @Test void loadsDepartmentOptionsFromTheSelectedHospital() throws Exception {
        var departments=CgmhParser.departments(fixture(),"1");
        assertTrue(departments.stream().anyMatch(d->d.code().equals("02") && d.name().equals("內科")));
        assertTrue(departments.stream().anyMatch(d->d.code().equals("08") && d.name().equals("中醫")));
        assertThrows(IllegalStateException.class,()->CgmhParser.departments("maintenance","1"));
        assertThrows(IllegalStateException.class,()->CgmhParser.departments(fixture(),"3"));
    }
}
