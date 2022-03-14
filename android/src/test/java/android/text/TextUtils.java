package android.text;

import androidx.annotation.NonNull;

/**
 * This is a trick to make unit test run faster and do not depend on TextUtils
 * More detail at
 * http://sangsoonam.github.io/2018/12/02/is-textutils-isempty-evil.html
 */
public class TextUtils {
    public static String join(@NonNull CharSequence delimiter, @NonNull Object[] tokens) {
        final int length = tokens.length;
        if (length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(tokens[0]);
        for (int i = 1; i < length; i++) {
            sb.append(delimiter);
            sb.append(tokens[i]);
        }
        return sb.toString();
    }
}
