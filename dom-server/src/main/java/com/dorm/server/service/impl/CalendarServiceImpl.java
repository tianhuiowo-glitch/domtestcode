package com.dorm.server.service.impl;

import com.dorm.server.entity.vo.CalendarDataVO;
import com.dorm.server.mapper.ResidenceHistoryMapper;
import com.dorm.server.mapper.RoomMapper;
import com.dorm.server.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

/**
 * カレンダー業務実装クラス
 * 指定年月に在籍中の入居者を寮→部屋→在籍者の階層で返し、
 * 各在籍者に日次在室フラグ配列・重複エラー・退寮警告を付与する
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final ResidenceHistoryMapper residenceHistoryMapper;
    private final RoomMapper roomMapper;

    @Override
    public CalendarDataVO getCalendarData(Integer regionId, Integer year, Integer month, String roomFilter) {
        log.info("[カレンダーデータ] regionId={}, year={}, month={}, roomFilter={}", regionId, year, month, roomFilter);

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();
        LocalDate today = LocalDate.now();

        // 寮ID → DormitoryVO のマップ（挿入順保持）
        Map<Integer, CalendarDataVO.DormitoryVO> dormMap = new LinkedHashMap<>();
        // "dormId:roomId" → RoomVO のマップ（挿入順保持）
        Map<String, CalendarDataVO.RoomVO> roomMap = new LinkedHashMap<>();

        // Step1: 全部屋を先に構築（空室含む）
        List<Map<String, Object>> allRooms = roomMapper.selectAllRoomsForCalendar(regionId);
        for (Map<String, Object> row : allRooms) {
            int dormId = toInt(row.get("dormitory_id"));
            String dormName = str(row.get("dormitory_name"));
            int roomId = toInt(row.get("room_id"));
            String roomName = str(row.get("room_name"));

            dormMap.computeIfAbsent(dormId, id -> {
                CalendarDataVO.DormitoryVO d = new CalendarDataVO.DormitoryVO();
                d.setId(id);
                d.setName(dormName);
                d.setRooms(new ArrayList<>());
                return d;
            });

            String roomKey = dormId + ":" + roomId;
            roomMap.computeIfAbsent(roomKey, k -> {
                CalendarDataVO.RoomVO r = new CalendarDataVO.RoomVO();
                r.setId(roomId);
                r.setName(roomName);
                r.setResidents(new ArrayList<>());
                dormMap.get(dormId).getRooms().add(r);
                return r;
            });
        }

        // Step2: 入居者を部屋に紐付け
        List<Map<String, Object>> rows = residenceHistoryMapper.selectCalendarResidents(regionId, year, month);

        for (Map<String, Object> row : rows) {
            int dormId = toInt(row.get("dormitory_id"));
            int roomId = toInt(row.get("room_id"));

            String roomKey = dormId + ":" + roomId;
            CalendarDataVO.RoomVO room = roomMap.get(roomKey);
            if (room == null) continue;

            LocalDate checkIn = toLocalDate(row.get("check_in_date"));
            LocalDate checkOut = toLocalDate(row.get("check_out_date"));
            LocalDate plannedCheckout = toLocalDate(row.get("planned_checkout_date"));

            CalendarDataVO.ResidentVO resident = new CalendarDataVO.ResidentVO();
            resident.setId(toInt(row.get("id")));
            resident.setResidentName(str(row.get("resident_name")));
            resident.setDepartment(str(row.get("department_name")));
            resident.setIsResponsible(toBoolean(row.get("is_responsible")));
            resident.setCheckInDate(checkIn);
            resident.setCheckOutDate(checkOut);
            resident.setRemarks(str(row.get("remarks")));

            // 日次在室フラグ配列
            List<Boolean> days = new ArrayList<>(daysInMonth);
            for (int d = 1; d <= daysInMonth; d++) {
                LocalDate day = firstDay.withDayOfMonth(d);
                boolean present = checkIn != null
                        && !day.isBefore(checkIn)
                        && (checkOut == null || !day.isAfter(checkOut));
                days.add(present);
            }
            resident.setDays(days);

            // 退寮予定14日以内警告（確定退室日 or 予定退室日の近い方を使用）
            resident.setWarning(false);
            LocalDate effectiveCheckout = checkOut != null ? checkOut : plannedCheckout;
            if (effectiveCheckout != null) {
                long daysUntil = ChronoUnit.DAYS.between(today, effectiveCheckout);
                if (daysUntil >= 0 && daysUntil <= 14) {
                    resident.setWarning(true);
                    resident.setWarningMessage("退寮予定まで" + daysUntil + "日");
                }
            }

            resident.setHasViolation(false);
            room.getResidents().add(resident);
        }

        // 同室重複エラー：同日に2名以上在室している部屋の在籍者にフラグを立てる
        for (CalendarDataVO.RoomVO room : roomMap.values()) {
            List<CalendarDataVO.ResidentVO> residents = room.getResidents();
            if (residents.size() < 2) continue;
            for (int d = 0; d < daysInMonth; d++) {
                int overlap = 0;
                for (CalendarDataVO.ResidentVO r : residents) {
                    if (Boolean.TRUE.equals(r.getDays().get(d))) overlap++;
                }
                if (overlap >= 2) {
                    for (CalendarDataVO.ResidentVO r : residents) {
                        if (Boolean.TRUE.equals(r.getDays().get(d))) r.setHasViolation(true);
                    }
                }
            }
        }

        // Step3: roomFilter に基づいて部屋をフィルタリング（Java 層で実施、SQL 変更なし）
        // all または未指定の場合はフィルタリングしない
        String filter = StringUtils.hasText(roomFilter) ? roomFilter.trim().toLowerCase() : "all";
        List<CalendarDataVO.DormitoryVO> dormitories;
        if ("vacant".equals(filter)) {
            // 空室のみ：residents が空の部屋だけ残す、全部屋が除外された寮も除外
            dormitories = new ArrayList<>();
            for (CalendarDataVO.DormitoryVO dorm : dormMap.values()) {
                List<CalendarDataVO.RoomVO> vacantRooms = new ArrayList<>();
                for (CalendarDataVO.RoomVO room : dorm.getRooms()) {
                    if (room.getResidents().isEmpty()) {
                        vacantRooms.add(room);
                    }
                }
                if (!vacantRooms.isEmpty()) {
                    CalendarDataVO.DormitoryVO filteredDorm = new CalendarDataVO.DormitoryVO();
                    filteredDorm.setId(dorm.getId());
                    filteredDorm.setName(dorm.getName());
                    filteredDorm.setRooms(vacantRooms);
                    dormitories.add(filteredDorm);
                }
            }
        } else if ("occupied".equals(filter)) {
            // 入居中のみ：residents が1名以上の部屋だけ残す、全部屋が除外された寮も除外
            dormitories = new ArrayList<>();
            for (CalendarDataVO.DormitoryVO dorm : dormMap.values()) {
                List<CalendarDataVO.RoomVO> occupiedRooms = new ArrayList<>();
                for (CalendarDataVO.RoomVO room : dorm.getRooms()) {
                    if (!room.getResidents().isEmpty()) {
                        occupiedRooms.add(room);
                    }
                }
                if (!occupiedRooms.isEmpty()) {
                    CalendarDataVO.DormitoryVO filteredDorm = new CalendarDataVO.DormitoryVO();
                    filteredDorm.setId(dorm.getId());
                    filteredDorm.setName(dorm.getName());
                    filteredDorm.setRooms(occupiedRooms);
                    dormitories.add(filteredDorm);
                }
            }
        } else if ("error".equals(filter)) {
            // 重複エラーのみ：hasViolation が true の居住者がいる部屋だけ残す
            dormitories = new ArrayList<>();
            for (CalendarDataVO.DormitoryVO dorm : dormMap.values()) {
                List<CalendarDataVO.RoomVO> errorRooms = new ArrayList<>();
                for (CalendarDataVO.RoomVO room : dorm.getRooms()) {
                    boolean hasError = room.getResidents().stream()
                            .anyMatch(r -> Boolean.TRUE.equals(r.getHasViolation()));
                    if (hasError) {
                        errorRooms.add(room);
                    }
                }
                if (!errorRooms.isEmpty()) {
                    CalendarDataVO.DormitoryVO filteredDorm = new CalendarDataVO.DormitoryVO();
                    filteredDorm.setId(dorm.getId());
                    filteredDorm.setName(dorm.getName());
                    filteredDorm.setRooms(errorRooms);
                    dormitories.add(filteredDorm);
                }
            }
        } else if ("warning".equals(filter)) {
            // 退寮14日以内警告のある部屋のみ
            dormitories = new ArrayList<>();
            for (CalendarDataVO.DormitoryVO dorm : dormMap.values()) {
                List<CalendarDataVO.RoomVO> warningRooms = new ArrayList<>();
                for (CalendarDataVO.RoomVO room : dorm.getRooms()) {
                    boolean hasWarning = room.getResidents().stream()
                            .anyMatch(r -> Boolean.TRUE.equals(r.getWarning()));
                    if (hasWarning) {
                        warningRooms.add(room);
                    }
                }
                if (!warningRooms.isEmpty()) {
                    CalendarDataVO.DormitoryVO filteredDorm = new CalendarDataVO.DormitoryVO();
                    filteredDorm.setId(dorm.getId());
                    filteredDorm.setName(dorm.getName());
                    filteredDorm.setRooms(warningRooms);
                    dormitories.add(filteredDorm);
                }
            }
        } else {
            // all：フィルタリングなし
            dormitories = new ArrayList<>(dormMap.values());
        }

        CalendarDataVO vo = new CalendarDataVO();
        vo.setYear(year);
        vo.setMonth(month);
        vo.setDaysInMonth(daysInMonth);
        vo.setDormitories(dormitories);
        return vo;
    }

    // ── ユーティリティ ────────────────────────────────────────────

    private int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(o.toString());
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private boolean toBoolean(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).intValue() != 0;
        return "1".equals(o.toString()) || "true".equalsIgnoreCase(o.toString());
    }

    private LocalDate toLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate) return (LocalDate) o;
        if (o instanceof Date) return ((Date) o).toLocalDate();
        if (o instanceof java.util.Date) return ((java.util.Date) o).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        try {
            return LocalDate.parse(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
