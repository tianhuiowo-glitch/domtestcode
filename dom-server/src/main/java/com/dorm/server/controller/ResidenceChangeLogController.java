package com.dorm.server.controller;

import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.vo.ResidenceChangeLogVO;
import com.dorm.server.service.ResidenceChangeLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 入居履歴変更ログ接口 Controller
 * フロントエンド /change-logs ページ对应
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/residence-change-logs")
@RequiredArgsConstructor
public class ResidenceChangeLogController {

    private final ResidenceChangeLogService residenceChangeLogService;

    /**
     * 分页查询入居履歴変更ログ
     * GET /api/v1/residence-change-logs?page=1&pageSize=20&operationType=&from=&to=
     *
     * @param page          页码（デフォルト1）
     * @param pageSize      每页大小（デフォルト20）
     * @param operationType 操作タイプ（INSERT/UPDATE/DELETE，任意）
     * @param from          開始日時（任意，yyyy-MM-dd HH:mm:ss）
     * @param to            終了日時（任意，yyyy-MM-dd HH:mm:ss）
     * @return 変更ログ分页結果
     */
    @GetMapping
    public Result<PageVO<ResidenceChangeLogVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        log.info("[API] GET /api/v1/residence-change-logs, page={}, pageSize={}, operationType={}",
                page, pageSize, operationType);
        PageVO<ResidenceChangeLogVO> result = residenceChangeLogService.listChangeLogs(
                page, pageSize, operationType, from, to);
        return Result.success(result);
    }
}
