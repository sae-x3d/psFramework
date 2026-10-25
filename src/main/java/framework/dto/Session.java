package framework.dto;

import java.util.HashMap;
import java.util.Map;

public class Session {

    private final Map<String, Object> attributs = new HashMap<>();

    public void set(String nom, Object valeur) {
        attributs.put(nom, valeur);
    }

    public Object get(String nom) {
        return attributs.get(nom);
    }

    public boolean has(String nom) {
        return attributs.containsKey(nom);
    }

    public void remove(String nom) {
        attributs.remove(nom);
    }

    public Map<String, Object> getAttributs() {
        return attributs;
    }

    public void clear() {
        attributs.clear();
    }

    @Override
    public String toString() {
        return "Session" + attributs;
    }
}
