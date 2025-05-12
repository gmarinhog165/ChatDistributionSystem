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
        // Atualiza o contexto causal
        for (Map.Entry<String, Integer> entry : other.causalContext.entrySet()) {
            String server = entry.getKey();
            int version = entry.getValue();
            this.causalContext.put(server, Math.max(this.causalContext.getOrDefault(server, 0), version));
        }

        // Processa os elementos de ambos os sets
        Set<String> allElements = new HashSet<>(this.dotMap.keySet());
        allElements.addAll(other.dotMap.keySet());

        for (String element : allElements) {
            Set<Dot> selfDots = this.dotMap.getOrDefault(element, new HashSet<>());
            Set<Dot> otherDots = other.dotMap.getOrDefault(element, new HashSet<>());

            Set<Dot> survivingSelfDots = new HashSet<>();
            for (Dot dot : selfDots) {
                if (!other.causalContext.containsKey(dot.getServer()) || dot.getValue() > other.causalContext.get(dot.getServer())) {
                    survivingSelfDots.add(dot);
                }
            }

            Set<Dot> survivingOtherDots = new HashSet<>();
            for (Dot dot : otherDots) {
                if (!this.causalContext.containsKey(dot.getServer()) || dot.getValue() > this.causalContext.get(dot.getServer())) {
                    survivingOtherDots.add(dot);
                }
            }

            survivingSelfDots.addAll(survivingOtherDots);

            if (!survivingSelfDots.isEmpty()) {
                this.dotMap.put(element, survivingSelfDots);
            } else {
                this.dotMap.remove(element); // limpa se não sobrarem dots
            }
        }
    }

    private static class Dot {
        private String server;
        private int value;

        public Dot(String server, int value) {
            this.server = server;
            this.value = value;
        }

        public String getServer() {
            return server;
        }

        public int getValue() {
            return value;
        }
    }
}
