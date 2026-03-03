package com.book.manager.repos;

import com.book.manager.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Description 用户 JPA Repository
 */
@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    /**
     * 用户名模糊查询（分页）
     * 等价 SQL：
     * select * from users where username like %keyword%
     */
    Page<Users> findByUsernameContaining(String keyword, Pageable pageable);

    /**
     * 用户名精确查询（登录 / 注册校验）
     */
    Users findByUsername(String username);
}
