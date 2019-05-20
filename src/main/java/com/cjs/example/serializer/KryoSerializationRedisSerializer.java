package com.cjs.example.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializationRedisSerializer<T> implements RedisSerializer<T> {

	private static final KryoPool kryoPool = new KryoPool();

	/**
	 * kryo为线程不安全对象，为避免每次create, 使用pool优化性能
	 */
	@Override
	public byte[] serialize(T t) throws SerializationException {
		Kryo kryo = null;
		try{
			kryo = kryoPool.borrowObject();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Output output = new Output(bos);
			kryo.writeClassAndObject(output, t);
			output.close();
			return bos.toByteArray();
		}catch(Exception e){
			throw new RuntimeException(e);
		} finally {
			if (kryo != null) {
				kryoPool.returnObject(kryo);
			}

		}
	}

	@Override
	public T deserialize(byte[] bytes) throws SerializationException {
		Kryo kryo = null;
		if(bytes != null && bytes.length == 0){
			throw new SerializationException("bytes length is 0");
		}
		
		try{
			kryo = kryoPool.borrowObject();
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			Input input = new Input(bis);
			T t = (T) kryo.readClassAndObject(input);
			input.close();
			return t;
		}catch(Exception e){
			throw new RuntimeException(e);
		} finally {
			if (kryo != null) {
				kryoPool.returnObject(kryo);
			}

		}
	}
}