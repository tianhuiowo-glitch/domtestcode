package com.dorm.server.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * カレンダー表示用 VO
 * 寮 → 部屋 → 在籍者の階層構造と、各在籍者の日次在室フラグ配列を返す
 *
 * @author dorm-server
 */
@Data
public class CalendarDataVO {

    /** 対象年 */
    private Integer year;

    /** 対象月 */
    private Integer month;

    /** 月の日数（日次配列の長さ） */
    private Integer daysInMonth;

    /** 寮一覧 */
    private List<DormitoryVO> dormitories;

    @Data
    public static class DormitoryVO {
        private Integer id;
        private String name;
        private List<RoomVO> rooms;
    }

    @Data
    public static class RoomVO {
        private Integer id;
        private String name;
        private List<ResidentVO> residents;
    }

    @Data
    public static class ResidentVO {
        /** 入居記録ID */
        private Integer id;

        /** 入居者氏名 */
        private String residentName;

        /** 所属名 */
        private String department;

        /** 責任者フラグ */
        private Boolean isResponsible;

        /** 入寮日 */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate checkInDate;

        /** 退寮日（NULL=在籍中） */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate checkOutDate;

        /**
         * 日次在室フラグ配列（長さ = daysInMonth）
         * days[i] = true → i+1 日目は在室
         */
        private List<Boolean> days;

        /** 同室重複エラーフラグ */
        private Boolean hasViolation;

        /** 退寮予定14日以内警告フラグ */
        private Boolean warning;

        /** 警告メッセージ（例：「退寮予定まで3日」） */
        private String warningMessage;

        /** 備考 */
        private String remarks;
    }
}
