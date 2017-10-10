package io.isharing.springddal.route.annotation;

/**
 * 拆分规则库引擎参数的默认属性名
 */
public class RouterConst {

    /**
     * 默认拆分字段，值为createDate
     */
//    public static final String ROUTER_ROUTEFIELD_DEFAULT = "createDate";
//    public static final String ROUTER_DBRULENAME_DEFAULT = "partitionByDate";
    public static final String ROUTER_TBLRULENAME_DEFAULT = "AutoPartitionByLong";
    public static final String ROUTER_TABLE_SUFFIX_DEFAULT = "_0000";

}
