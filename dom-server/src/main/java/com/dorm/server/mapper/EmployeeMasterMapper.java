package com.dorm.server.mapper;

import com.dorm.server.entity.EmployeeMaster;
import com.dorm.server.entity.vo.EmployeeMasterVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社員マスタ持久層接口
 *
 * @author dorm-server
 */
public interface EmployeeMasterMapper {

    /**
     * 氏名キーワードで社員マスタを曖昧検索（コンボボックス用、最大20件）
     *
     * @param keyword 検索キーワード（氏名・社員番号）
     * @return 社員マスタ VO リスト（最大20件）
     */
    List<EmployeeMasterVO> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 社員番号で社員マスタを1件取得
     *
     * @param employeeId 社員番号
     * @return 社員マスタ VO（存在しない場合 null）
     */
    EmployeeMasterVO selectVoByEmployeeId(@Param("employeeId") String employeeId);

    /**
     * 社員番号で実体を1件取得
     *
     * @param employeeId 社員番号
     * @return 社員マスタ実体（存在しない場合 null）
     */
    EmployeeMaster selectByEmployeeId(@Param("employeeId") String employeeId);
}
