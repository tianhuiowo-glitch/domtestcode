package com.dorm.server.mapper;

import com.dorm.server.entity.ResidenceChangeLog;
import com.dorm.server.entity.vo.ResidenceChangeLogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 入居履歴変更ログ持久层接口
 *
 * @author dorm-server
 */
public interface ResidenceChangeLogMapper {

    /**
     * 新增変更ログ
     *
     * @param log 変更ログ実体
     * @return 影響行数
     */
    Integer insert(ResidenceChangeLog log);

    /**
     * 分页查询変更ログリスト
     *
     * @param operationType 操作タイプ（INSERT/UPDATE/DELETE，null=全部）
     * @param from          開始日時（null=無制限）
     * @param to            終了日時（null=無制限）
     * @param offset        分页偏移量
     * @param pageSize      每页大小
     * @return 変更ログ VO リスト
     */
    List<ResidenceChangeLogVO> selectPageList(@Param("operationType") String operationType,
                                              @Param("from") String from,
                                              @Param("to") String to,
                                              @Param("offset") Integer offset,
                                              @Param("pageSize") Integer pageSize);

    /**
     * 統計分页総数
     *
     * @param operationType 操作タイプ（null=全部）
     * @param from          開始日時（null=無制限）
     * @param to            終了日時（null=無制限）
     * @return 総数
     */
    Long selectPageCount(@Param("operationType") String operationType,
                         @Param("from") String from,
                         @Param("to") String to);
}
