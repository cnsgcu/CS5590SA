package home.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({
    ElementType.TYPE,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.PACKAGE,
    ElementType.TYPE_USE,
    ElementType.PARAMETER,
    ElementType.CONSTRUCTOR,
    ElementType.TYPE_PARAMETER,
    ElementType.LOCAL_VARIABLE,
    ElementType.ANNOTATION_TYPE,
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Feature
{
    String value();
}
