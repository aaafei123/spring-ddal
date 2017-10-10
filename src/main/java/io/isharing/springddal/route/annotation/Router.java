package io.isharing.springddal.route.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface Router {
	
	/**
	 * 是否拆分，默认为拆分
	 */
	boolean isRoute() default true;
	
	/**
	 * 强制将读操作在写库中读，以避免写的时候从读库读不到数据（主从同步延迟导致的问题）
	 * TODO 未考虑事务传播的情况
	 */
	boolean forceReadOnMaster() default false;
	
	/**
	 * 是否读操作，默认true
	 */
	boolean readOnly() default true;
	
	/**
	 * 拆分字段
	 */
//    String routerField() default RouterConst.ROUTER_ROUTEFIELD_DEFAULT;
    
    /**
     * 拆分规则名
     */
    String ruleName() default RouterConst.ROUTER_TBLRULENAME_DEFAULT;
    
    /**
     * 拆分表的后缀样式/风格
     */
    String tableStyle() default RouterConst.ROUTER_TABLE_SUFFIX_DEFAULT;
}
