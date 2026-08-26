package co.edu.unbosque.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

	private final long loginIpCapacity;
	private final Duration loginIpWindow;
	private final long loginUserCapacity;
	private final Duration loginUserWindow;
	private final long forgotPasswordCapacity;
	private final Duration forgotPasswordWindow;

	private final Cache<String, Bucket> loginIpBuckets;
	private final Cache<String, Bucket> userFailureBuckets;
	private final Cache<String, Bucket> forgotPasswordBuckets;

	public RateLimitService(
			@Value("${ratelimit.login.ip.capacity:10}") long loginIpCapacity,
			@Value("${ratelimit.login.ip.window-seconds:60}") long loginIpWindowSeconds,
			@Value("${ratelimit.login.user.capacity:5}") long loginUserCapacity,
			@Value("${ratelimit.login.user.window-seconds:60}") long loginUserWindowSeconds,
			@Value("${ratelimit.forgot-password.ip.capacity:3}") long forgotPasswordCapacity,
			@Value("${ratelimit.forgot-password.ip.window-seconds:3600}") long forgotPasswordWindowSeconds) {
		this.loginIpCapacity = loginIpCapacity;
		this.loginIpWindow = Duration.ofSeconds(loginIpWindowSeconds);
		this.loginUserCapacity = loginUserCapacity;
		this.loginUserWindow = Duration.ofSeconds(loginUserWindowSeconds);
		this.forgotPasswordCapacity = forgotPasswordCapacity;
		this.forgotPasswordWindow = Duration.ofSeconds(forgotPasswordWindowSeconds);
		this.loginIpBuckets = newCache();
		this.userFailureBuckets = newCache();
		this.forgotPasswordBuckets = newCache();
	}

	public ConsumptionProbe tryConsumeLoginByIp(String ip) {
		Bucket bucket = loginIpBuckets.get(ip, k -> newBucket(loginIpCapacity, loginIpWindow));
		return bucket.tryConsumeAndReturnRemaining(1);
	}

	public ConsumptionProbe tryConsumeForgotPassword(String ip) {
		Bucket bucket = forgotPasswordBuckets.get(ip, k -> newBucket(forgotPasswordCapacity, forgotPasswordWindow));
		return bucket.tryConsumeAndReturnRemaining(1);
	}

	public boolean isUserBlocked(String username) {
		if (username == null || username.isBlank()) {
			return false;
		}
		Bucket bucket = userFailureBuckets.getIfPresent(username);
		return bucket != null && bucket.getAvailableTokens() <= 0;
	}

	public void registerFailedLogin(String username) {
		if (username == null || username.isBlank()) {
			return;
		}
		Bucket bucket = userFailureBuckets.get(username, k -> newBucket(loginUserCapacity, loginUserWindow));
		bucket.tryConsume(1);
	}

	public void resetUserFailures(String username) {
		if (username == null || username.isBlank()) {
			return;
		}
		userFailureBuckets.invalidate(username);
	}

	private Cache<String, Bucket> newCache() {
		return Caffeine.newBuilder()
				.expireAfterAccess(Duration.ofHours(1))
				.maximumSize(100_000)
				.build();
	}

	private Bucket newBucket(long capacity, Duration window) {
		return Bucket.builder()
				.addLimit(Bandwidth.builder().capacity(capacity).refillGreedy(capacity, window).build())
				.build();
	}
}
