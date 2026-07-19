package com.triage.config;

/** Current request's tenant id on a ThreadLocal (web thread only; does not cross Kafka). */
public final class TenantContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();
    private TenantContext() {}
    public static void set(String t) { CURRENT.set(t); }
    public static String get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
}
