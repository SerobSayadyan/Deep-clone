import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class CopyUtils {

    // We need IdentityHashMap because we need to compare just references not values(that is obj1 == obj2)
    private static final Map<Object, Object> copiedObjects = new IdentityHashMap<>();

    @SuppressWarnings("all")
    public static <T> T deepCopy(T obj) {
        if (obj == null) {
            return null;
        }

        if (copiedObjects.containsKey(obj)) {
            return (T) copiedObjects.get(obj);
        }

        if (isImmutable(obj)) {
            return obj;
        }

        // Handle arrays
        if (obj.getClass().isArray()) {
            T[] originalArray = (T[]) obj;
            int length = originalArray.length;
            T[] newArray = (T[]) new Object[length];
            for (int i = 0; i < length; i++) {
                newArray[i] = deepCopy(originalArray[i]);
            }
            copiedObjects.put(obj, newArray);
            return (T) newArray;
        }

        // Handle Map.Entry
        if (obj instanceof Map.Entry<?, ?>) {
            Map.Entry<?, ?> originalEntry = (Map.Entry<?, ?>) obj;
            try {
                Object key = deepCopy(originalEntry.getKey());
                Object value = deepCopy(originalEntry.getValue());
                return (T) new AbstractMap.SimpleEntry<>(key, value);
            } catch (Exception e) {
                throw new RuntimeException("Error creating new Map.Entry instance for deep copy", e);
            }
        }

        // Handle Maps
        if (obj instanceof Map) {
            Map<?, ?> originalMap = (Map<?, ?>) obj;

            try {
                // Try to create a new instance of the same map type
                Map<Object, Object> newMap = null;
                if (!isImmutableMap(originalMap)) {
                    Constructor<?> constructorOfMap = originalMap.getClass().getDeclaredConstructor();
                    constructorOfMap.setAccessible(true);
                    newMap = (Map<Object, Object>) constructorOfMap.newInstance();

                    for (Map.Entry<?, ?> key : originalMap.entrySet()) {
                        Map.Entry entry = deepCopy(key);
                        newMap.put(entry.getKey(), entry.getValue());
                    }
                } else {
                    newMap = (Map) originalMap;
                }
                copiedObjects.put(obj, newMap);

                return (T) newMap;
            } catch (Exception e) {
                throw new RuntimeException("Error creating new map instance for deep copy", e);
            }

        }

        // Handle Collections
        if (obj instanceof Collection<?>) {
            Collection<?> originalCollection = (Collection<?>) obj;
            Class<?> collectionType = obj.getClass();
            try {
                Object newCollection = null;

                if (!isImmutableCollection(originalCollection)) {
                    // Try to create a new instance of the same collection type
                    Constructor<?> constructorOfIterable = collectionType.getDeclaredConstructor();
                    constructorOfIterable.setAccessible(true);
                    newCollection = constructorOfIterable.newInstance();
                    for (Object element : originalCollection) {
                        ((Collection) newCollection).add(deepCopy(element));
                    }
                } else {
                    newCollection = (Collection<?>) obj;
                }

                copiedObjects.put(obj, newCollection);
                return (T) newCollection;
            } catch (Exception e) {
                throw new RuntimeException("Error creating new collection instance for deep copy", e);
            }
        }

        // Handle custom objects with parameterized constructors
        try {
            Class<?> clazz = obj.getClass();
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> matchingConstructor = null;
            Object[] constructorArgs = null;

            for (Constructor<?> constructor : constructors) {
                constructor.setAccessible(true);
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                constructorArgs = new Object[parameterTypes.length];

                boolean allArgsValid = true;
                for (int i = 0; i < parameterTypes.length; i++) {
                    constructorArgs[i] = createArgumentCopy(parameterTypes[i]);
                    if (constructorArgs[i] == null && !parameterTypes[i].isPrimitive()) {
                        allArgsValid = false;
                        break;
                    }
                }

                if (allArgsValid) {
                    matchingConstructor = constructor;
                    break;
                }
            }

            if (matchingConstructor == null) {
                throw new RuntimeException("No suitable constructor found for class: " + clazz.getName());
            }

            T newInstance = (T) matchingConstructor.newInstance(constructorArgs);
            copiedObjects.put(obj, newInstance);

            for (Field field : getAllFields(clazz)) {
                field.setAccessible(true);
                field.set(newInstance, deepCopy(field.get(obj)));
            }

            return newInstance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create a deep copy of object", e);
        }
    }

    private static Object createArgumentCopy(Class<?> parameterType) {
        if (parameterType.isPrimitive() || isImmutable(parameterType)) {
            return getDefaultValue(parameterType);
        } else {
            try {
                return deepCopy(parameterType.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static <T> boolean isImmutable(T obj) {
        return obj.getClass().isPrimitive() ||
                obj instanceof CharSequence ||
                obj instanceof Boolean ||
                obj instanceof Character ||
                obj instanceof Number;

    }

    static boolean isImmutableCollection(Collection<?> collection) {
        return collection.getClass().getName().startsWith("java.util.ImmutableCollections$");
    }

    private static boolean isImmutableMap(Map<?, ?> map) {
        return map.getClass().getName().startsWith("java.util.ImmutableCollections$");
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    private static Object getDefaultValue(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) {
                return false;
            } else if (clazz == char.class) {
                return '\0';
            } else if (clazz == byte.class || clazz == short.class || clazz == int.class || clazz == long.class) {
                return 0;
            } else if (clazz == float.class) {
                return 0.0f;
            } else if (clazz == double.class) {
                return 0.0;
            }
        }
        return null;
    }


}
