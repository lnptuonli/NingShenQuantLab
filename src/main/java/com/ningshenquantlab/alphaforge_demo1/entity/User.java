package com.ningshenquantlab.alphaforge_demo1.entity;
import  lombok.Data;
// @Data 是 Lombok 注解，自动生成 getter、setter、toString、equals、hashCode
// 等价于手写：
// public Long getId() { return id; }
// public void setId(Long id) { this.id = id; }
@Data
public class User {
    private Long cust_id;         //用户名
    private String cust_key;      //key
    private String cust_name;     //昵称
}
