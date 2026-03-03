package com.book.manager.service;

import com.book.manager.dao.SeatReservationSlotQuotaMapper;
import com.book.manager.entity.SeatReservationSlotQuota;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SeatQuotaService {

    @Autowired
    private SeatReservationSlotQuotaMapper mapper;

    public List<SeatReservationSlotQuota> listAll() {
        return mapper.listAll();
    }

    /**
     * 页面/后台保存配额：reservedCount 由系统维护，不允许页面随便改。
     */
    public boolean save(SeatReservationSlotQuota q) {
        if (q.getSlotDate() == null || q.getSlotStart() == null || q.getSlotEnd() == null || q.getRoom() == null) {
            return false;
        }
        if (q.getReservedCount() == null) q.setReservedCount(0);
        if (q.getMaxCount() == null) q.setMaxCount(0);

        SeatReservationSlotQuota exist = mapper.find(q.getSlotDate(), q.getSlotStart(), q.getSlotEnd(), q.getRoom());
        if (exist == null) {
            return mapper.insert(q) > 0;
        }
        q.setId(exist.getId());
        return mapper.update(q) > 0;
    }

    /**
     * 系统内部更新 reservedCount（例如：预约成功 +1，取消预约 -1）。
     */
    public boolean updateReservedCount(Long id, Integer reservedCount) {
        if (id == null || reservedCount == null || reservedCount < 0) {
            return false;
        }
        SeatReservationSlotQuota q = new SeatReservationSlotQuota();
        q.setId(id);
        q.setReservedCount(reservedCount);
        return mapper.updateReservedCount(q) > 0;
    }

    public boolean delete(Long id) {
        return mapper.delete(id) > 0;
    }

    public SeatReservationSlotQuota find(Date slotDate, Date slotStart, Date slotEnd, String room) {
        return mapper.find(slotDate, slotStart, slotEnd, room);
    }
}
