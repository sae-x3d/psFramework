package framework.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class ToJson {

    public static String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        write(sb, obj);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Object obj) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String || obj instanceof Character) {
            writeString(sb, obj.toString());
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
        } else if (obj instanceof Map) {
            writeMap(sb, (Map<?, ?>) obj);
        } else if (obj instanceof Collection) {
            writeCollection(sb, (Collection<?>) obj);
        } else if (obj.getClass().isArray()) {
            writeArray(sb, obj);
        } else if (obj instanceof Enum) {
            writeString(sb, ((Enum<?>) obj).name());
        } else {
            writeObject(sb, obj);
        }
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private static void writeMap(StringBuilder sb, Map<?, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            writeString(sb, String.valueOf(entry.getKey()));
            sb.append(':');
            write(sb, entry.getValue());
        }
        sb.append('}');
    }

    private static void writeCollection(StringBuilder sb, Collection<?> col) {
        sb.append('[');
        boolean first = true;
        for (Object o : col) {
            if (!first) sb.append(',');
            first = false;
            write(sb, o);
        }
        sb.append(']');
    }

    private static void writeArray(StringBuilder sb, Object array) {
        sb.append('[');
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(',');
            write(sb, Array.get(array, i));
        }
        sb.append(']');
    }

    private static void writeObject(StringBuilder sb, Object obj) {
        sb.append('{');
        boolean first = true;
        for (Method m : obj.getClass().getMethods()) {
            if (m.getParameterCount() != 0 || m.getReturnType() == void.class) continue;
            if (m.getDeclaringClass() == Object.class) continue;

            String name = m.getName();
            String prop;
            if (name.startsWith("get") && name.length() > 3) {
                prop = decapitalize(name.substring(3));
            } else if (name.startsWith("is") && name.length() > 2
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                prop = decapitalize(name.substring(2));
            } else {
                continue;
            }

            Object value;
            try {
                value = m.invoke(obj);
            } catch (Exception e) {
                continue;
            }

            if (!first) sb.append(',');
            first = false;
            writeString(sb, prop);
            sb.append(':');
            write(sb, value);
        }
        sb.append('}');
    }

    private static String decapitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
