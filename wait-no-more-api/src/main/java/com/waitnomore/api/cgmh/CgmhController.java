package com.waitnomore.api.cgmh;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/hospitals/cgmh")
public class CgmhController {
    private final CgmhService service;
    public CgmhController(CgmhService service) { this.service = service; }
    @GetMapping("/branches")
    public Map<String, String> branches() {
        return Map.ofEntries(Map.entry("1", "台北長庚"), Map.entry("2", "基隆長庚"),
            Map.entry("E", "情人湖院區"), Map.entry("B", "長庚診所"), Map.entry("V", "土城醫院"),
            Map.entry("3", "林口長庚"), Map.entry("5", "桃園長庚"), Map.entry("N", "長青院區"),
            Map.entry("M", "雲林長庚"), Map.entry("6", "嘉義長庚"), Map.entry("8", "高雄長庚"), Map.entry("T", "鳳山醫院"));
    }
    @GetMapping("/{hospitalCode}/departments")
    public java.util.List<CgmhParser.Department> departments(@PathVariable String hospitalCode) {
        return service.departments(hospitalCode);
    }
    @GetMapping("/{hospitalCode}/progress")
    public CgmhProgress progress(@PathVariable String hospitalCode,
            @RequestParam String department, @RequestParam String session) {
        return service.get(hospitalCode, department, session);
    }
}
