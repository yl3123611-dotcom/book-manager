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
    public boolean addPost(Integer userId, String title, String content, String category) {
        if (userId == null || title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) return false;
        String cat = normalizeCategory(category);
        return forumPostMapper.insertPost(userId, title.trim(), content.trim(), cat) > 0;
    }

    public PageOut getPostPage(PageIn pageIn) {
        int curr = pageIn.getCurrPage() == null ? 1 : pageIn.getCurrPage();
        int size = pageIn.getPageSize() == null ? 10 : pageIn.getPageSize();
        PageHelper.startPage(curr, size);
        List<Map<String, Object>> list = forumPostMapper.listPosts(pageIn.getKeyword(), normalizeCategory(pageIn.getCategory()));
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
    public boolean addReply(Long postId, Integer userId, String content, Long parentId) {
        if (postId == null || userId == null || content == null || content.trim().isEmpty()) return false;
        Long rootId = null;
        if (parentId != null && parentId > 0) {
            Map<String, Object> parent = forumReplyMapper.selectById(parentId);
            if (parent == null) {
                return false;
            }
            Number pPostId = (Number) parent.get("postId");
            if (pPostId == null || pPostId.longValue() != postId.longValue()) {
                return false;
            }
            Number pRootId = (Number) parent.get("rootId");
            rootId = (pRootId == null || pRootId.longValue() <= 0) ? parentId : pRootId.longValue();
        }
        int r = forumReplyMapper.insertReply(postId, userId, content.trim(), parentId, rootId);
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
        Map<String, Object> old = forumReplyMapper.selectById(replyId);
        if (old == null) return false;
        Long postId = ((Number) old.get("postId")).longValue();

        int n = forumReplyMapper.updateStatus(replyId, status);
        if (n <= 0) return false;

        int visible = forumReplyMapper.countVisibleByPost(postId);
        forumPostMapper.updateReplyCount(postId, visible);
        return true;
    }

    @Transactional
    public boolean recountReplyCount(Long postId) {
        if (postId == null) return false;
        int visible = forumReplyMapper.countVisibleByPost(postId);
        return forumPostMapper.updateReplyCount(postId, visible) > 0;
    }

    public List<Map<String, Object>> listPosts(String keyword, String category) {
        return forumPostMapper.listPosts(keyword, normalizeCategory(category));
    }

    @Transactional
    public boolean updateMyPost(Long id, Integer userId, String title, String content, String category) {
        if (id == null || userId == null || title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            return false;
        }
        return forumPostMapper.updateMyPost(id, userId, title.trim(), content.trim(), normalizeCategory(category)) > 0;
    }

    @Transactional
    public boolean deleteMyPost(Long id, Integer userId) {
        if (id == null || userId == null) return false;
        return forumPostMapper.deleteMyPost(id, userId) > 0;
    }

    @Transactional
    public boolean reportPost(Long postId, Integer userId, String reason) {
        if (postId == null || userId == null) return false;
        String cleanReason = (reason == null || reason.trim().isEmpty()) ? "疑似违规内容" : reason.trim();
        if (cleanReason.length() > 500) {
            cleanReason = cleanReason.substring(0, 500);
        }
        return forumPostMapper.insertReport(postId, userId, cleanReason) > 0;
    }

    public List<Map<String, Object>> listReports(Integer status, String keyword) {
        return forumPostMapper.listReports(status, keyword);
    }

    @Transactional
    public boolean handleReport(Long id, Integer status, String handleNote, Integer handledBy) {
        if (id == null || status == null || handledBy == null) return false;
        if (status != 1 && status != 2) return false;
        String note = handleNote == null ? "" : handleNote.trim();
        if (note.length() > 500) {
            note = note.substring(0, 500);
        }
        return forumPostMapper.handleReport(id, status, note, handledBy) > 0;
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;
        String c = category.trim();
        if (c.isEmpty() || "all".equalsIgnoreCase(c) || "全部".equals(c)) {
            return null;
        }
        return c;
    }

    public boolean favorite(Long postId, Integer userId, boolean on) {
        if (postId == null || userId == null) return false;
        return on ? forumPostMapper.addFavorite(postId, userId) > 0 : forumPostMapper.removeFavorite(postId, userId) > 0;
    }

    public boolean isFavorited(Long postId, Integer userId) {
        if (postId == null || userId == null) return false;
        return forumPostMapper.existsFavorite(postId, userId) > 0;
    }

    @Transactional
    public boolean acceptReply(Long postId, Long replyId, Integer currentUserId) {
        if (postId == null || replyId == null || currentUserId == null) return false;
        if (forumReplyMapper.existsReplyInPost(postId, replyId) <= 0) return false;
        return forumPostMapper.updateAcceptedReply(postId, replyId, currentUserId) > 0;
    }

    public List<Map<String, Object>> myPosts(Integer userId, String keyword) {
        if (userId == null) return List.of();
        return forumPostMapper.listMyPosts(userId, keyword);
    }

    public List<Map<String, Object>> myFavorites(Integer userId, String keyword) {
        if (userId == null) return List.of();
        return forumPostMapper.listMyFavorites(userId, keyword);
    }
}
