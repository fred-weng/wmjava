//<editor-fold>
/**
 * @author weng mingjun
 * indicate whether a class opens the annotation check function
 */
package j.v;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Valid {}
//</editor-fold>