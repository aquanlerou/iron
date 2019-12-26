package run.aquan.iron.system.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import run.aquan.iron.security.entity.JwtUser;
import run.aquan.iron.security.utils.JwtTokenUtil;
import run.aquan.iron.system.core.Result;
import run.aquan.iron.system.core.ResultResponse;
import run.aquan.iron.system.enums.Datalevel;
import run.aquan.iron.system.model.dto.AuthToken;
import run.aquan.iron.system.model.entity.SysUser;
import run.aquan.iron.system.model.params.LoginParam;
import run.aquan.iron.system.repository.SysUserRepository;
import run.aquan.iron.system.service.SysUserService;
import run.aquan.iron.system.utils.IronDateUtil;

import java.util.Date;
import java.util.Optional;

/**
 * @Class SysUserServiceImpl
 * @Description TODO
 * @Author Aquan
 * @Date 2019.12.23 23:44
 * @Version 1.0
 **/
@Slf4j
@Service
public class SysUserServiceImpl implements SysUserService {

    private final SysUserRepository sysUserRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SysUserServiceImpl(SysUserRepository sysUserRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.sysUserRepository = sysUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public String init() {
        // 防止重复初始化
        Optional<SysUser> admin = sysUserRepository.findByUsernameAndDatalevel("admin", Datalevel.EFFECTIVE);
        if (!admin.isPresent()) {
            SysUser sysUser = SysUser.builder().username("admin").password(bCryptPasswordEncoder.encode("aquan")).roles("ADMIN").build();
            try {
                sysUserRepository.save(sysUser);
                return "initialized successfully";
            } catch (Exception e) {
                log.error(e.getMessage());
                return "initialization failed";
            }
        } else {
            return "No need to repeat initialization";
        }
    }

    @Override
    public SysUser findUserByUserName(String username) {
        try {
            SysUser sysUser = sysUserRepository.findByUsernameAndDatalevel(username, Datalevel.EFFECTIVE).orElseThrow(() -> new UsernameNotFoundException("No user found with username " + username));
            return sysUser;
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public Result login(LoginParam loginParam) {
        String username = loginParam.getUsername();
        try {
            SysUser sysUser = sysUserRepository.findByUsernameAndDatalevel(username, Datalevel.EFFECTIVE).orElseThrow(() -> new UsernameNotFoundException("No user found with username " + username));
            if (bCryptPasswordEncoder.matches(loginParam.getPassword(), sysUser.getPassword())) {
                synchronized (this) {
                    AuthToken authToken = JwtTokenUtil.createToken(new JwtUser(sysUser), loginParam.getRememberMe());
                    sysUser.setExpirationTime(authToken.getExpiration());
                    sysUserRepository.saveAndFlush(sysUser);
                    return ResultResponse.genSuccessResult(authToken);
                }
            } else {
                return ResultResponse.genFailResult("Admin Password erro");
            }
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            return ResultResponse.genFailResult("No user found with username " + username);
        }
    }

}