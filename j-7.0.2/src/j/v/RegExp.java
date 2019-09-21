//<editor-fold>
/**
 * Indicate the value of the current field must match the pattern of the regular expression.
 * if the type of the field is not a string , convert it into a string with toString
 */
package j.v;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegExp {
    
   String value();
   String desc() default "";
}
//</editor-fold>