//<editor-fold>
/*
  indicate the value of the current Field cannot be null
*/
package j.v;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Require {
   String value() default "";
}
//</editor-fold>