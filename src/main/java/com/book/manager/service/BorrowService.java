package com.book.manager.service;

import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.BorrowMapper;
import com.book.manager.entity.Book;
import com.book.manager.entity.Borrow;
import com.book.manager.entity.Users;
import com.book.manager.repos.BorrowRepository;
import com.book.manager.util.consts.Constants;
import com.book.manager.util.vo.BookOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    /**
     * 查询某个用户的所有借阅记录
     */
    public List<Borrow> findAllBorrowByUserId(Integer userId) {
        // ✅ 改为 MyBatis Mapper（原来 Repository 没这个方法）
        return borrowMapper.findAllBorrowByUserId(userId);
    }

    /**
     * 查询用户某本书的借阅记录
     */
    public Borrow findBorrowByUserIdAndBookId(Integer userId, Integer bookId) {
        return borrowMapper.findBorrowByUserIdAndBookId(userId, bookId);
    }

    /**
     * 查询用户某状态的借阅记录
     */
    public List<Borrow> findBorrowsByUserIdAndRet(Integer userId, Integer ret) {
        return borrowMapper.findBorrowsByUserIdAndRet(userId, ret);
    }

    /**
     * 查询所有未归还借阅记录（用于到期/逾期提醒）
     */
    public List<Borrow> findAllActive() {
        return borrowMapper.findAllActiveBorrows();
    }

    /**
     * 添加借阅
     */
    @Transactional
    public Integer addBorrow(Borrow borrow) {
        Book book = bookService.findBook(borrow.getBookId());
        Users users = userService.findUserById(borrow.getUserId());

        Borrow bor = findBorrowByUserIdAndBookId(users.getId(), book.getId());
        if (bor != null && bor.getRet() != null && bor.getRet() == Constants.NO) {
            return Constants.BOOK_BORROWED;
        }

        if (book.getSize() <= 0) {
            return Constants.BOOK_SIZE_NOT_ENOUGH;
        }
        if (users.getSize() <= 0) {
            return Constants.USER_SIZE_NOT_ENOUGH;
        }

        book.setSize(book.getSize() - 1);
        bookService.updateBook(book);

        users.setSize(users.getSize() - 1);
        userService.updateUser(users);

        borrow.setRet(Constants.NO);
        borrowRepository.save(borrow);

        return Constants.OK;
    }

    /**
     * 归还书籍（保留记录）
     */
    @Transactional
    public boolean retBook(Integer userId, Integer bookId) {
        Borrow borrow = findBorrowByUserIdAndBookId(userId, bookId);
        if (borrow == null || borrow.getRet() == Constants.YES) {
            return false;
        }

        borrow.setRet(Constants.YES);
        borrow.setUpdateTime(new Date());
        borrowRepository.save(borrow);

        Book book = bookService.findBook(bookId);
        book.setSize(book.getSize() + 1);
        bookService.updateBook(book);

        Users user = userService.findUserById(userId);
        user.setSize(user.getSize() + 1);
        userService.updateUser(user);

        return true;
    }

    /**
     * 详情
     */
    public Borrow findById(Integer id) {
        Optional<Borrow> optional = borrowRepository.findById(id);
        return optional.orElse(null);
    }
    /**
     * 编辑借阅记录
     */
    public boolean updateBorrow(Borrow borrow) {
        return borrowMapper.updateBorrow(borrow) > 0;
    }

    public boolean renewBorrow(Integer borrowId) {
        Borrow borrow = borrowMapper.selectByPrimaryKey(borrowId);
        if (borrow == null || borrow.getRet() == 1) {
            return false; // 不存在或已归还
        }
        // 检查是否已经逾期，逾期不能续借
        if (new Date().after(borrow.getEndTime())) {
            return false;
        }

        // 延长30天
        Date newEndTime = DateUtil.offsetDay(borrow.getEndTime(), 30);
        borrow.setEndTime(newEndTime);
        borrowMapper.updateByPrimaryKeySelective(borrow);
        return true;
    }
    /**
     * 按借阅记录ID归还书籍（推荐做法）
     */
    @Transactional
    public boolean retBookByBorrowId(Integer borrowId) {
        if (borrowId == null || borrowId <= 0) {
            return false;
        }

        // 1. 找到借阅记录
        Borrow borrow = borrowRepository.findById(borrowId).orElse(null);
        if (borrow == null || borrow.getRet() == Constants.YES) {
            return false;
        }

        // 2. 标记已归还
        borrow.setRet(Constants.YES);
        borrow.setUpdateTime(new Date());
        borrowRepository.save(borrow);

        // 3. 图书库存 +1
        Book book = bookService.findBook(borrow.getBookId());
        if (book != null) {
            book.setSize(book.getSize() + 1);
            bookService.updateBook(book);
        }

        // 4. 用户可借数量 +1
        Users user = userService.findUserById(borrow.getUserId());
        if (user != null) {
            user.setSize(user.getSize() + 1);
            userService.updateUser(user);
        }

        return true;
    }

}
