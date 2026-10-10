package framework.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class Binder {

    public static Object[] bind(HttpServletRequest req, HttpServletResponse resp, Method method) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();

            if (type == HttpServletRequest.class) {
                args[i] = req;
            } else if (type == HttpServletResponse.class) {
                args[i] = resp;
            } else if (type == HttpSession.class) {
                args[i] = req.getSession();
            } else if (type == String.class || type == int.class || type == boolean.class) {
                String nom = nomParametre(params[i], method);
                args[i] = convertirNonObjet(req.getParameter(nom), type, nom, method.getName());
            } else {
                String nom = nomParametre(params[i], method);
                args[i] = construireObjet(req, type, nom, method.getName());
            }
        }
        return args;
    }

    private static Object convertirNonObjet(String brut, Class<?> type, String nom, String methode) {
        if (type == String.class) {
            return brut;
        }
        if (type == int.class) {
            if (brut == null || brut.isBlank()) {
                return 0;
            }
            try {
                return Integer.valueOf(brut);
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        "Paramètre '" + nom + "' de " + methode + " : '" + brut + "' n'est pas un entier");
            }
        }
        if (type == boolean.class) {
            return "true".equalsIgnoreCase(brut) || "on".equalsIgnoreCase(brut) || "1".equals(brut);
        }
        throw new RuntimeException(
                "Type de paramètre non supporté : " + type.getName() + " (méthode " + methode + ")");
    }

    private static Object construireObjet(HttpServletRequest req, Class<?> type, String nom, String methode) {
        if (type.isInterface()) {
            throw new RuntimeException(
                    "Type " + type.getName() + " : interface, impossible à construire (méthode " + methode + ")");
        }

        Object instance;
        try {
            Constructor<?> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            instance = ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible d'instancier " + type.getName() + " (méthode " + methode + ") : " + e.getMessage(), e);
        }

        for (Method setter : type.getMethods()) {
            if (!setter.getName().startsWith("set") || setter.getName().length() <= 3) continue;
            if (setter.getParameterCount() != 1) continue;

            String prop = decapitalize(setter.getName().substring(3));
            String brut = req.getParameter(prop);
            if (brut == null) continue;

            Object valeur = convertirNonObjet(brut, setter.getParameterTypes()[0],
                    type.getSimpleName() + "." + prop, methode);
            try {
                setter.invoke(instance, valeur);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Setter " + type.getSimpleName() + "." + setter.getName()
                        + " a échoué (méthode " + methode + ") : " + e.getMessage(), e);
            }
        }
        return instance;
    }

    private static String nomParametre(Parameter p, Method method) {
        if (!p.isNamePresent()) {
            throw new RuntimeException(
                    "Nom du paramètre indisponible pour " + method.getName()
                    + " : recompiler avec l'option -parameters");
        }
        return p.getName();
    }

    private static String decapitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
