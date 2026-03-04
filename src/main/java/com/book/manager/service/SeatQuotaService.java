package com.book.manager.service;

import com.book.manager.dao.SeatReservationSlotQuotaMapper;
import com.book.manager.entity.SeatReservationSlotQuota;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        if (q == null || q.getSlotDate() == null || q.getSlotStart() == null || q.getSlotEnd() == null || !StringUtils.hasText(q.getRoom())) {
            return false;
        }
        if (!q.getSlotEnd().after(q.getSlotStart())) {
            return false;
        }
        if (q.getMaxCount() == null || q.getMaxCount() <= 0) {
            return false;
        }
        q.setRoom(q.getRoom().trim());

        if (q.getId() != null) {
            SeatReservationSlotQuota current = mapper.findById(q.getId());
            if (current == null) {
                return false;
            }

            SeatReservationSlotQuota duplicate = mapper.find(q.getSlotDate(), q.getSlotStart(), q.getSlotEnd(), q.getRoom());
            if (duplicate != null && !q.getId().equals(duplicate.getId())) {
                return false;
            }

            q.setReservedCount(current.getReservedCount());
            return mapper.updateById(q) > 0;
        }

        SeatReservationSlotQuota exist = mapper.find(q.getSlotDate(), q.getSlotStart(), q.getSlotEnd(), q.getRoom());
        if (exist == null) {
            if (q.getReservedCount() == null || q.getReservedCount() < 0) {
                q.setReservedCount(0);
            }
            return mapper.insert(q) > 0;
        }
        q.setId(exist.getId());
        q.setReservedCount(exist.getReservedCount());
        return mapper.updateById(q) > 0;
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

    public boolean incrementIfAvailable(Long id) {
        if (id == null) {
            return false;
        }
        return mapper.incrementReservedIfAvailable(id) > 0;
    }

    public boolean decrementReserved(Long id) {
        if (id == null) {
            return false;
        }
        return mapper.decrementReserved(id) > 0;
    }
}
