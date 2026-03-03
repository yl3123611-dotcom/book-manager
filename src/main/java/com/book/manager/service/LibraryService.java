package com.book.manager.service;

import com.book.manager.dao.LibraryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LibraryService {

    @Autowired
    private LibraryMapper libraryMapper;

    public List<Map<String, Object>> listFloors(){
        return libraryMapper.listFloors();
    }

    public List<Map<String, Object>> listShelves(Integer floorId){
        return libraryMapper.listShelves(floorId);
    }

    public List<Map<String, Object>> listShelfCells(Integer shelfId){
        return libraryMapper.listShelfCells(shelfId);
    }

    public Map<String,Object> findBookLocation(Integer bookId){
        return libraryMapper.findBookLocation(bookId);
    }

    public boolean bindBookLocation(Integer bookId, Integer floorId, Integer shelfId, Integer cellId){
        return libraryMapper.bindBookLocation(bookId, floorId, shelfId, cellId) > 0;
    }

    public List<Map<String, Object>> adminListFloors(){
        return libraryMapper.adminListFloors();
    }

    public boolean insertFloor(String name, Integer sort, String mapImageUrl){
        return libraryMapper.insertFloor(name, sort, mapImageUrl) > 0;
    }

    public boolean updateFloor(Integer id, String name, Integer sort, String mapImageUrl, Integer enabled){
        return libraryMapper.updateFloor(id, name, sort, mapImageUrl, enabled) > 0;
    }

    public boolean deleteFloor(Integer id){
        return libraryMapper.deleteFloor(id) > 0;
    }

    public boolean insertShelf(Integer floorId, String code, String name, Integer x, Integer y, String orientation){
        return libraryMapper.insertShelf(floorId, code, name, x, y, orientation) > 0;
    }

    public boolean updateShelf(Integer id, String code, String name, Integer x, Integer y, String orientation, Integer enabled){
        return libraryMapper.updateShelf(id, code, name, x, y, orientation, enabled) > 0;
    }

    public boolean deleteShelf(Integer id){
        return libraryMapper.deleteShelf(id) > 0;
    }

    public boolean insertCell(Integer shelfId, Integer layerNo, Integer cellNo, Integer x, Integer y){
        return libraryMapper.insertCell(shelfId, layerNo, cellNo, x, y) > 0;
    }

    public boolean updateCell(Integer id, Integer layerNo, Integer cellNo, Integer x, Integer y){
        return libraryMapper.updateCell(id, layerNo, cellNo, x, y) > 0;
    }

    public boolean deleteCell(Integer id){
        return libraryMapper.deleteCell(id) > 0;
    }

    public List<Map<String,Object>> listBooksByShelf(Integer shelfId){
        return libraryMapper.listBooksByShelf(shelfId);
    }
}
