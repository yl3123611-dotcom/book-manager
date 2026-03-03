package com.book.manager.service;

import com.book.manager.dao.ForumPostMapper;
import com.book.manager.dao.ForumReplyMapper;
import com.book.manager.util.ro.PageIn;
import com.book.manager.util.vo.PageOut;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ForumService {

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ForumReplyMapper forumReplyMapper;

    @Transactional
    public boolean addPost(Integer userId, String title, String content) {
        if (userId == null || title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) return false;
        return forumPostMapper.insertPost(userId, title.trim(), content.trim()) > 0;
    }

    public PageOut getPostPage(PageIn pageIn) {
        int curr = pageIn.getCurrPage() == null ? 1 : pageIn.getCurrPage();
        int size = pageIn.getPageSize() == null ? 10 : pageIn.getPageSize();
        PageHelper.startPage(curr, size);
        List<Map<String, Object>> list = forumPostMapper.listPosts(pageIn.getKeyword());
        PageInfo<Map<String, Object>> pi = new PageInfo<>(list);
        PageOut out = new PageOut();
        out.setCurrPage(pi.getPageNum());
        out.setPageSize(pi.getPageSize());
        out.setTotal((int) pi.getTotal());
        out.setList(pi.getList());
        return out;
    }

    public PageOut getAdminPage(PageIn pageIn) {
        int curr = pageIn.getCurrPage() == null ? 1 : pageIn.getCurrPage();
        int size = pageIn.getPageSize() == null ? 20 : pageIn.getPageSize();
        PageHelper.startPage(curr, size);
        List<Map<String, Object>> list = forumPostMapper.adminList(pageIn.getKeyword());
        PageInfo<Map<String, Object>> pi = new PageInfo<>(list);
        PageOut out = new PageOut();
        out.setCurrPage(pi.getPageNum());
        out.setPageSize(pi.getPageSize());
        out.setTotal((int) pi.getTotal());
        out.setList(pi.getList());
        return out;
    }

    public Map<String, Object> getPost(Long id) {
        return forumPostMapper.selectById(id);
    }

    public void incView(Long id) {
        forumPostMapper.incViewCount(id);
    }

    @Transactional
    public boolean addReply(Long postId, Integer userId, String content) {
        if (postId == null || userId == null || content == null || content.trim().isEmpty()) return false;
        int r = forumReplyMapper.insertReply(postId, userId, content.trim());
        if (r > 0) {
            forumPostMapper.incReplyCount(postId);
            return true;
        }
        return false;
    }

    public List<Map<String, Object>> listReplies(Long postId) {
        return forumReplyMapper.listByPost(postId);
    }

    public List<Map<String, Object>> adminList(String keyword) {
        return forumPostMapper.adminList(keyword);
    }

    public boolean updateStatus(Long id, Integer status) {
        return forumPostMapper.updateStatus(id, status) > 0;
    }

    public boolean updatePinned(Long id, Integer isPinned) {
        return forumPostMapper.updatePinned(id, isPinned) > 0;
    }

    public boolean updateFeatured(Long id, Integer isFeatured) {
        return forumPostMapper.updateFeatured(id, isFeatured) > 0;
    }

    @Transactional
    public boolean updateReplyStatus(Long replyId, Integer status) {
        Map<String,Object> old = forumReplyMapper.selectById(replyId);
        if(old == null) return false;
        Long postId = ((Number) old.get("postId")).longValue();

        int n = forumReplyMapper.updateStatus(replyId, status);
        if(n <= 0) return false;

        // recompute and fix reply_count based on visible replies (status=0)
        int visible = forumReplyMapper.countVisibleByPost(postId);
        forumPostMapper.updateReplyCount(postId, visible);
        return true;
    }

    @Transactional
    public boolean recountReplyCount(Long postId) {
        if(postId == null) return false;
        int visible = forumReplyMapper.countVisibleByPost(postId);
        return forumPostMapper.updateReplyCount(postId, visible) > 0;
    }

    public List<Map<String, Object>> listPosts(String keyword) {
        return forumPostMapper.listPosts(keyword);
    }
}
