package my;

import java.util.HashMap;
import java.util.Map;

public class TypeSingletonMap {
    public final Map<Class<?>, Object> registry = new HashMap<>();
    // 通过可变参数（varargs）注册多个类型，自动创建单例
    public  TypeSingletonMap(Class... types) {
        for (Class type : types) {
            if (!registry.containsKey(type)) {
                // 在首次访问时实例化
                try {
                    registry.put(type, type.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create instance of " + type, e);
                }
            }
        }
    }

    // 通过类型查询实例
    public <T> T getInstance(Class<T> clazz) {
        return clazz.cast(registry.get(clazz));
    }

}