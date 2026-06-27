package com.dorm.server.service.impl;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.entity.Room;
import com.dorm.server.entity.common.PageVO;
import com.dorm.server.entity.dto.CreateRoomDTO;
import com.dorm.server.entity.dto.UpdateRoomDTO;
import com.dorm.server.entity.vo.RoomVO;
import com.dorm.server.exception.BusinessException;
import com.dorm.server.mapper.RoomMapper;
import com.dorm.server.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 房间业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;

    @Override
    public PageVO<RoomVO> listRooms(Integer dormitoryId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<RoomVO> items = roomMapper.selectPageList(dormitoryId, offset, pageSize);
        Long total = roomMapper.selectPageCount(dormitoryId);

        log.info("[房间列表] dormitoryId={}, page={}, total={}", dormitoryId, page, total);
        return PageVO.of(items, total, page, pageSize);
    }

    @Override
    public RoomVO getRoomById(Integer id) {
        RoomVO vo = roomMapper.selectVoById(id);
        if (vo == null) {
            throw new BusinessException(MessageConstants.ROOM_NOT_FOUND);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomVO createRoom(CreateRoomDTO dto) {
        log.info("[新增房间] dormitoryId={}, name={}", dto.getDormitoryId(), dto.getName());

        Room room = new Room();
        room.setDormitoryId(dto.getDormitoryId());
        room.setName(dto.getName());
        room.setCapacity(dto.getCapacity());
        room.setVersion(1);

        roomMapper.insert(room);
        return roomMapper.selectVoById(room.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomVO updateRoom(Integer id, UpdateRoomDTO dto) {
        log.info("[更新房间] id={}, version={}", id, dto.getVersion());

        Room existing = roomMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(MessageConstants.ROOM_NOT_FOUND);
        }

        Room room = new Room();
        room.setId(id);
        // 房间不允许跨寮迁移，dormitoryId 始终保持原值
        room.setDormitoryId(existing.getDormitoryId());
        room.setName(dto.getName());
        room.setCapacity(dto.getCapacity());
        room.setVersion(dto.getVersion());

        int affected = roomMapper.updateWithVersion(room);
        if (affected == 0) {
            throw new BusinessException(MessageConstants.VERSION_CONFLICT);
        }

        return roomMapper.selectVoById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Integer id) {
        log.info("[删除房间] id={}", id);

        Room existing = roomMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(MessageConstants.ROOM_NOT_FOUND);
        }

        // 校验是否有在住记录
        Integer activeCount = roomMapper.countActiveResidents(id);
        if (activeCount != null && activeCount > 0) {
            throw new BusinessException(MessageConstants.ROOM_HAS_RESIDENTS);
        }

        roomMapper.softDeleteById(id);
    }

    @Override
    public List<RoomVO> getVacantRooms(Integer dormitoryId, String date) {
        if (dormitoryId == null) {
            return Collections.emptyList();
        }
        // date が未指定の場合は本日日付をデフォルト値として使用する
        String targetDate = (date != null && !date.isEmpty()) ? date : LocalDate.now().toString();
        List<RoomVO> rooms = roomMapper.selectVacantRooms(dormitoryId, targetDate);
        log.info("[空闲房间] dormitoryId={}, date={}, count={}", dormitoryId, targetDate, rooms.size());
        return rooms;
    }
}
