package io.isharing.springddal.route;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.isharing.springddal.datasource.DynamicDataSourceHolder;
import io.isharing.springddal.route.annotation.Router;
import io.isharing.springddal.route.exception.ParamsErrorException;
import io.isharing.springddal.route.rule.conf.TableRule;
import io.isharing.springddal.route.rule.conf.XMLLoader;
import io.isharing.springddal.route.rule.utils.ParameterMapping;

/**
 * 切面切点 在Router注解的方法执行前执行 切点织入
 */
@Component
public class RouteInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RouteInterceptor.class);
    
    /**
     * 缓存
     */
    private static ConcurrentHashMap<String, Boolean> methodIsReadCache = new ConcurrentHashMap<String, Boolean>();

    private RouteStrategy routeStrategy;

    public void routePoint() {}
    
    public Object doRoute(ProceedingJoinPoint jp) throws Throwable {
        long t1 = System.currentTimeMillis();
        Object result = null;
        
        /**
         * #1 获取Router注解，通过isRoute属性判断该实体是否需要拆分
         * 		如不需拆分库表，则直接访问默认库
         * 		如需拆分库表，则DAO的方法判断读写操作
         */
        Router router = getDeclaringClassAnnotation(jp);
        if(null == router){
        	log.debug(">>> No Router annotation, use default node for query.");
        	routeStrategy.routeToGlobalNode();
        }else{
        	boolean isRoute = router.isRoute();
        	boolean readOnly = router.readOnly();
            Method method = ((MethodSignature) jp.getSignature()).getMethod();
            Object target = jp.getTarget();
            String cacheKey = target.getClass().getName() + "." + method.getName();
            Boolean isReadCacheValue = methodIsReadCache.get(cacheKey);
            if (isReadCacheValue == null) {
                // 重新获取方法，否则传递的是接口的方法信息
                Method realMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
                isReadCacheValue = isChoiceReadDB(realMethod, readOnly);
                methodIsReadCache.put(cacheKey, isReadCacheValue);
            }
            String ruleName = router.ruleName();
        	if(StringUtils.isBlank(ruleName)){
        		log.error(">>> ruleName is NULL, throw ParamsErrorException.");
        		throw new ParamsErrorException();
        	}
            if(isRoute) {//路由计算
            	/**
                 * #2 计算路由。
                 * 		需要路由的操作：
                 * 		如果是写操作，则进入规则库进行计算写库的index
                 * 		如果是读操作，则进入规则库计算读库的index
                 */
            	log.debug(">>> calculating route...");
                execute(jp, router, ruleName, isReadCacheValue);
            }else{
            	log.debug(">>> isRoute is not config on Router, using default node config on Rules.xml.");
            	routeStrategy.routeToDefaultNode(ruleName);
            }
        }

        try {
        	result = jp.proceed();
        } finally {
            DynamicDataSourceHolder.reset();
        }
        log.debug(">>> doRoute time cost: " + (System.currentTimeMillis() - t1));
        return result;
    }
    
    /**
     * 根据拆分字段routeField和拆分字段值routeFieldValue进入规则库进行路由计算
     * 
     * @param jp					ProceedingJoinPoint对象
     * @param router				Router注解对象
     * @param isReadCacheValue		是否读操作，boolean值
     * @throws Throwable
     */
    private void execute(ProceedingJoinPoint jp, Router router, String ruleName, boolean isReadCacheValue) throws Throwable {
    	TableRule tableRule = XMLLoader.getTableRuleByRuleName(ruleName);
    	String routeField = tableRule.getRouteColumn();
    	log.debug(">>> ruleName="+ruleName+", routeField="+routeField);
        Object[] args = jp.getArgs();
        Map<String, Object> nameAndArgs = getFieldsNameAndArgs(jp, args);
        log.debug(">>> "+nameAndArgs.toString());

        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                long t2 = System.currentTimeMillis();
                String routeFieldValue = "";
                
                /**
                 * MyBatis的传入参数parameterType类型分两种
   				 * 1. 基本数据类型：int,string,long,Date;
   				 * 2. 复杂数据类型：类和Map
                 */
                if(ParameterMapping.isPrimitiveType(args[i].getClass())){//基本数据类型：int,string,long,Date
                	/*
                	 * !!CAUTION!! 
                	 * MyBatis中SQL语句传值是基本类型的情况，如果没有拆分字段则会无法匹配拆分字段的值。
                	 * 规避方法为都用对象传值（如Map或实体对象），以下为另外一种临时解决方法。
                	 */
                	if(null != nameAndArgs && nameAndArgs.size() > 0){
                    	Object objValue = nameAndArgs.get(routeField);
                    	if(null != objValue){
                    		/**
                    		 * 要求：传递的值为基本类型时，函数的参数的名称必须与字段的名称一致.
                    		 * 比如rules.xml定义的拆分字段为userName，那么对应的方法名比如queryByUserName(String userName)，
                    		 * 参数userName与拆分字段必须保持一致，即为userName
                    		 */
                    		routeFieldValue = objValue.toString();
                    		log.error(">>> parameters is primitive type, routeField="+routeField+", routeFieldValue="+routeFieldValue);
                    	}
                    }
                	//无拆分字段，则直接路由至最新分库上查询
                	if(StringUtils.isBlank(routeFieldValue)){
                    	log.error(">>> routeFieldValue is NULL[isPrimitiveType], query from default nodes(define in rules.xml).");
                    	routeStrategy.routeToDefaultNode(ruleName);
                    	return;
                	}
//                	routeFieldValue = args[i].toString();
                }else{//复杂数据类型：类和Map，以及collection（List、Array...）
                	if(args[i] instanceof List 
                			|| args[i].getClass().isArray()){
                		/**
                		 * 这里有两种情况：1）带分片键的；2）不带分片键的
                		 * 带分片键的：比如 id in (...) 之类的
                		 * 不带分片键的：比如 name in (...) 之类的
                		 * 这两种情况，应该是 TODO 按切分字段到相应节点查询或全库扫描，
                		 * 但是目前全库扫描和跨库查询过于复杂，暂时不支持，对于这种情况，会直接路由至最新分库上查询。
                		 */
                		log.error(">>> Parameters is collection object, query from latest nodes(define in rules.xml).");
                		routeStrategy.routeToDefaultNode(ruleName);
                    	return;
                	}else{
                		routeFieldValue = BeanUtils.getProperty(args[i], routeField);
                	}
                }
                log.debug(">>> routeFieldValue="+routeFieldValue + ", cost time=" + (System.currentTimeMillis() - t2));
//            	String ruleName = router.ruleName();
                if(StringUtils.isBlank(routeFieldValue)){
                	log.error(">>> routeFieldValue is NULL, query from default nodes(define in rules.xml).");
                	routeStrategy.routeToDefaultNode(ruleName);
                	return;
            	}
                boolean forceReadOnMaster = router.forceReadOnMaster();
                //进入规则库进行路由计算
                routeStrategy.route(ruleName, routeField, routeFieldValue, isReadCacheValue, forceReadOnMaster);
            }
        }
    }
    
    /**
     * 判断是否只读方法
     * 
     * 这里有两种途径判断是否读操作：
     * 1）通过事务注解 <code>@Transactional</code>
     * 2）通过Router注解配置 <code>@Router</code>
     * 
     * 
     *
     * @param method 		执行方法
     * @param readOnly		注解中配置的是否读操作
     * @return 			当前方法是否只读
     */
    private boolean isChoiceReadDB(Method method, boolean readOnly) {
        Transactional transactionalAnno = AnnotationUtils.findAnnotation(method, Transactional.class);
        if (transactionalAnno == null || readOnly) {
            return true;
        }
        // 如果之前选择了写库，则现在还选择写库
        if (DynamicDataSourceHolder.isChoiceWrite()) {
            return false;
        }
        if(!readOnly){
        	return false;
        }
        if (transactionalAnno.readOnly()) {
            return true;
        }
        return false;
    }
    
    /**
     * 获取自定义注解Router对象
     * 
     * @param jp
     * @return
     * @throws NoSuchMethodException
     */
	private Router getDeclaringClassAnnotation(ProceedingJoinPoint jp) throws NoSuchMethodException {
		Router annotation = null;
		Method method = getMethod(jp);
		boolean flag = method.isAnnotationPresent(Router.class);
		log.error(">>> isAnnotationPresent flag is " + flag);
		if (flag) {
			annotation = method.getAnnotation(Router.class);
		} else {
			// 如果方法上没有注解，则搜索类上是否有注解
			annotation = AnnotationUtils.findAnnotation(method.getDeclaringClass(), Router.class);
			if (annotation == null) {
				annotation = AnnotationUtils.findAnnotation(jp.getTarget().getClass(), Router.class);
			}
		}
		log.error(">>> annotation is "+annotation);
		return annotation;
	}

    private Method getMethod(ProceedingJoinPoint jp) throws NoSuchMethodException {
        MethodSignature msig = (MethodSignature) jp.getSignature();
        return getClass(jp).getMethod(msig.getName(), msig.getParameterTypes());
    }

    private Class<? extends Object> getClass(ProceedingJoinPoint jp) throws NoSuchMethodException {
        return jp.getTarget().getClass();
    }
    
    private Map<String,Object> getFieldsNameAndArgs(ProceedingJoinPoint jp, Object[] args) throws NotFoundException, ClassNotFoundException{
    	String classType = jp.getTarget().getClass().getName();    
        Class<?> clazz = Class.forName(classType);    
        String clazzName = clazz.getName();
        String methodName = jp.getSignature().getName();
        Map<String, Object> nameAndArgs = getFieldsNameAndArgs(this.getClass(), clazzName, methodName, args);
        return nameAndArgs;
    }
    
    /**
     * 获取参数名和参数值，以Map形式返回
     * 参数名为调用的函数的参数名。
     * @param cls
     * @param clazzName
     * @param methodName
     * @param args
     * @return
     * @throws NotFoundException
     */
    private Map<String,Object> getFieldsNameAndArgs(Class cls, String clazzName, String methodName, Object[] args) throws NotFoundException {
        Map<String,Object > map = new HashMap<String,Object>();
        ClassPool pool = ClassPool.getDefault();
        //ClassClassPath classPath = new ClassClassPath(this.getClass());
        ClassClassPath classPath = new ClassClassPath(cls);
        pool.insertClassPath(classPath);
        CtClass cc = pool.get(clazzName);
        CtMethod cm = cc.getDeclaredMethod(methodName);
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            throw new NotFoundException(">>> LocalVariableAttribute is NULL!");
        }
       // String[] paramNames = new String[cm.getParameterTypes().length];
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < cm.getParameterTypes().length; i++){
            map.put( attr.variableName(i + pos),args[i]);//paramNames即参数名
        }
        return map;
    }

	public RouteStrategy getRouteStrategy() {
		return routeStrategy;
	}

	public void setRouteStrategy(RouteStrategy routeStrategy) {
		this.routeStrategy = routeStrategy;
	}

}
