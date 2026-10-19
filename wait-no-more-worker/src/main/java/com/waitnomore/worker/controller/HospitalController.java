package com.waitnomore.worker.controller;

import com.waitnomore.worker.service.HospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 醫院資料同步任務控制器
 * 用於 Cloud Scheduler 外部觸發或手動管理任務
 */
@RestController
@RequestMapping("/internal/tasks/hospital")
@Slf4j
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    /**
     * 同步全台醫事機構資料
     * 建議 Cloud Scheduler 設定：0 2 * * 0 (每週日凌晨兩點)
     */
    @PostMapping("/sync")
    public ResponseEntity<String> runHospitalSyncTask() {
        log.info("【外部觸發】開始同步全台醫院資料...");
        long start = System.currentTimeMillis();

        try {
            hospitalService.syncAllHospitals();

            long duration = System.currentTimeMillis() - start;
            String msg = "醫院資料同步完成。總耗時: " + (duration / 1000) + " 秒";
            log.info("【外部觸發】" + msg);
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            log.error("【外部觸發】同步失敗: ", e);
            return ResponseEntity.internalServerError().body("同步失敗: " + e.getMessage());
        }
    }

}
