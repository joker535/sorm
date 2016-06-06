package com.guye.orm.impl;

import com.guye.orm.DaoConfig;
import com.guye.orm.Entity;
import com.guye.orm.utils.Lang;

import android.util.LruCache;

public class EntityHolder {
    private AnnotationEntityMaker         maker;

    private LruCache<Class<?>, Entity<?>> map;

    public EntityHolder() {
        this.map = new LruCache<>(DaoConfig.DEFAULT_CACHESIZE);
        maker = new AnnotationEntityMaker(this);
    }

    public void set( Entity<?> en ) {
        this.map.put(en.getType(), en);
    }

    public void remove( Entity<?> en ) {
        if (en == null || en.getType() == null)
            return;
        this.map.remove(en.getType());
    }

    /**
     * 根据类型获取实体
     * 
     * @param classOfT
     *            实体类型
     * @return 实体
     */
    @SuppressWarnings("unchecked")
    public <T> Entity<T> getEntity( Class<T> classOfT ) {
        Entity<?> re = map.get(classOfT);
        if (null == re || !re.isComplete()) {
            re = map.get(classOfT);
            if (null == re) {
                re = maker.make(classOfT);
                map.put(classOfT, re);
            }
        }
        return (Entity<T>) re;
    }

    /**
     * 根据一个对象获取实体
     * <p>
     * 对象如果是集合或者数组，则取其第一个元素进行判断
     * 
     * @param obj
     *            对象
     * @return 实体
     */
    @SuppressWarnings("unchecked")
    public Entity<?> getEntityBy( Object obj ) {
        // 正常的构建一个 Entity
        Object first = Lang.first(obj);
        // 对象为空，不能构建实体
        if (first == null)
            return null;

        // 作为 POJO 构建
        return getEntity(first.getClass());
    }

}
