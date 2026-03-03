package com.book.manager.controller;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.book.manager.entity.Borrow;
import com.book.manager.service.BookService;
import com.book.manager.service.BorrowService;
import com.book.manager.service.UserActionService;
import com.book.manager.util.R;
import com.book.manager.util.consts.Constants;
import com.book.manager.util.http.CodeEnum;
import com.book.manager.util.vo.BackOut;
import com.book.manager.util.vo.BookOut;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Tag(name = "借阅管理")
@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserActionService userActionService;

    @Operation(summary = "借阅列表")
    @GetMapping("/list")
    public R getBorrowList(@RequestParam Integer userId) {
        if (userId == null || userId <= 0) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }
        return R.success(CodeEnum.SUCCESS, borrowService.findAllBorrowByUserId(userId));
    }

    /**
     * ✅ 修复：归还页面需要“书信息 + 借阅信息”，不能只返回 Borrow
     * 前端调用：GET /borrow/borrowed?userId=xx
     */
    @Operation(summary = "已借阅列表（未归还，用于归还页面，带书信息）")
    @GetMapping("/borrowed")
    public R borrowed(@RequestParam Integer userId) {
        if (userId == null || userId <= 0) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }

        List<BackOut> outs = new ArrayList<>();

        // 未归还借阅记录
        List<Borrow> borrows = borrowService.findBorrowsByUserIdAndRet(userId, Constants.NO);
        for (Borrow borrow : borrows) {
            BackOut backOut = new BackOut();

            // 借阅记录ID（如果 BackOut 有该字段）
            try {
                backOut.setBorrowId(borrow.getId());
            } catch (Exception ignored) {
            }

            // 注入书籍信息（书名/作者/ISBN/出版社/分类...）
            BookOut book = bookService.findBookById(borrow.getBookId());
            if (book != null) {
                BeanUtils.copyProperties(book, backOut);
            }

            // 借阅时间/应还时间（格式化成 yyyy-MM-dd）
            if (borrow.getCreateTime() != null) {
                backOut.setBorrowTime(DateUtil.format(borrow.getCreateTime(), Constants.DATE_FORMAT));
            }
            if (borrow.getEndTime() != null) {
                backOut.setEndTime(DateUtil.format(borrow.getEndTime(), Constants.DATE_FORMAT));
            }

            // 是否逾期
            Date now = new Date();
            if (borrow.getEndTime() != null && now.after(borrow.getEndTime())) {
                backOut.setLate(Constants.YES_STR);
            } else {
                backOut.setLate(Constants.NO_STR);
            }

            outs.add(backOut);
        }

        return R.success(CodeEnum.SUCCESS, outs);
    }

    @Operation(summary = "借阅图书")
    @PostMapping("/add")
    public R addBorrow(@RequestBody Borrow borrow) {
        if (borrow == null || borrow.getUserId() == null || borrow.getBookId() == null) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }

        Integer result = borrowService.addBorrow(borrow);
        if (result == Constants.BOOK_BORROWED) {
            return R.success(CodeEnum.BOOK_BORROWED);
        } else if (result == Constants.USER_SIZE_NOT_ENOUGH) {
            return R.success(CodeEnum.USER_NOT_ENOUGH);
        } else if (result == Constants.BOOK_SIZE_NOT_ENOUGH) {
            return R.success(CodeEnum.BOOK_NOT_ENOUGH);
        }

        // 成功后写入行为
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            userActionService.recordBorrow(ud.getUsername(), borrow.getBookId());
        }

        return R.success(CodeEnum.SUCCESS, Constants.OK);
    }

    // --- 续借图书 ---
    @Operation(summary = "续借图书")
    @PostMapping("/renew")
    public R renewBook(@RequestParam Integer borrowId) {
        if (borrowId == null || borrowId <= 0) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }

        // BorrowService 中需实现 renewBorrow(borrowId)
        boolean success = borrowService.renewBorrow(borrowId);
        if (success) {
            // 续借行为
            Borrow b = borrowService.findById(borrowId);
            if (b != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof UserDetails ud) {
                    userActionService.recordRenew(ud.getUsername(), b.getBookId());
                }
            }
            return R.successMsg("续借成功，归还时间已延长");
        }
        return R.failMsg("续借失败：可能已归还、已超期或记录不存在");
    }

    @Operation(summary = "未归还列表（含逾期计算）")
    @GetMapping("/back/list")
    public R backList(@RequestParam Integer userId) {
        if (userId == null || userId <= 0) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }

        List<BackOut> outs = new ArrayList<>();

        // 获取所有 已借阅 未归还书籍
        List<Borrow> borrows = borrowService.findBorrowsByUserIdAndRet(userId, Constants.NO);
        for (Borrow borrow : borrows) {
            BackOut backOut = new BackOut();

            // 借阅记录ID（如果 BackOut 有 borrowId 字段）
            try {
                backOut.setBorrowId(borrow.getId());
            } catch (Exception ignored) {
            }

            // 注入书籍信息
            BookOut out = bookService.findBookById(borrow.getBookId());
            if (out != null) {
                BeanUtils.copyProperties(out, backOut);
            }

            // 格式化时间
            if (borrow.getCreateTime() != null) {
                backOut.setBorrowTime(DateUtil.format(borrow.getCreateTime(), Constants.DATE_FORMAT));
            }
            if (borrow.getEndTime() != null) {
                backOut.setEndTime(DateUtil.format(borrow.getEndTime(), Constants.DATE_FORMAT));
            }

            // 精确计算逾期
            Date now = new Date();
            if (borrow.getEndTime() != null && now.after(borrow.getEndTime())) {
                backOut.setLate(Constants.YES_STR);
                long lateDays = DateUtil.between(borrow.getEndTime(), now, DateUnit.DAY);
                try {
                    backOut.setLateDays((int) lateDays);
                } catch (Exception ignored) {
                }
            } else {
                backOut.setLate(Constants.NO_STR);
                try {
                    backOut.setLateDays(0);
                } catch (Exception ignored) {
                }
            }

            outs.add(backOut);
        }

        return R.success(CodeEnum.SUCCESS, outs);
    }

    @Operation(summary = "归还书籍（按借阅记录ID）")
    @PostMapping("/ret")
    public R retBook(@RequestParam Integer borrowId) {
        if (borrowId == null || borrowId <= 0) {
            return R.paramError("参数错误");
        }

        Borrow b = borrowService.findById(borrowId);
        boolean ok = borrowService.retBookByBorrowId(borrowId);
        if (!ok) {
            return R.failMsg("归还失败：借阅记录不存在或已归还");
        }
        if (b != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails ud) {
                userActionService.recordReturn(ud.getUsername(), b.getBookId());
            }
        }
        return R.success(CodeEnum.SUCCESS);
    }

}
