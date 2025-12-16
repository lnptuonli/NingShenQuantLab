package com.ningshenquantlab.alphaforge_demo1.mapper;
import com.ningshenquantlab.alphaforge_demo1.entity.UserAcct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper//标记这是一个 MyBatis Mapper 接口,Spring Boot 会自动扫描并创建代理对象,不需要写实现类
public interface UserAcctMapper {

    /**
     * 根据 ID 查询用户
     */
    UserAcct selectByCustId(Long custId);

    /**
     * 根据用户名查询用户
     */
    UserAcct selectByCustName(String custName);

    /**
     * 查询所有用户
     */
    List<UserAcct> selectAll();

    /**
     * 分页查询用户
     */
    List<UserAcct> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);
    //给参数命名，在 XML 中可以通过名称引用
    //- 如果只有一个参数，可以不加 @Param
    //- 如果有多个参数，建议都加 @Param

    /**
     * 查询用户总数
     */
    Long countAll();

    /**
     * 插入用户
     */
    int insert(UserAcct userAcct);

    /**
     * 更新用户
     */
    int update(UserAcct userAcct);

    /**
     * 删除用户
     */
    int deleteByCustId(Long custId);
}



