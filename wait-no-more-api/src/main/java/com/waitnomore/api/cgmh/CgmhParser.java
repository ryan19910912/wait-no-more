package com.waitnomore.api.cgmh;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
final class CgmhParser {
    static CgmhProgress parse(String html, String hospital, String dept, String session,
                              String url, Instant fetchedAt) {
        var doc = Jsoup.parse(html);
        Element form = doc.selectFirst("form.progress__controller");
        Element pane = doc.selectFirst("#progress-tab-1");
        if (form == null || pane == null ||
                !selected(form, "dept").equals(dept) || !selected(form, "time").equals(session))
            throw new IllegalStateException("長庚頁面格式或查詢條件不符");
        Element hospitalOption = doc.selectFirst("#changeHospital option[selected][value]");
        if (hospitalOption == null || !hospitalOption.val().equals(hospital))
            throw new IllegalStateException("長庚回傳院區不符");
        Element table = pane.selectFirst("table");
        Element stamp = pane.selectFirst(".table-title span:last-child");
        if (table == null || stamp == null || !table.select("th").text().contains("目前看診號碼"))
            throw new IllegalStateException("無法辨識長庚看診進度表格");
        List<CgmhProgress.Clinic> clinics = new ArrayList<>();
        for (Element row : table.select("tbody > tr")) {
            var cells = row.select("td");
            if (cells.size() != 6) throw new IllegalStateException("長庚看診進度欄位已變更");
            clinics.add(new CgmhProgress.Clinic(cells.get(0).text(), cells.get(1).text(),
                cells.get(2).text(), cells.get(3).text(), cells.get(4).text(), cells.get(5).text()));
        }
        return new CgmhProgress(hospital, dept, session, url, stamp.text(), fetchedAt,
                false, List.copyOf(clinics));
    }
    record Department(String code, String name) {}
    static List<Department> departments(String html, String hospital) {
        var doc = Jsoup.parse(html);
        Element branch = doc.selectFirst("#changeHospital option[selected][value]");
        var options = doc.select("form.progress__controller select[name=dept] option[value]");
        if (branch == null || !branch.val().equals(hospital) || options.isEmpty())
            throw new IllegalStateException("無法辨識院區科別清單");
        return options.stream().filter(e -> e.val().matches("[0-9]{2}") && !e.hasAttr("disabled"))
                .map(e -> new Department(e.val(), e.text())).toList();
    }
    private static String selected(Element form, String name) {
        Element e = form.selectFirst("select[name=" + name + "] option[selected]");
        return e == null ? "" : e.val();
    }
}
