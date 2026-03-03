package com.book.manager.service;

import com.book.manager.dao.SpecialTopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SpecialTopicService {

    @Autowired
    private SpecialTopicMapper mapper;

    public List<Map<String,Object>> listTopics(){
        return mapper.listTopics();
    }

    public Map<String,Object> findBySlug(String slug){
        return mapper.findBySlug(slug);
    }

    public List<Map<String,Object>> listTopicBooks(Integer topicId){
        return mapper.listTopicBooks(topicId);
    }

    public boolean insertTopic(String slug, String name, String description){
        return mapper.insertTopic(slug, name, description) > 0;
    }

    public boolean addTopicBook(Integer topicId, Integer bookId, Integer sort){
        return mapper.addTopicBook(topicId, bookId, sort) > 0;
    }

    public List<Map<String,Object>> listTopicsByBookId(Integer bookId){
        return mapper.listTopicsByBookId(bookId);
    }

    public boolean removeTopicBook(Integer topicId, Integer bookId){
        return mapper.removeTopicBook(topicId, bookId) > 0;
    }

    public boolean addBookToTopicSlug(String slug, Integer bookId, Integer sort){
        Map<String,Object> topic = mapper.findBySlug(slug);
        if(topic == null) return false;
        Integer tid = (Integer) topic.get("id");
        return mapper.addTopicBook(tid, bookId, sort == null ? 0 : sort) > 0;
    }

    public boolean removeBookFromTopicSlug(String slug, Integer bookId){
        Map<String,Object> topic = mapper.findBySlug(slug);
        if(topic == null) return false;
        Integer tid = (Integer) topic.get("id");
        return mapper.removeTopicBook(tid, bookId) > 0;
    }
}
