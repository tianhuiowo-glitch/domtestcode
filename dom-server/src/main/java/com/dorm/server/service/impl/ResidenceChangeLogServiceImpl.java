package com.dorm.server.service.impl;

import com.dorm.server.entity.ResidenceChangeLog;
import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.vo.ResidenceChangeLogVO;
import com.dorm.server.mapper.ResidenceChangeLogMapper;
import com.dorm.server.service.ResidenceChangeLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入居履歴変更ログ業務実現クラス
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResidenceChangeLogServiceImpl implements ResidenceChangeLogService {

    private final ResidenceChangeLogMapper residenceChangeLogMapper;

    @Override
    public PageVO<ResidenceChangeLogVO> listChangeLogs(Integer page, Integer pageSize,
                                                       String operationType,
                                                       String from, String to) {
        int offset = (page - 1) * pageSize;
        List<ResidenceChangeLogVO> items = residenceChangeLogMapper.selectPageList(
                operationType, from, to, offset, pageSize);
        Long total = residenceChangeLogMapper.selectPageCount(operationType, from, to);

        log.info("[変更ログ一覧] page={}, operationType={}, total={}", page, operationType, total);
        return PageVO.of(items, total, page, pageSize);
    }

    /**
     * 非同期で変更ログを記録する。主業務フローに影響を与えない。
     */
    @Async
    @Override
    public void recordChange(Integer residenceHistoryId, String operationType, String operatedBy,
                             String residentName, String dormitoryName, String roomName) {
        try {
            ResidenceChangeLog changeLog = new ResidenceChangeLog();
            changeLog.setResidenceHistoryId(residenceHistoryId);
            changeLog.setOperationType(operationType);
            changeLog.setOperatedBy(operatedBy);
            changeLog.setOperatedAt(LocalDateTime.now());
            changeLog.setResidentName(residentName);
            changeLog.setDormitoryName(dormitoryName);
            changeLog.setRoomName(roomName);

            residenceChangeLogMapper.insert(changeLog);
            log.info("[変更ログ記録] residenceHistoryId={}, operationType={}, operatedBy={}",
                    residenceHistoryId, operationType, operatedBy);
        } catch (Exception e) {
            // 変更ログ記録失敗は主業務に影響させない
            log.warn("[変更ログ記録失敗] residenceHistoryId={}, error={}", residenceHistoryId, e.getMessage());
        }
    }
}
