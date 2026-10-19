package com.waitnomore.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WaitNoMore Worker 服務啟動類
 * 負責執行資料同步、座標補點與診間狀態爬蟲等背景任務
 */
@SpringBootApplication
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
