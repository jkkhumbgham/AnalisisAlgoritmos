import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuncionesJuego {

    // estado de trazas para el solver automático
    private static boolean solverEnEjecucion = false;
    private static List<String> logSolver = new ArrayList<>();
    private static List<List<String>> ultimoEstadoSolver = null;

    private static void logDecision(String msg) {
        if (solverEnEjecucion) logSolver.add(msg);
    }

    private static void actualizarUltimoEstado(List<List<String>> mapa) {
        if (!solverEnEjecucion) return;
        ultimoEstadoSolver = clonarMapa(mapa);
    }

    //La funcion revisa si jugada esta en el mapa
    private static boolean enRango(int x, int y, List<List<String>> mapa) {
        return x >= 0 && x < mapa.size() && y >= 0 && y < mapa.get(0).size();
    }

    //funcion revisa si la celda dada es una isla o no
    private static boolean esIsla(String celda) {
        return celda != null && celda.matches("[1-8](\\s*[-=|║]*)?");
    }

    // número de la isla
    private static int numeroIsla(String cel) {
        if (cel == null) return 0;
        cel = cel.trim();
        Matcher m = Pattern.compile("^\\d").matcher(cel);
        if (m.find()) return Integer.parseInt(m.group());
        return 0;
    }

    // reemplaza o añade símbolo si es 1 o dos puentes
    private static String editarSimbolo(String cell, String symbol) {
        if (cell == null) cell = "";
        String trimmed = cell.trim();
        if (trimmed.contains(symbol)) return trimmed;

        if (symbol.equals("-") || symbol.equals("=")) {
            String base = trimmed.replaceAll("[-=]+$", "");
            return base + symbol;
        } else {
            String base = trimmed.replaceAll("[|║]+$", "");
            return base + symbol;
        }
    }

    // cuenta los puentes por una isla
    private static int contarPuentesIsla(List<List<String>> mapa, int x, int y) {
        int total = 0;
        // contamos puentes por las celdas adyacentes (no dependemos de símbolos insertados en la isla)
        if (enRango(x, y - 1, mapa)) {
            String celda = mapa.get(x).get(y - 1).trim();
            if (celda.contains("=")) total += 2;
            else if (celda.contains("-")) total += 1;
        }
        if (enRango(x, y + 1, mapa)) {
            String celda = mapa.get(x).get(y + 1).trim();
            if (celda.contains("=")) total += 2;
            else if (celda.contains("-")) total += 1;
        }
        if (enRango(x - 1, y, mapa)) {
            String celda = mapa.get(x - 1).get(y).trim();
            if (celda.contains("║")) total += 2;
            else if (celda.contains("|")) total += 1;
        }
        if (enRango(x + 1, y, mapa)) {
            String celda = mapa.get(x + 1).get(y).trim();
            if (celda.contains("║")) total += 2;
            else if (celda.contains("|")) total += 1;
        }
        return total;
    }


    // cuántos puentes ya existen entre 2 islas
    private static int puentesEntre(List<List<String>> mapa, int x1, int y1, int x2, int y2) {
        if (x1 == x2) { // horizontal
            int fila = x1;
            for (int j = Math.min(y1,y2)+1; j < Math.max(y1,y2); j++) {
                String cel = mapa.get(fila).get(j).trim();
                if (cel.contains("=")) return 2;
                if (cel.contains("-")) return 1;
            }
        } else if (y1 == y2) { // vertical
            int col = y1;
            for (int i = Math.min(x1,x2)+1; i < Math.max(x1,x2); i++) {
                String cel = mapa.get(i).get(col).trim();
                if (cel.contains("║")) return 2;
                if (cel.contains("|")) return 1;
            }
        }
        return 0;
    }

    // verifica que el camino entre dos islas esté libre de cruces o islas intermedias
    private static boolean caminoLibre(List<List<String>> mapa, int x1, int y1, int x2, int y2) {
        if (x1 == x2) { // horizontal
            for (int j = Math.min(y1,y2)+1; j < Math.max(y1,y2); j++) {
                String cel = mapa.get(x1).get(j).trim();
                if (esIsla(cel)) return false;
                if (cel.equals("|") || cel.equals("║")) return false; // cruce vertical
                // permitimos "-" o "=" porque pueden ser parte del mismo puente a reforzar
            }
        } else if (y1 == y2) { // vertical
            for (int i = Math.min(x1,x2)+1; i < Math.max(x1,x2); i++) {
                String cel = mapa.get(i).get(y1).trim();
                if (esIsla(cel)) return false;
                if (cel.equals("-") || cel.equals("=")) return false; // cruce horizontal
                // permitimos "|" o "║" porque pueden ser parte del mismo puente a reforzar
            }
        }
        return true;
    }

    // clona el mapa para backtracking
    private static List<List<String>> clonarMapa(List<List<String>> mapa) {
        List<List<String>> copia = new ArrayList<>();
        for (List<String> fila : mapa) {
            copia.add(new ArrayList<>(fila));
        }
        return copia;
    }

    // sobreescribe el contenido de un mapa con otro manteniendo la referencia externa
    private static void sobreescribirMapa(List<List<String>> destino, List<List<String>> origen) {
        destino.clear();
        for (List<String> fila : origen) {
            destino.add(new ArrayList<>(fila));
        }
    }

    // expone el log acumulado del solver
    public static List<String> obtenerLogSolver() {
        return new ArrayList<>(logSolver);
    }

    // obtiene todas las islas del mapa
    private static List<int[]> obtenerIslas(List<List<String>> mapa) {
        List<int[]> islas = new ArrayList<>();
        for (int i = 0; i < mapa.size(); i++) {
            for (int j = 0; j < mapa.get(0).size(); j++) {
                if (esIsla(mapa.get(i).get(j))) {
                    islas.add(new int[]{i, j});
                }
            }
        }
        return islas;
    }


    /**
     * Intenta resolver el tablero automáticamente aplicando heurísticas y backtracking.
     * Retorna true si encuentra solución y deja el mapa actualizado; false si no hay solución.
     */
    public static boolean resolverAutomaticamente(List<List<String>> mapa) {
        if (mapa == null) return false;
        solverEnEjecucion = true;
        logSolver.clear();
        ultimoEstadoSolver = clonarMapa(mapa);
        List<List<String>> trabajo = clonarMapa(mapa);
        boolean exito = resolverPorAristas(trabajo);
        if (exito) {
            sobreescribirMapa(mapa, trabajo);
        } else if (ultimoEstadoSolver != null) {
            sobreescribirMapa(mapa, ultimoEstadoSolver);
        }
        solverEnEjecucion = false;
        return exito;
    }

    // Debug: imprime estado de cada isla (capacidad vs puentes usados)
    public static void imprimirEstadoIslas(List<List<String>> mapa) {
        List<int[]> islas = obtenerIslas(mapa);
        for (int[] isla : islas) {
            int x = isla[0], y = isla[1];
            int capacidad = numeroIsla(mapa.get(x).get(y));
            int usados = contarPuentesIsla(mapa, x, y);
            System.out.println(" Isla (" + x + "," + y + ") cap=" + capacidad + " usados=" + usados);
        }
    }

    private static class Arista {
        int a, b;
        boolean horizontal;
        List<int[]> celdas = new ArrayList<>();
    }

    private static List<Arista> construirAristas(List<List<String>> mapa, List<int[]> islas, Map<String, Integer> indiceIsla) {
        List<Arista> aristas = new ArrayList<>();
        int[] dx = {0, 1};
        int[] dy = {1, 0};

        for (int idx = 0; idx < islas.size(); idx++) {
            int x = islas.get(idx)[0];
            int y = islas.get(idx)[1];
            for (int dir = 0; dir < 2; dir++) { // derecha y abajo para evitar duplicados
                int nx = x + dx[dir];
                int ny = y + dy[dir];
                List<int[]> celdas = new ArrayList<>();
                while (enRango(nx, ny, mapa)) {
                    String cel = mapa.get(nx).get(ny);
                    if (esIsla(cel)) {
                        int otro = indiceIsla.get(nx + "," + ny);
                        Arista a = new Arista();
                        a.a = idx;
                        a.b = otro;
                        a.horizontal = (x == nx);
                        a.celdas = celdas;
                        aristas.add(a);
                        break;
                    }
                    String trimmed = (cel == null) ? "" : cel.trim();
                    // No avanzamos a través de cruces de orientación opuesta, terminamos búsqueda
                    if (dx[dir] == 0 && (trimmed.equals("|") || trimmed.equals("║"))) break;
                    if (dy[dir] == 0 && (trimmed.equals("-") || trimmed.equals("="))) break;

                    celdas.add(new int[]{nx, ny});
                    nx += dx[dir];
                    ny += dy[dir];
                }
            }
        }
        return aristas;
    }

    // cruces precomputados entre aristas
    private static Map<Integer, List<Integer>> calcularCruces(List<Arista> aristas, List<int[]> islas) {
        Map<Integer, List<Integer>> cruces = new HashMap<>();
        for (int i = 0; i < aristas.size(); i++) cruces.put(i, new ArrayList<>());

        for (int i = 0; i < aristas.size(); i++) {
            Arista h = aristas.get(i);
            if (!h.horizontal) continue;
            // obtener rango horizontal de h
            int hx = islas.get(h.a)[0];
            int hy1 = Math.min(islas.get(h.a)[1], islas.get(h.b)[1]);
            int hy2 = Math.max(islas.get(h.a)[1], islas.get(h.b)[1]);
            for (int j = 0; j < aristas.size(); j++) {
                Arista v = aristas.get(j);
                if (v.horizontal) continue;
                int vy1 = islas.get(v.a)[0];
                int vy2 = islas.get(v.b)[0];
                int vx = islas.get(v.a)[1];
                // cruzan en un punto interno (no en islas)
                if (vx > hy1 && vx < hy2 && hx > vy1 && hx < vy2) {
                    cruces.get(i).add(j);
                    cruces.get(j).add(i);
                }
            }
        }
        return cruces;
    }

    private static boolean asignarAristasDFS(List<Arista> aristas,
                                             int[] asignacion,
                                             int[] restante,
                                             Map<Integer, List<Integer>> cruces,
                                             List<int[]> islas) {
        boolean quedanSinAsignar = false;
        for (int val : asignacion) if (val == -1) { quedanSinAsignar = true; break; }

        if (!quedanSinAsignar) {
            // todas asignadas, verificar que todas las islas quedaron completas
            for (int r : restante) if (r != 0) return false;
            // conectividad
            Map<Integer, Set<Integer>> grafo = new HashMap<>();
            for (int i = 0; i < islas.size(); i++) grafo.put(i, new HashSet<>());
            for (int i = 0; i < aristas.size(); i++) {
                if (asignacion[i] > 0) {
                    Arista a = aristas.get(i);
                    grafo.get(a.a).add(a.b);
                    grafo.get(a.b).add(a.a);
                }
            }
            Set<Integer> vis = new HashSet<>();
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(0);
            vis.add(0);
            while (!stack.isEmpty()) {
                int cur = stack.pop();
                for (int nxt : grafo.get(cur)) if (vis.add(nxt)) stack.push(nxt);
            }
            return vis.size() == islas.size();
        }

        // elegir arista con menor dominio restante (heurística)
        int best = -1;
        int bestOpciones = Integer.MAX_VALUE;
        for (int i = 0; i < aristas.size(); i++) {
            if (asignacion[i] != -1) continue;
            Arista a = aristas.get(i);
            int capMax = Math.min(2, Math.min(restante[a.a], restante[a.b]));
            // si algún cruce ya tiene valor >0, esta debe ser 0
            boolean bloqueada = false;
            for (int c : cruces.get(i)) {
                if (asignacion[c] > 0) bloqueada = true;
            }
            int opciones = bloqueada ? 1 : capMax + 1; // incluye 0
            if (opciones < bestOpciones) {
                bestOpciones = opciones;
                best = i;
            }
        }
        if (best == -1) return false;

        // intentar asignaciones para la arista seleccionada
        Arista seleccionada = aristas.get(best);
        boolean bloqueada = false;
        for (int c : cruces.get(best)) if (asignacion[c] > 0) bloqueada = true;
        int capMax = Math.min(2, Math.min(restante[seleccionada.a], restante[seleccionada.b]));

        List<Integer> valores = new ArrayList<>();
        if (bloqueada) {
            valores.add(0);
        } else {
            for (int v = capMax; v >= 0; v--) valores.add(v);
        }

        asignacion[best] = 0; // evitar reuso en recursión
        for (int val : valores) {
            int ra = restante[seleccionada.a] - val;
            int rb = restante[seleccionada.b] - val;
            if (ra < 0 || rb < 0) continue;
            restante[seleccionada.a] = ra;
            restante[seleccionada.b] = rb;
            asignacion[best] = val;

            // poda simple: si alguna isla queda sin suficiente potencial
            boolean valido = true;
            for (int i = 0; i < restante.length && valido; i++) {
                if (restante[i] == 0) continue;
                int potencial = 0;
                for (int j = 0; j < aristas.size(); j++) {
                    if (asignacion[j] != -1) continue;
                    Arista a = aristas.get(j);
                    if (a.a != i && a.b != i) continue;
                    // si está bloqueada por cruce activo, solo aporta 0
                    boolean bloqueado = false;
                    for (int c : cruces.get(j)) if (asignacion[c] > 0) bloqueado = true;
                    if (bloqueado) continue;
                    potencial += 2; // valor máximo posible por arista
                }
                if (potencial < restante[i]) valido = false;
            }

            if (valido && asignarAristasDFS(aristas, asignacion, restante, cruces, islas)) {
                return true;
            }

            // backtrack
            restante[seleccionada.a] += val;
            restante[seleccionada.b] += val;
        }

        asignacion[best] = -1;
        return false;
    }

    private static boolean resolverPorAristas(List<List<String>> mapa) {
        List<int[]> islas = obtenerIslas(mapa);
        Map<String, Integer> indiceIsla = new HashMap<>();
        for (int i = 0; i < islas.size(); i++) {
            int[] pos = islas.get(i);
            indiceIsla.put(pos[0] + "," + pos[1], i);
        }

        List<Arista> aristas = construirAristas(mapa, islas, indiceIsla);
        Map<Integer, List<Integer>> cruces = calcularCruces(aristas, islas);

        int[] restante = new int[islas.size()];
        for (int i = 0; i < islas.size(); i++) {
            int cap = numeroIsla(mapa.get(islas.get(i)[0]).get(islas.get(i)[1]));
            int usados = contarPuentesIsla(mapa, islas.get(i)[0], islas.get(i)[1]);
            restante[i] = cap - usados;
            if (restante[i] < 0) return false;
        }

        int[] asignacion = new int[aristas.size()];
        Arrays.fill(asignacion, -1);

        if (!asignarAristasDFS(aristas, asignacion, restante, cruces, islas)) return false;

        // aplicar solución al mapa
        for (int i = 0; i < aristas.size(); i++) {
            int val = asignacion[i];
            if (val <= 0) continue;
            Arista a = aristas.get(i);
            String fill;
            if (a.horizontal) fill = (val == 1) ? "-" : "=";
            else fill = (val == 1) ? "|" : "║";
            for (int[] c : a.celdas) {
                mapa.get(c[0]).set(c[1], fill);
            }
            logDecision("Aristas: asigna " + val + " entre isla " + a.a + " y " + a.b + (a.horizontal ? " (H)" : " (V)"));
            actualizarUltimoEstado(mapa);
        }
        return true;
    }
    //funcion principal verifica jugada y agrega puente
    public static boolean agregarPuente(int x1, int y1, int x2, int y2, int cantidad, List<List<String>> mapa) {
        if (!enRango(x1, y1, mapa) || !enRango(x2, y2, mapa)) {
            System.out.println(" Coordenadas fuera de rango.");
            return false;
        }
        String origen = mapa.get(x1).get(y1);
        String destino = mapa.get(x2).get(y2);
        if (!esIsla(origen) || !esIsla(destino)) {
            System.out.println(" Ambos extremos deben ser islas (1-8).");
            return false;
        }

        boolean horizontal = (x1 == x2);
        boolean vertical = (y1 == y2);
        if (!horizontal && !vertical) {
            System.out.println(" Solo se permiten puentes horizontales o verticales.");
            return false;
        }

        int cap1 = numeroIsla(origen);
        int cap2 = numeroIsla(destino);
        int usados1 = contarPuentesIsla(mapa,x1,y1);
        int usados2 = contarPuentesIsla(mapa,x2,y2);

        if (usados1 + cantidad > cap1 || usados2 + cantidad > cap2) {
            System.out.println(" Una de las islas no puede recibir tantos puentes (sobrecarga).");
            return false;
        }

        int existentes = puentesEntre(mapa,x1,y1,x2,y2);
        if (existentes + cantidad > 2) {
            System.out.println(" Demasiados puentes entre estas islas (máx 2).");
            return false;
        }

        int total = existentes + cantidad;
        String fill = horizontal ? (total==1?"-":"=") : (total==1?"|":"║");

        // Validación de camino libre
        if (horizontal) {
            int fila = x1;
            for (int j=Math.min(y1,y2)+1; j<Math.max(y1,y2); j++) {
                String cel = mapa.get(fila).get(j).trim();
                if (esIsla(cel)) { System.out.println(" Hay una isla en el camino."); return false; }
                if (cel.contains("|") || cel.contains("║")) { System.out.println(" Cruce detectado."); return false; }
            }
            for (int j=Math.min(y1,y2)+1; j<Math.max(y1,y2); j++) {
                mapa.get(fila).set(j, fill);
            }
        } else {
            int col = y1;
            for (int i=Math.min(x1,x2)+1; i<Math.max(x1,x2); i++) {
                String cel = mapa.get(i).get(col).trim();
                if (esIsla(cel)) { System.out.println(" Hay una isla en el camino."); return false; }
                if (cel.contains("-") || cel.contains("=")) { System.out.println(" Cruce detectado."); return false; }
            }
            for (int i=Math.min(x1,x2)+1; i<Math.max(x1,x2); i++) {
                mapa.get(i).set(col, fill);
            }
        }
        logDecision("Puente x1=" + x1 + ",y1=" + y1 + " x2=" + x2 + ",y2=" + y2 + " cant=" + cantidad + " total=" + total + (horizontal ? " (H)" : " (V)"));
        actualizarUltimoEstado(mapa);
        System.out.println("Puente agregado total ahora entre islas: " + total + ".");
        return true;
    }

    /**
     * Verifica si el tablero cumple las condiciones de victoria.
     * Revisa que cada isla tenga exactamente sus puentes, que solo existan símbolos válidos,
     * que todas las islas estén conectadas y que no haya segmentos de puente sueltos.
     */
    public static boolean verificarVictoria(List<List<String>> mapa) {
        if (mapa == null || mapa.isEmpty() || mapa.get(0).isEmpty()) return false;

        int filas = mapa.size();
        int columnas = mapa.get(0).size();

        List<int[]> islas = new ArrayList<>();
        Map<String, Integer> indiceIsla = new HashMap<>();

        // Validamos símbolos y registramos islas
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                String celda = mapa.get(i).get(j);
                String trimmed = (celda == null) ? "" : celda.trim();

                if (esIsla(celda)) {
                    int idx = islas.size();
                    islas.add(new int[]{i, j});
                    indiceIsla.put(i + "," + j, idx);
                } else if (!trimmed.isEmpty()) {
                    if (!trimmed.equals("-") && !trimmed.equals("=")
                            && !trimmed.equals("|") && !trimmed.equals("║")) {
                        return false;
                    }
                }
            }
        }

        if (islas.isEmpty()) return false;

        // Cada isla debe tener exactamente el número de puentes indicado
        for (int[] pos : islas) {
            int x = pos[0];
            int y = pos[1];
            int capacidad = numeroIsla(mapa.get(x).get(y));
            int usados = contarPuentesIsla(mapa, x, y);
            if (capacidad != usados) return false;
        }

        Map<Integer, Set<Integer>> grafo = new HashMap<>();
        for (int idx = 0; idx < islas.size(); idx++) {
            grafo.put(idx, new HashSet<>());
        }

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        // Explora en las 4 direcciones para construir conexiones y detectar puentes inválidos
        for (int idx = 0; idx < islas.size(); idx++) {
            int x = islas.get(idx)[0];
            int y = islas.get(idx)[1];
            for (int dir = 0; dir < 4; dir++) {
                boolean horizontal = dx[dir] == 0;
                boolean vioPuente = false;

                int nx = x + dx[dir];
                int ny = y + dy[dir];
                while (enRango(nx, ny, mapa) && !esIsla(mapa.get(nx).get(ny))) {
                    String celda = mapa.get(nx).get(ny).trim();
                    if (!celda.isEmpty()) {
                        if (horizontal) {
                            if (celda.equals("-") || celda.equals("=")) {
                                vioPuente = true;
                            } else if (celda.equals("|") || celda.equals("║")) {
                                return false;
                            } else {
                                return false;
                            }
                        } else {
                            if (celda.equals("|") || celda.equals("║")) {
                                vioPuente = true;
                            } else if (celda.equals("-") || celda.equals("=")) {
                                return false;
                            } else {
                                return false;
                            }
                        }
                    }
                    nx += dx[dir];
                    ny += dy[dir];
                }

                if (enRango(nx, ny, mapa) && esIsla(mapa.get(nx).get(ny))) {
                    int dist = Math.abs(nx - x) + Math.abs(ny - y);
                    String origen = mapa.get(x).get(y).trim();
                    String destino = mapa.get(nx).get(ny).trim();
                    boolean endpointMarca = horizontal
                            ? (origen.contains("-") || origen.contains("=") || destino.contains("-") || destino.contains("="))
                            : (origen.contains("|") || origen.contains("║") || destino.contains("|") || destino.contains("║"));

                    if (vioPuente || (dist == 1 && endpointMarca)) {
                        int vecinoIdx = indiceIsla.get(nx + "," + ny);
                        grafo.get(idx).add(vecinoIdx);
                        grafo.get(vecinoIdx).add(idx);
                    }
                } else {
                    // Si vimos un puente pero no termina en isla, hay un puente suelto
                    if (vioPuente) return false;
                }
            }
        }

        // Comprobamos conectividad de todas las islas
        Set<Integer> visitadas = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        visitadas.add(0);

        while (!stack.isEmpty()) {
            int actual = stack.pop();
            for (int vecino : grafo.get(actual)) {
                if (!visitadas.contains(vecino)) {
                    visitadas.add(vecino);
                    stack.push(vecino);
                }
            }
        }

        return visitadas.size() == islas.size();
    }

    /**
     * Verifica condiciones de derrota: estado inválido por símbolos erróneos,
     * puentes cruzados/sueltos o islas sobrecargadas de puentes.
     * Además detecta estados sin solución: componentes desconectados sin posibilidad de unión
     * o islas insatisfechas que ya no pueden conectarse.
     */
    public static boolean verificarDerrota(List<List<String>> mapa) {
        if (mapa == null || mapa.isEmpty() || mapa.get(0).isEmpty()) return true;

        int filas = mapa.size();
        int columnas = mapa.get(0).size();

        List<int[]> islas = new ArrayList<>();
        Map<String, Integer> indiceIsla = new HashMap<>();

        // Detecta símbolos inválidos e islas sobrecargadas
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                String celda = mapa.get(i).get(j);
                String trimmed = (celda == null) ? "" : celda.trim();

                if (esIsla(celda)) {
                    int capacidad = numeroIsla(celda);
                    int usados = contarPuentesIsla(mapa, i, j);
                    if (usados > capacidad) return true;
                    int idx = islas.size();
                    islas.add(new int[]{i, j});
                    indiceIsla.put(i + "," + j, idx);
                } else if (!trimmed.isEmpty()) {
                    if (!trimmed.equals("-") && !trimmed.equals("=")
                            && !trimmed.equals("|") && !trimmed.equals("║")) {
                        return true;
                    }
                }
            }
        }

        if (islas.isEmpty()) return true;

        Map<Integer, Set<Integer>> grafo = new HashMap<>();
        for (int idx = 0; idx < islas.size(); idx++) {
            grafo.put(idx, new HashSet<>());
        }

        // Revisa puentes sueltos o cruzados partiendo desde cada isla y arma grafo actual
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        for (int[] isla : islas) {
            int x = isla[0];
            int y = isla[1];
            int idxIsla = indiceIsla.get(x + "," + y);
            for (int dir = 0; dir < 4; dir++) {
                boolean horizontal = dx[dir] == 0;
                boolean vioPuente = false;

                int nx = x + dx[dir];
                int ny = y + dy[dir];
                while (enRango(nx, ny, mapa) && !esIsla(mapa.get(nx).get(ny))) {
                    String celda = mapa.get(nx).get(ny).trim();
                    if (!celda.isEmpty()) {
                        if (horizontal) {
                            if (celda.equals("-") || celda.equals("=")) {
                                vioPuente = true;
                            } else {
                                return true; // puente vertical cruzado o símbolo inválido
                            }
                        } else {
                            if (celda.equals("|") || celda.equals("║")) {
                                vioPuente = true;
                            } else {
                                return true; // puente horizontal cruzado o símbolo inválido
                            }
                        }
                    }
                    nx += dx[dir];
                    ny += dy[dir];
                }

                // Si se encontraron segmentos de puente pero no hay isla al final, es derrota
                if (vioPuente) {
                    if (enRango(nx, ny, mapa) && esIsla(mapa.get(nx).get(ny))) {
                        int idxVecino = indiceIsla.get(nx + "," + ny);
                        grafo.get(idxIsla).add(idxVecino);
                        grafo.get(idxVecino).add(idxIsla);
                    } else {
                        return true;
                    }
                }
            }
        }

        // Calculamos componentes de conectividad con puentes actuales
        int n = islas.size();
        int[] componente = new int[n];
        for (int i = 0; i < n; i++) componente[i] = -1;
        int compId = 0;
        for (int i = 0; i < n; i++) {
            if (componente[i] != -1) continue;
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(i);
            componente[i] = compId;
            while (!stack.isEmpty()) {
                int actual = stack.pop();
                for (int vecino : grafo.get(actual)) {
                    if (componente[vecino] == -1) {
                        componente[vecino] = compId;
                        stack.push(vecino);
                    }
                }
            }
            compId++;
        }

        boolean posibleUnion = false;

        // Verifica si hay al menos un puente posible entre componentes distintos
        for (int idx = 0; idx < n; idx++) {
            int x = islas.get(idx)[0];
            int y = islas.get(idx)[1];
            int capacidad = numeroIsla(mapa.get(x).get(y));
            int usados = contarPuentesIsla(mapa, x, y);
            int espacioRestante = capacidad - usados;

            boolean tienePosibleConexion = false;

            for (int dir = 0; dir < 4; dir++) {
                int nx = x + dx[dir];
                int ny = y + dy[dir];
                while (enRango(nx, ny, mapa)) {
                    String cel = mapa.get(nx).get(ny);
                    if (esIsla(cel)) {
                        int idxVecino = indiceIsla.get(nx + "," + ny);
                        int capVec = numeroIsla(cel);
                        int usadosVec = contarPuentesIsla(mapa, nx, ny);
                        int espacioVec = capVec - usadosVec;
                        int existentes = puentesEntre(mapa, x, y, nx, ny);

                        // hay posibilidad de conectar si no excede 2 y hay espacio en ambas islas
                        if (existentes < 2 && espacioRestante > 0 && espacioVec > 0
                                && caminoLibre(mapa, x, y, nx, ny)) {
                            tienePosibleConexion = true;
                            if (componente[idx] != componente[idxVecino]) {
                                posibleUnion = true;
                            }
                        }
                        break; // solo la isla más cercana en esa dirección es relevante
                    }

                    String trimmed = (cel == null) ? "" : cel.trim();
                    // Si hay un puente en la dirección opuesta, no podremos conectar en este eje
                    if (dx[dir] == 0 && (trimmed.equals("|") || trimmed.equals("║"))) break;
                    if (dy[dir] == 0 && (trimmed.equals("-") || trimmed.equals("="))) break;

                    nx += dx[dir];
                    ny += dy[dir];
                }
            }

            // Isla insatisfecha sin posibilidad de conexiones futuras => derrota
            if (espacioRestante > 0 && !tienePosibleConexion) return true;
        }

        // Si hay más de un componente y no existe ninguna conexión posible entre ellos => derrota
        if (compId > 1 && !posibleUnion) return true;

        return false;
    }
}
