package com.book.manager.service;

import cn.hutool.core.bean.BeanUtil;
import com.book.manager.dao.UsersMapper;
import com.book.manager.entity.Users;
import com.book.manager.repos.UsersRepository;
import com.book.manager.util.ro.PageIn;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Description 用户业务类
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UsersMapper usersMapper;

    /**
     * 添加用户 (注册)
     * @param users 用户
     * @return 返回添加的用户
     */
    public Users addUser(Users users) {
        // Plaintext storage to match NoOpPasswordEncoder
        return usersRepository.saveAndFlush(users);
    }

    /**
     * 编辑用户
     * @param users 用户对象
     * @return true or false
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(Users users) {
        // 如果编辑时修改了密码，这里通常也需要加密，但具体看你的业务逻辑是否包含密码修改
        // 如果这里仅修改信息不改密码，保持原样即可
        return usersMapper.updateUsers(BeanUtil.beanToMap(users)) > 0;
    }

    /**
     * 用户详情
     * @param id 主键
     * @return 用户详情
     */
    public Users findUserById(Integer id) {
        Optional<Users> optional = usersRepository.findById(id);
        return optional.orElse(null);
    }

    /**
     * 删除用户
     * @param id 主键
     */
    public void deleteUser(Integer id) {
        usersRepository.deleteById(id);
    }

    /**
     * 用户搜索查询(mybatis 分页)
     * @param pageIn
     * @return
     */
    public PageInfo<Users> getUserList(PageIn pageIn) {
        PageHelper.startPage(pageIn.getCurrPage(), pageIn.getPageSize());
        List<Users> listByLike = usersMapper.findListByLike(pageIn.getKeyword());
        return new PageInfo<>(listByLike);
    }

    /**
     * 读者搜索查询(mybatis 分页)
     */
    public PageInfo<Users> getReaderList(PageIn pageIn) {
        PageHelper.startPage(pageIn.getCurrPage(), pageIn.getPageSize());
        List<Users> listByLike = usersMapper.findReadersByLike(pageIn.getKeyword());
        return new PageInfo<>(listByLike);
    }

    /**
     * 用户鉴权 (Spring Security 调用)
     * @param username 用户名
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查找用户
        Users user = usersRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        // 获得角色
        String role = String.valueOf(user.getIsAdmin());
        // 角色集合
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 角色必须以`ROLE_`开头
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        // 返回 UserDetails，密码校验由 Spring Security 自动完成
        return new User(user.getUsername(), user.getPassword(), authorities);
    }

    /**
     * 用户名查询用户信息
     * @param username 用户名
     */
    public Users findByUsername(String username) {
        return usersRepository.findByUsername(username);
    }
}