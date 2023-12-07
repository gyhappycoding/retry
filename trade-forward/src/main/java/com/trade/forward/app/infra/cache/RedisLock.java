package com.trade.forward.app.infra.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisLock {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedisLock.class);

	private static final String PREFIX = "lock:";

	private static final long DEFAULT_EXPIRE_TIME = 5000;

	private static final long DEFAULT_WAIT_TIME = 100;

	@Resource
	private StringRedisTemplate redisTemplate;

	private static final String LOCK_SCRIPT = " local tt = redis.call('PTTL',KEYS[1]);if (tt == -2) then redis.call('PSETEX',KEYS[1],ARGV[1],ARGV[2]);end; if(tt == -1) then redis.call('PEXPIRE',KEYS[1],ARGV[1]);end; return tt;";

	private static final String UNLOCK_SCRIPT = " if(ARGV[1] == 'force' or redis.call('get',KEYS[1])==ARGV[1]) then redis.call('DEL',KEYS[1]);return 1; end; return 0; ";

	private RedisScript<Long> lockScript;

	private RedisScript<Long> unlockScript;

	@PostConstruct
	public void init() {
		lockScript = new DefaultRedisScript<>(LOCK_SCRIPT, Long.class);
		unlockScript = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
	}

	public String tryLock(Object lockKey, long expireTime, long waitTime) {
		LockResult lockResult = executeLock(lockKey, expireTime);
		if (lockResult.getPttl() == -2) {
			return lockResult.getValue();
		} else if (lockResult.getPttl() > -1 && waitTime > 0 && lockResult.getPttl() < waitTime) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				LOGGER.error("RedisLock.tryLock Thread.sleep exception", e);
			}
			return tryLock(lockKey, expireTime, waitTime - 10);
		}
		return null;
	}

	public String tryLock(Object lockKey, long expireTime) {

		return tryLock(lockKey, expireTime, 0);
	}

	public String tryLock(Object lockKey) {

		return tryLock(lockKey, DEFAULT_EXPIRE_TIME, 0);
	}

	public void lock(Object lockKey, long expireTime) {
		String lock = tryLock(lockKey, expireTime, DEFAULT_WAIT_TIME);
		while (lock == null) {
			lock = tryLock(lockKey, expireTime, DEFAULT_WAIT_TIME);
		}
	}

	public void lock(Object key) {

		lock(key, DEFAULT_EXPIRE_TIME);
	}

	private LockResult executeLock(Object lockKey, long expireTime) {
		String key = PREFIX + lockKey;
		String lockVal = UUID.randomUUID().toString();
		List<String> keyList = new ArrayList<>();
		keyList.add(key);
		Long pttl = redisTemplate.execute(lockScript, keyList, String.valueOf(expireTime), lockVal);
		LockResult lockResult = new LockResult();
		lockResult.setPttl(pttl);
		lockResult.setValue(lockVal);
		return lockResult;
	}

	private boolean executeUnlock(Object lockKey, String lockVal) {
		String key = PREFIX + lockKey;
		List<String> keyList = new ArrayList<>();
		keyList.add(key);
		Long result = redisTemplate.execute(unlockScript, keyList, lockVal);
		return result != null && result == 1;
	}

	public void unlock(Object key, String lockVal) {
		executeUnlock(key, lockVal);
	}

	public void unlock(Object key) {
		executeUnlock(key, "force");
	}

	private static class LockResult {
		private String value;

		private long pttl;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public long getPttl() {
			return pttl;
		}

		public void setPttl(long pttl) {
			this.pttl = pttl;
		}
	}

}
