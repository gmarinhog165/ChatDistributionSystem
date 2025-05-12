package pt.uminho.di;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ORSet {
    private Map<String, Set<Dot>> dotMap;
    private Map<String, Integer> causalContext;

    public ORSet() {
        this.dotMap = new HashMap<>();
        this.causalContext = new HashMap<>();
    }

    public Map<String, Integer> getCausalContext() {
        return causalContext;
    }

    public Map<String, Set<Dot>> getDotMap() {
        return dotMap;
    }

    public void setCausalContext(Map<String, Integer> cc) {
        this.causalContext = cc;
    }

    public void setDotMap(Map<String, Set<Dot>> dm) {
        this.dotMap = dm;
    }

    public void initCausalContext(Set<String> servers) {
        for (String server : servers) {
            this.causalContext.put(server, 0);
        }
    }

    public void add(String server, String element) {
        this.causalContext.put(server, this.causalContext.get(server) + 1);
        int dotValue = this.causalContext.get(server);
        Dot newDot = new Dot(server, dotValue);
        this.dotMap.computeIfAbsent(element, k -> new HashSet<>()).add(newDot);
    }

    public void remove(String element) {
        this.dotMap.remove(element);
    }

    public Set<String> elements() {
        return this.dotMap.keySet();
    }

    public void join(ORSet other) {
        // Atualiza o contexto causal (join dos contextos)
        for (Map.Entry<String, Integer> entry : other.causalContext.entrySet()) {
            String server = entry.getKey();
            int version = entry.getValue();
            this.causalContext.put(server, Math.max(this.causalContext.getOrDefault(server, 0), version));
        }

        // União de todas as chaves (elementos)
        Set<String> allElements = new HashSet<>(this.dotMap.keySet());
        allElements.addAll(other.dotMap.keySet());

        for (String element : allElements) {
            Set<Dot> selfDots = this.dotMap.getOrDefault(element, Set.of());
            Set<Dot> otherDots = other.dotMap.getOrDefault(element, Set.of());

            Set<Dot> survivingDots = new HashSet<>();

            // Dots do próprio ORSet (this)
            for (Dot dot : selfDots) {
                int ccOther = other.causalContext.getOrDefault(dot.getServer(), 0);
                boolean covered = dot.getValue() <= ccOther;
                boolean inOther = otherDots.contains(dot);
                if (!covered || inOther) {
                    survivingDots.add(dot);
                }
            }

            // Dots do ORSet recebido (other)
            for (Dot dot : otherDots) {
                int ccSelf = this.causalContext.getOrDefault(dot.getServer(), 0);
                boolean covered = dot.getValue() <= ccSelf;
                boolean inSelf = selfDots.contains(dot);
                if (!covered || inSelf) {
                    survivingDots.add(dot);
                }
            }

            // Atualiza dotMap apenas se houver sobreviventes
            if (!survivingDots.isEmpty()) {
                this.dotMap.put(element, survivingDots);
            } else {
                this.dotMap.remove(element);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ORSet {\n");

        sb.append("  Causal Context:\n");
        for (Map.Entry<String, Integer> entry : causalContext.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        sb.append("  Dot Map:\n");
        for (Map.Entry<String, Set<Dot>> entry : dotMap.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ");
            sb.append("[");
            for (Dot dot : entry.getValue()) {
                sb.append("(").append(dot.getServer()).append(",").append(dot.getValue()).append("), ");
            }
            if (!entry.getValue().isEmpty()) {
                sb.setLength(sb.length() - 2); // remove trailing comma and space
            }
            sb.append("]\n");
        }

        sb.append("}");
        return sb.toString();
    }

}
