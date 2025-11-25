import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuncionesJuego {

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
        String cel = mapa.get(x).get(y);
        if (cel == null) return 0;
        cel = cel.trim();

        int total = 0;
        if (cel.contains("=")) total += 2;
        else if (cel.contains("-")) total += 1;
        if (cel.contains("║")) total += 2;
        else if (cel.contains("|")) total += 1;

        if (!cel.contains("-") && !cel.contains("=")) {
            if (enRango(x,y-1,mapa)){
                String celda = mapa.get(x).get(y-1).trim();
                if (celda.contains("=")) total += 2;
                else if (celda.contains("-")) total += 1;
            }
            if (enRango(x,y+1,mapa)){
                String celda = mapa.get(x).get(y+1).trim();
                if (celda.contains("=")) total += 2;
                else if (celda.contains("-")) total += 1;
            }
        }
        if (!cel.contains("|") && !cel.contains("║")) {
            if (enRango(x-1,y,mapa)){
                String celda = mapa.get(x-1).get(y).trim();
                if (celda.contains("║")) total += 2;
                else if (celda.contains("|")) total += 1;
            }
            if (enRango(x+1,y,mapa)){
                String celda = mapa.get(x+1).get(y).trim();
                if (celda.contains("║")) total += 2;
                else if (celda.contains("|")) total += 1;
            }
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
                if (cel.equals("|") || cel.equals("║")) return false;
            }
        } else if (y1 == y2) { // vertical
            for (int i = Math.min(x1,x2)+1; i < Math.max(x1,x2); i++) {
                String cel = mapa.get(i).get(y1).trim();
                if (esIsla(cel)) return false;
                if (cel.equals("-") || cel.equals("=")) return false;
            }
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

        // adyacentes
        if ((horizontal && Math.abs(y1-y2)==1) || (vertical && Math.abs(x1-x2)==1)) {
            int total = existentes + cantidad;
            String simbolo = horizontal ? (total==1?"-":"=") : (total==1?"|":"║");
            mapa.get(x1).set(y1, editarSimbolo(origen, simbolo));
            mapa.get(x2).set(y2, editarSimbolo(destino, simbolo));
            System.out.println("Puente agregado entre islas total entre islas: " + total);
            return true;
        }

        // no adyacentes
        int total = existentes + cantidad;
        if (horizontal) {
            int fila = x1;
            String fill = (total==1)?"-":"=";
            for (int j=Math.min(y1,y2)+1; j<Math.max(y1,y2); j++) {
                String cel = mapa.get(fila).get(j).trim();
                if (esIsla(cel)) { System.out.println(" Hay una isla en el camino."); return false; }
                if (cel.contains("|") || cel.contains("║")) { System.out.println(" Cruce detectado."); return false; }
                mapa.get(fila).set(j, fill);
            }
        } else {
            int col = y1;
            String fill = (total==1)?"|":"║";
            for (int i=Math.min(x1,x2)+1; i<Math.max(x1,x2); i++) {
                String cel = mapa.get(i).get(col).trim();
                if (esIsla(cel)) { System.out.println(" Hay una isla en el camino."); return false; }
                if (cel.contains("-") || cel.contains("=")) { System.out.println(" Cruce detectado."); return false; }
                mapa.get(i).set(col, fill);
            }
        }
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
