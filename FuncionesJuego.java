import java.util.List;

public class FuncionesJuego {

    private static boolean inBounds(int x, int y, List<List<String>> mapa) {
        return x >= 0 && x < mapa.size() && y >= 0 && y < mapa.get(0).size();
    }

    private static boolean esIsla(String celda) {
        return celda != null && celda.matches("[1-8](\\s[-=|#║]*)?");
    }

    private static int conexionesActuales(String celda) {
        if (celda == null) return 0;
        if (celda.contains("=") || celda.contains("║")) return 2; // doble puente
        if (celda.contains("-") || celda.contains("|")) return 1; // simple puente
        return 0;
    }
    private static int contarPuentesIsla(List<List<String>> mapa, int x, int y) {
        int filas = mapa.size();
        int cols = mapa.get(0).size();

        String cel = mapa.get(x).get(y);
        if (cel == null) return 0;
        cel = cel.trim();

        int total = 0;

        // --- 1) Contar símbolos pegados en la propia celda (maneja islas adyacentes) ---
        // horizontales pegados
        if (cel.contains("=")) total += 2;
        else if (cel.contains("-")) total += 1;
        // verticales pegados
        if (cel.contains("║")) total += 2;
        else if (cel.contains("|")) total += 1;

        // --- 2) Si la propia celda no tiene símbolo horizontal, buscar hacia la izquierda/derecha ---
        // izquierda
        if (!cel.contains("-") && !cel.contains("=")) {
            for (int j = y - 1; j >= 0; j--) {
                String s = mapa.get(x).get(j);
                if (s == null) break;
                s = s.trim();
                if (s.isEmpty() || s.equals(".") || s.equals("0")) continue; // sigue el intervalo vacío
                // si la primera celda no-vacía contiene puente horizontal -> cuenta y para
                if (s.contains("=")) { total += 2; break; }
                if (s.contains("-"))  { total += 1; break; }
                // si la primera celda no-vacía es una isla sin símbolo horizontal -> no hay conexión en esta dirección
                break;
            }
        }
        // derecha
        if (!cel.contains("-") && !cel.contains("=")) {
            for (int j = y + 1; j < cols; j++) {
                String s = mapa.get(x).get(j);
                if (s == null) break;
                s = s.trim();
                if (s.isEmpty() || s.equals(".") || s.equals("0")) continue;
                if (s.contains("=")) { total += 2; break; }
                if (s.contains("-"))  { total += 1; break; }
                break;
            }
        }

        // --- 3) Si la propia celda no tiene símbolo vertical, buscar arriba/abajo ---
        // arriba
        if (!cel.contains("|") && !cel.contains("║")) {
            for (int i = x - 1; i >= 0; i--) {
                String s = mapa.get(i).get(y);
                if (s == null) break;
                s = s.trim();
                if (s.isEmpty() || s.equals(".") || s.equals("0")) continue;
                if (s.contains("║")) { total += 2; break; }
                if (s.contains("|"))  { total += 1; break; }
                break;
            }
        }
        // abajo
        if (!cel.contains("|") && !cel.contains("║")) {
            for (int i = x + 1; i < filas; i++) {
                String s = mapa.get(i).get(y);
                if (s == null) break;
                s = s.trim();
                if (s.isEmpty() || s.equals(".") || s.equals("0")) continue;
                if (s.contains("║")) { total += 2; break; }
                if (s.contains("|"))  { total += 1; break; }
                break;
            }
        }

        return total;
    }


    /**
     * Devuelve true si la isla en (x,y) ya alcanzó su capacidad.
     */
    private static boolean islaCompleta(List<List<String>> mapa, int x, int y) {
        String celda = mapa.get(x).get(y);
        if (celda == null || celda.isEmpty()) return false;

        int capacidad = -1;
        for (char c : celda.toCharArray()) {
            if (Character.isDigit(c)) {
                capacidad = Character.getNumericValue(c);
                break;
            }
        }
        if (capacidad == -1) return false;

        int usados = contarPuentesIsla(mapa, x, y);
        return usados >= capacidad;
    }



    public static boolean agregarPuente(int x1, int y1, int x2, int y2, int cantidad, List<List<String>> mapa) {
        // validaciones básicas de rango
        if (!inBounds(x1, y1, mapa) || !inBounds(x2, y2, mapa)) {
            System.out.println(" Coordenadas fuera de rango.");
            return false;
        }
        if (islaCompleta(mapa, x1, y1) || islaCompleta(mapa, x2, y2)) {
            System.out.println(" Una de las islas ya alcanzó su número máximo de puentes.");
            return false;
        }

        // los extremos deben ser islas
        String origen = mapa.get(x1).get(y1);
        String destino = mapa.get(x2).get(y2);
        if (!esIsla(origen) || !esIsla(destino)) {
            System.out.println(" Ambos extremos deben ser islas (1-8).");
            return false;
        }

        // orientación (horizontal o vertical)
        boolean horizontal = (x1 == x2);
        boolean vertical = (y1 == y2);
        if (!horizontal && !vertical) {
            System.out.println(" Solo se permiten puentes horizontales o verticales.");
            return false;
        }


        // Caso especial: islas adyacentes
        if ((horizontal && Math.abs(y1 - y2) == 1) || (vertical && Math.abs(x1 - x2) == 1)) {

            String isla1 = mapa.get(x1).get(y1);
            String isla2 = mapa.get(x2).get(y2);

            int actuales = Math.max(conexionesActuales(isla1), conexionesActuales(isla2));
            int nuevas = actuales + cantidad;

            if (nuevas > 2) {
                System.out.println("❌ Demasiados puentes entre estas islas (máx 2).");
                return false;
            }

            String simbolo;
            if (horizontal) {
                simbolo = (nuevas == 1) ? "-" : "=";
            } else { // vertical
                simbolo = (nuevas == 1) ? "|" : "║";
            }

            // Sobrescribimos el símbolo en ambas islas
            mapa.get(x1).set(y1, isla1.replaceAll("[-=|║]*$", "") + simbolo);


            System.out.println("✅ Puente agregado entre islas adyacentes. Total: " + nuevas);
            return true;
        }




        // recorrer celdas intermedias y comprobar:
        int inicio, fin;
        int existing = 0; // 0,1 o 2 puentes existentes entre estas islas
        boolean foundSingle = false, foundDouble = false;

        if (horizontal) {
            int fila = x1;
            inicio = Math.min(y1, y2) + 1;
            fin = Math.max(y1, y2) - 1;
            for (int j = inicio; j <= fin; j++) {
                String cel = mapa.get(fila).get(j);
                if (esIsla(cel)) {
                    System.out.println(" Hay una isla en el camino.");
                    return false;
                }
                if (cel.equals("|") || cel.equals("║")) {
                    System.out.println(" Cruce detectado (hay puente vertical en el camino).");
                    return false;
                }
                if (cel.equals("=")) foundDouble = true;
                if (cel.equals("-")) foundSingle = true;
            }
        } else { // vertical
            int col = y1;
            inicio = Math.min(x1, x2) + 1;
            fin = Math.max(x1, x2) - 1;
            for (int i = inicio; i <= fin; i++) {
                String cel = mapa.get(i).get(col);
                if (esIsla(cel)) {
                    System.out.println(" Hay una isla en el camino.");
                    return false;
                }
                if (cel.equals("-") || cel.equals("=")) {
                    System.out.println(" Cruce detectado (hay puente horizontal en el camino).");
                    return false;
                }
                if (cel.equals("║")) foundDouble = true;
                if (cel.equals("|")) foundSingle = true;
            }
        }

        if (foundDouble) existing = 2;
        else if (foundSingle) existing = 1;
        else existing = 0;

        if (existing + cantidad > 2) {
            System.out.println(" Demasiados puentes entre estas islas (max 2).");
            return false;
        }

        int target = existing + cantidad; // resultado final entre esas islas
        if (horizontal) {
            int fila = x1;
            for (int j = Math.min(y1, y2) + 1; j < Math.max(y1, y2); j++) {
                if (target == 1) mapa.get(fila).set(j, "-");
                else mapa.get(fila).set(j, "=");
            }
        } else {
            int col = y1;
            for (int i = Math.min(x1, x2) + 1; i < Math.max(x1, x2); i++) {
                if (target == 1) mapa.get(i).set(col, "|");
                else mapa.get(i).set(col, "║");
            }
        }

        System.out.println(" Puente agregado (total ahora entre estas islas: " + target + ").");
        return true;
    }
}
