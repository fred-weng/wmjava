//<editor-fold>
/**
 * A mathematical interval apply to the value of current field.
 * such as (a,b) (a,b],[a,b),[a,b],(,b),(a,)
 */
package j.v;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {
   String value();
   String desc() default "";
}
//</editor-fold>