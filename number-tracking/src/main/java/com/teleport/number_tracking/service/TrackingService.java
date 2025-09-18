package com.teleport.number_tracking.service;

import com.teleport.number_tracking.dto.TrackingResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public interface TrackingService {
    TrackingResponse generate(String originCountry,
                              String destinationCountry,
                              String weight,
                              OffsetDateTime createdAt,
                              UUID customerId,
                              String customerName,
                              String customerSlug);
}

@Service
@ConditionalOnBean(StringRedisTemplate.class)
class RedisTrackingService implements TrackingService {

    private final StringRedisTemplate redis;

    private static final String SEQ_KEY_PREFIX = "tn:seq:";
    private static final String SET_KEY = "tn:set";
    private static final int MAX_LEN = 16;
    private static final int SEQ_BITS = 20; // space for up to ~1M increments per ms

    public RedisTrackingService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public TrackingResponse generate(String originCountry, String destinationCountry, String weight, OffsetDateTime createdAt, UUID customerId, String customerName, String customerSlug) {
        // We'll attempt multiple times until we get a unique candidate (SADD assures uniqueness).
        for (int attempt = 0; attempt < 10; attempt++) {
            long millis = System.currentTimeMillis();
            String seqKey = SEQ_KEY_PREFIX + millis;
            Long seq = redis.opsForValue().increment(seqKey);
            // expire the sequence key after a short time to avoid memory growth
            redis.expire(seqKey, 5, TimeUnit.SECONDS);

            BigInteger combined = BigInteger.valueOf(millis).shiftLeft(SEQ_BITS).add(BigInteger.valueOf(seq));
            String candidate = toBase36Upper(combined);

            if (candidate.length() > MAX_LEN) {
                // compress deterministically using SHA-1 and hex: uppercase hex digits are within A-F and 0-9
                candidate = compressToHex(candidate, MAX_LEN);
            }

            candidate = candidate.toUpperCase(Locale.ROOT);
            // ensure purely A-Z0-9: base36 + hex already satisfies that.
            Long added = redis.opsForSet().add(SET_KEY, candidate);
            if (added != null && added == 1L) {
                // success
                return new TrackingResponse(candidate, OffsetDateTime.now());
            } else {
                // collision -> loop (very unlikely)
                try { Thread.sleep(0, 100); } catch (InterruptedException ignored) {}
            }
        }
        // last resort: fallback: create a strong digest and return first 16 chars uppercase hex
        String fallback = compressToHex(UUID.randomUUID().toString() + System.nanoTime(), MAX_LEN);
        redis.opsForSet().add(SET_KEY, fallback);
        return new TrackingResponse(fallback, OffsetDateTime.now());
    }

    private static String toBase36Upper(BigInteger n) {
        return n.toString(36).toUpperCase(Locale.ROOT);
    }

    private static String compressToHex(String input, int maxLen) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hex = String.format("%02X", b);
                sb.append(hex);
                if (sb.length() >= maxLen) break;
            }
            return sb.substring(0, maxLen);
        } catch (Exception e) {
            // fallback if digest fails
            String safe = input.replaceAll("[^A-Z0-9]", "0").toUpperCase(Locale.ROOT);
            if (safe.length() >= maxLen) return safe.substring(0, maxLen);
            return StringUtils.rightPad(safe, maxLen, '0');
        }
    }
}

/**
 * In-memory fallback for dev & testing if Redis is not present.
 * NOT recommended for a horizontally scaled production deployment.
 */
@Service
@ConditionalOnMissingBean(StringRedisTemplate.class)
class InMemoryTrackingService implements TrackingService {

    private final ConcurrentHashMap<String, Boolean> set = new ConcurrentHashMap<>();
    private static final int MAX_LEN = 16;

    @Override
    public TrackingResponse generate(String originCountry, String destinationCountry, String weight, OffsetDateTime createdAt, UUID customerId, String customerName, String customerSlug) {
        for (int attempt = 0; attempt < 1000; attempt++) {
            long millis = System.currentTimeMillis();
            long seq = (long) (Math.random() * 1_000_000);
            BigInteger combined = BigInteger.valueOf(millis).shiftLeft(20).add(BigInteger.valueOf(seq));
            String candidate = combined.toString(36).toUpperCase();
            if (candidate.length() > MAX_LEN) {
                candidate = sha1Hex(candidate).substring(0, MAX_LEN);
            }
            Boolean prev = set.putIfAbsent(candidate, Boolean.TRUE);
            if (prev == null) {
                return new TrackingResponse(candidate, OffsetDateTime.now());
            }
        }
        throw new RuntimeException("Unable to generate unique tracking number (in-memory fallback)");
    }

    private String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] d = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02X", b));
            return sb.toString();
        } catch (Exception ex) {
            return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        }
    }
}