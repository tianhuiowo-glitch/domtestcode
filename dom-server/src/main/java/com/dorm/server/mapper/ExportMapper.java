package com.dorm.server.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * CSV エクスポート用 Mapper
 *
 * @author dorm-server
 */
public interface ExportMapper {

    /**
     * 入居履歴エクスポートデータ取得
     *
     * @param regionId 地域ID（null = 全地域）
     * @return 入居履歴行データ
     */
    List<LinkedHashMap<String, Object>> selectResidenceExportData(@Param("regionId") Integer regionId);

    /**
     * 寮費一覧エクスポートデータ取得
     *
     * @param year     対象年（null = 全年）
     * @param month    対象月（null = 全月）
     * @param regionId 地域ID（null = 全地域）
     * @return 寮費行データ
     */
    List<LinkedHashMap<String, Object>> selectFeeExportData(@Param("year") Integer year,
                                                             @Param("month") Integer month,
                                                             @Param("regionId") Integer regionId);
}
