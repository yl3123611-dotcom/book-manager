package com.book.manager.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.BookMapper;
import com.book.manager.dao.HomeBannerMapper;
import com.book.manager.dao.HomeRecommendMapper;
import com.book.manager.entity.Book;
import com.book.manager.entity.HomeBanner;
import com.book.manager.entity.HomeRecommend;
import com.book.manager.util.vo.BookOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HomeService {

    @Autowired
    private HomeBannerMapper homeBannerMapper;

    @Autowired
    private HomeRecommendMapper homeRecommendMapper;

    @Autowired
    private BookMapper bookMapper;

    public List<HomeBanner> getEnabledBanners() {
        return homeBannerMapper.listEnabled();
    }

    public List<BookOut> getEnabledRecommends(int size) {
        List<HomeRecommend> recs = homeRecommendMapper.listEnabled();
        if (recs == null || recs.isEmpty()) return Collections.emptyList();

        List<HomeRecommend> take = recs.stream().limit(size).collect(Collectors.toList());
        List<Integer> ids = take.stream().map(HomeRecommend::getBookId).collect(Collectors.toList());
        if (ids.isEmpty()) return Collections.emptyList();

        List<Book> books = bookMapper.selectByIds(ids);
        Map<Integer, Book> map = new HashMap<>();
        for (Book b : books) map.put(b.getId(), b);

        // 按推荐表排序输出
        List<BookOut> outs = new ArrayList<>();
        for (HomeRecommend r : take) {
            Book b = map.get(r.getBookId());
            if (b == null) continue;
            BookOut out = new BookOut();
            BeanUtil.copyProperties(b, out);
            if (b.getPublishTime() != null) {
                out.setPublishTime(DateUtil.format(b.getPublishTime(), "yyyy-MM-dd"));
            }
            outs.add(out);
        }
        return outs;
    }

    // ===== Admin ops =====

    public List<HomeBanner> listAllBanners() {
        return homeBannerMapper.listAll();
    }

    public int saveBanner(HomeBanner banner) {
        if (banner.getEnabled() == null) banner.setEnabled(1);
        if (banner.getSort() == null) banner.setSort(0);
        if (banner.getId() == null) return homeBannerMapper.insert(banner);
        return homeBannerMapper.update(banner);
    }

    public int deleteBanner(Integer id) {
        return homeBannerMapper.deleteById(id);
    }

    public int setBannerEnabled(Integer id, Integer enabled) {
        return homeBannerMapper.setEnabled(id, enabled);
    }

    public List<HomeRecommend> listAllRecs() {
        return homeRecommendMapper.listAll();
    }

    public int addRecommend(Integer bookId) {
        HomeRecommend r = new HomeRecommend();
        r.setBookId(bookId);
        r.setEnabled(1);
        r.setSort(0);
        return homeRecommendMapper.insert(r);
    }

    public int removeRecommend(Integer bookId) {
        return homeRecommendMapper.deleteByBookId(bookId);
    }
}
