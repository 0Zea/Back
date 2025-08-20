package org.ozea.security.util.blacklist;

public interface TokenBlacklistService {
    boolean isBlacklisted(String jti);
    void blacklist(String jti, long ttlMs);
}