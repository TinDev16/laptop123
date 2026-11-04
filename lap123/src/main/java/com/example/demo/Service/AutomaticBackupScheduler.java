package com.example.demo.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AutomaticBackupScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(AutomaticBackupScheduler.class);

    private final DatabaseBackupService backupService;

    public AutomaticBackupScheduler(
            DatabaseBackupService backupService
    ) {
        this.backupService = backupService;
    }

    // Tự động sao lưu mỗi 1 phút để kiểm thử
//    @Scheduled(
//            fixedDelay = 60_000,
//            initialDelay = 60_000
//    )

    @Scheduled(
            fixedDelay = 604_800_000L,
            initialDelay = 604_800_000L
    )

    public void tuDongSaoLuu() {

        String thoiGian = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        );

        String tenBanSaoLuu = "Sao lưu tự động "
                + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );

        try {

            logger.info(
                    "Bắt đầu sao lưu database tự động lúc {}",
                    thoiGian
            );

            String fileName = backupService.taoBanSaoLuu(
                    tenBanSaoLuu,
                    "TU_DONG"
            );

            logger.info(
                    "Sao lưu database tự động thành công: {}",
                    fileName
            );

        } catch (Exception e) {

            logger.error(
                    "Sao lưu database tự động thất bại lúc {}",
                    thoiGian,
                    e
            );
        }
    }
}