package com.book.manager.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LibraryMapper {

    List<Map<String,Object>> listFloors();

    List<Map<String,Object>> listShelves(@Param("floorId") Integer floorId);

    List<Map<String,Object>> listShelfCells(@Param("shelfId") Integer shelfId);

    Map<String,Object> findBookLocation(@Param("bookId") Integer bookId);

    int bindBookLocation(@Param("bookId") Integer bookId, @Param("floorId") Integer floorId,
                         @Param("shelfId") Integer shelfId, @Param("cellId") Integer cellId);

    // admin CRUD
    int insertFloor(@Param("name") String name, @Param("sort") Integer sort, @Param("mapImageUrl") String mapImageUrl);
    List<Map<String,Object>> adminListFloors();
    int updateFloor(@Param("id") Integer id, @Param("name") String name, @Param("sort") Integer sort, @Param("mapImageUrl") String mapImageUrl, @Param("enabled") Integer enabled);
    int deleteFloor(@Param("id") Integer id);

    int insertShelf(@Param("floorId") Integer floorId, @Param("code") String code, @Param("name") String name, @Param("x") Integer x, @Param("y") Integer y, @Param("orientation") String orientation);
    int updateShelf(@Param("id") Integer id, @Param("code") String code, @Param("name") String name, @Param("x") Integer x, @Param("y") Integer y, @Param("orientation") String orientation, @Param("enabled") Integer enabled);
    int deleteShelf(@Param("id") Integer id);

    int insertCell(@Param("shelfId") Integer shelfId, @Param("layerNo") Integer layerNo, @Param("cellNo") Integer cellNo, @Param("x") Integer x, @Param("y") Integer y);
    int updateCell(@Param("id") Integer id, @Param("layerNo") Integer layerNo, @Param("cellNo") Integer cellNo, @Param("x") Integer x, @Param("y") Integer y);
    int deleteCell(@Param("id") Integer id);

    List<Map<String,Object>> listBooksByShelf(@Param("shelfId") Integer shelfId);
}
