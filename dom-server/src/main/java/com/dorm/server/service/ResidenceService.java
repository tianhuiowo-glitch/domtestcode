package com.dorm.server.service;

import com.dorm.server.entity.dto.CreateResidenceDTO;
import com.dorm.server.entity.dto.UpdateResidenceDTO;
import com.dorm.server.entity.vo.ResidenceVO;

/**
 * 入居履歴业务接口（新规格 /residences 接口）
 * 映射 residence_histories 表
 *
 * @author dorm-server
 */
public interface ResidenceService {

    /**
     * 查询入居履歴详情
     *
     * @param id 记录ID
     * @return ResidenceVO
     */
    ResidenceVO getResidenceById(Integer id);

    /**
     * 新增入居履歴
     *
     * @param dto 新增参数
     * @return 新增后的 ResidenceVO
     */
    ResidenceVO createResidence(CreateResidenceDTO dto);

    /**
     * 更新入居履歴
     *
     * @param id  记录ID
     * @param dto 更新参数（含version）
     * @return 更新后的 ResidenceVO
     */
    ResidenceVO updateResidence(Integer id, UpdateResidenceDTO dto);

    /**
     * 软删除入居履歴
     *
     * @param id 记录ID
     */
    void deleteResidence(Integer id);
}
