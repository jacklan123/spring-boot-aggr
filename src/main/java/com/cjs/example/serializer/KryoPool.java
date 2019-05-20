package com.cjs.example.serializer;

import com.esotericsoftware.kryo.Kryo;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * 池化kryo
 * @author
 */
public class KryoPool extends GenericObjectPool<Kryo> {
    public KryoPool() {
        super(new KryoPool.KryoFactory());
        this.setMaxIdle(500);
        this.setMinIdle(50);
        this.setMaxTotal(60000);
        this.setMaxWaitMillis(100L);
    }

    public static class KryoFactory extends BasePooledObjectFactory<Kryo> {
        public KryoFactory() {
        }

        @Override
        public PooledObject<Kryo> wrap(Kryo kryo) {
            return new DefaultPooledObject(kryo);
        }

        @Override
        public Kryo create() throws Exception {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            return kryo;
        }
    }
}
