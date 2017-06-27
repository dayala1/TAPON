package utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class ReflectionUtils {
	public static Type getParameterClass(Object object){
		assert object != null;
		Class objectClass;
		Type objectSuperClass;
		Type objectParameter;
		
		objectClass = object.getClass();
		objectSuperClass = objectClass.getGenericSuperclass();
		objectParameter = ((ParameterizedType)objectSuperClass).getActualTypeArguments()[0];

		return objectParameter;
	}
}
