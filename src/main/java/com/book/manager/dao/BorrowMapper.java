package com.book.manager.dao;

import com.book.manager.entity.Borrow;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @Description 借阅管理
 */
@Mapper
public interface BorrowMapper {

    /**
     * 更新借阅记录
     */
    @Update("UPDATE borrow SET user_id = #{userId}, book_id = #{bookId}, end_time = #{endTime}, ret = #{ret}, last_notify = #{lastNotify}, update_time = #{updateTime} WHERE id = #{id}")
    int updateBorrow(Borrow borrow);

    /**
     * 根据用户ID + 图书ID 查询借阅记录
     */
    @Select("SELECT * FROM borrow WHERE user_id = #{userId} AND book_id = #{bookId} LIMIT 1")
    Borrow findBorrowByUserIdAndBookId(@Param("userId") Integer userId,
                                       @Param("bookId") Integer bookId);

    /**
     * 查询某个用户的所有借阅记录
     */
    @Select("SELECT * FROM borrow WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Borrow> findAllBorrowByUserId(@Param("userId") Integer userId);

    /**
     * 查询某个用户指定状态的借阅记录
     * ret：0-未归还，1-已归还
     */
    @Select("SELECT * FROM borrow WHERE user_id = #{userId} AND ret = #{ret} ORDER BY create_time DESC")
    List<Borrow> findBorrowsByUserIdAndRet(@Param("userId") Integer userId,
                                           @Param("ret") Integer ret);

    /**
     * 查询所有未归还借阅记录（ret=0）
     */
    @Select("SELECT * FROM borrow WHERE ret = 0")
    List<Borrow> findAllActiveBorrows();

    /**
     * （保留原有，若其他地方有用）
     */
    int updateBor(Map<String, Object> map);
    /**
     * 根据主键ID查询借阅记录
     */
    Borrow selectByPrimaryKey(Integer id);

    /**
     * 更新借阅记录（只更新非空字段）
     */
    int updateByPrimaryKeySelective(Borrow record);
}
