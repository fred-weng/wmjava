//<editor-fold>
/**
 * XResult<List<Integer>> result = JSON.parseObject("{'errorCode':'000000','errorMessage':'','data':[1,2,3,4,5]}", PType.compose(XResult.class, PType.compose(List.class, Integer.class)));
 */
package j.m;

public class XResult<T> {

    private String errorCode;
    private String errorMessage;
    private T data;

    public XResult() {
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean ok() {
        return "0".equals(this.errorCode);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public XResult(String errorCode, String errorMessage, T data) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    @Override
    public String toString() {
        return this.toJSON();
    }

    public String toJSON() {
        return JSON.toJSON(this);
    }

    public String toPrettyJSON() {
        return JSON.toPrettyJSON(this);
    }

    @SuppressWarnings("unchecked")
    public static <U> XResult<U> fromXMap(XMap m, Class<U> clazz) {
        if (m == null)
            return new XResult<>();
        return m.toObject(XResult.class, clazz);
    }

    public static <U> XResult<U> succeed(U data) {
        return new XResult<>("0", null, data);
    }

    public static <U> XResult<U> error(String errorCode, String errorMessage, U data) {
        return new XResult<>(errorCode, errorMessage, data);
    }

    public static <U> XResult<U> error(String errorCode, String errorMessage) {
        return new XResult<>(errorCode, errorMessage, null);
    }

    public static <U> XResult<U> error(String errorCode) {
        return new XResult<>(errorCode, null, null);
    }
}
//</editor-fold>
