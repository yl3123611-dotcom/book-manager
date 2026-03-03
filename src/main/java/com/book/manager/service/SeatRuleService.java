package com.book.manager.service;

import com.book.manager.dao.SeatRuleConfigMapper;
import com.book.manager.entity.SeatRuleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeatRuleService {

    @Autowired
    private SeatRuleConfigMapper mapper;

    public SeatRuleConfig getOrInitDefault() {
        SeatRuleConfig cfg = mapper.get();
        if (cfg != null) return cfg;

        // init default values matching SQL dump defaults
        cfg = new SeatRuleConfig();
        cfg.setFutureDaysLimit(7);
        cfg.setMinDurationMinutes(30);
        cfg.setMaxDurationMinutes(480);
        cfg.setDailyLimitPerUser(1);
        cfg.setActiveLimitPerUser(1);
        cfg.setCheckinGraceMinutes(15);
        cfg.setTempLeaveMaxMinutes(30);
        cfg.setMaxOverlapPerSlot(1);
        cfg.setViolationLimit(3);
        cfg.setBanDays(7);
        mapper.insert(cfg);
        return cfg;
    }

    public boolean save(SeatRuleConfig cfg) {
        SeatRuleConfig exist = mapper.get();
        if (exist == null) {
            mapper.insert(cfg);
            return true;
        }
        cfg.setId(exist.getId());
        return mapper.update(cfg) > 0;
    }
}
