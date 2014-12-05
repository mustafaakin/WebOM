package webom.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisJSONSession implements SessionBackend {
	final static Logger logger = LoggerFactory.getLogger(RedisJSONSession.class);
	public JedisPool pool;

	public RedisJSONSession(String host) {
		pool = new JedisPool(new JedisPoolConfig(), host);
	}

	@Override
	public void destroy(Session session) {
		try (Jedis jedis = pool.getResource()) {
			jedis.del("webom_" + session);
		}
	}

	@Override
	public Session get(String key) {
		try (Jedis jedis = pool.getResource()) {
			Session session = new Session(key, this);
			String json = jedis.get("webom_" + key);
			if (json == null)
				return session;
			session.fromJSON(json);
			return session;
		}
	}

	@Override
	public void set(Session session) {
		try (Jedis jedis = pool.getResource()) {
			jedis.set("webom_" + session.getKey(), session.toJSON());
		}
	}
}
