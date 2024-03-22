package cn.keking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import cn.keking.web.controller.MyAuthenticationFailureHandler;

@EnableWebSecurity
@Configuration
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/login/**").permitAll().antMatchers("/fail/**").permitAll().antMatchers("/api/download/**").permitAll().anyRequest()
				.authenticated().and().formLogin((formLogin) -> formLogin.loginPage("/login") // 自定义登录页面路径
						.usernameParameter("username") // 定义从Form中获取用户名的key 与html中的form参数匹配
						.passwordParameter("password") // 定义从Form中获取密码的key 与html中的form参数匹配
						.loginProcessingUrl("/my/login") // 认证发起的URL，访问该URL则认证凭证 这样要与HTML中form的提交地址一致
						.failureHandler(new MyAuthenticationFailureHandler("/fail")))
				// 禁用httpBasic
				.httpBasic((httpBasic) -> httpBasic.disable())
				// 不启用csrf
				// 如果启用csrf认证 则自定义的POST /my/login方法也会被 CsrfFilter 拦截.
				// 需要在请求参数中携带_csrf参数 或者 请求头中增加X-CSRF-TOKEN 否则csrf校验不过 又会跳转到loginPage页面
				.csrf((csrf) -> csrf.disable()).headers().frameOptions().disable();
	}
}
