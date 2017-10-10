package io.isharing.springddal.route.rule.utils;

public interface Visitor {
    void visit(String name, Class<?> type, Class<?> definedIn, Object value);
}