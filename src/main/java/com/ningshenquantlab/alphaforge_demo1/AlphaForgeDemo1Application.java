package com.ningshenquantlab.alphaforge_demo1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


/*@SpringBootApplication =
@SpringBootConfiguration +      // 标记为配置类
@EnableAutoConfiguration +      // 启用自动配置
@ComponentScan                  // 组件扫描*/
@SpringBootApplication(scanBasePackages = "com.ningshenquantlab.alphaforge_demo1")
public class AlphaForgeDemo1Application {

    public static void main(String[] args) {
        ApplicationContext context =SpringApplication.run(AlphaForgeDemo1Application.class, args);
        // 获取所有 Bean 的名称
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

}
