package com.ice.db;

import java.util.Map;

import com.ice.util.Const;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisHelper {
	private static JedisPool pool = null;

	public static JedisPool getPool() {
		if (pool == null) {
			JedisPoolConfig config = new JedisPoolConfig();
			// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
			// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
			
			config.setMaxTotal(100);
			// 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
			config.setMaxIdle(5);
			// 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
			config.setMaxWaitMillis(1000 * 100);
			// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
			config.setTestOnBorrow(true);
			pool = new JedisPool(config, Const.RedisURL, Const.RedisPort);
		}
		return pool;
	}

	/**
	 * 获取数据
	 * 
	 * @param key
	 * @return
	 */

	public static String get(String key) {
		String value = null;

		JedisPool pool = null;
		Jedis jedis = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			value = jedis.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}
		return value;
	}

	public static String hget(String key, String filed) {
		String value = null;

		JedisPool pool = null;
		Jedis jedis = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			value = jedis.hget(key, filed);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}
		return value;
	}

	public static Map<String, String> hgetAll(String key) {
		Map<String, String> value = null;

		JedisPool pool = null;
		Jedis jedis = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			value = jedis.hgetAll(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}
		return value;
	}

	public static String set(String key, String value) {
		JedisPool pool = null;
		Jedis jedis = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			jedis.set(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}

		return value;
	}

	public static String setex(String key, int seconds, String value) {
		JedisPool pool = null;
		Jedis jedis = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			jedis.setex(key, seconds, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}

		return value;
	}

	public static String hset(String key, String field, String value) {
		JedisPool pool = null;
		Jedis jedis = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			jedis.hset(key, field, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}

		return value;
	}

	public static Long del(String key) {
		JedisPool pool = null;
		Jedis jedis = null;
		Long value = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			value = jedis.del(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}

		return value;
	}

	public static Long hdel(String key, String field) {
		JedisPool pool = null;
		Jedis jedis = null;
		Long value = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			value = jedis.hdel(key, field);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}

		return value;
	}

	public static Long rpush(String key, String value) {
		JedisPool pool = null;
		Jedis jedis = null;
		Long l = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			l = jedis.rpush(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}

		return l;
	}

	public static String lpop(String key) {
		JedisPool pool = null;
		Jedis jedis = null;
		String value = null;
		try {
			pool = getPool();
			jedis = pool.getResource();
			value = jedis.lpop(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 返还到连接池
			if (null != jedis) {
				jedis.close();
			}
		}
		return value;
	}

	public static String getMQ(String key) {
		return lpop(key);
	}

	public static Long addMQ(String key, String value) {
		return rpush(key, value);
	}
}

