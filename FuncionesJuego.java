import java.util.List;

public class FuncionesJuego {

    //La funcion revisa si los datos dados por el usuario estan dentro del mapa esperado
    private static boolean enRango(int x, int y, List<List<String>> mapa) {
        return x >= 0 && x < mapa.size() && y >= 0 && y < mapa.get(0).size();
    }

    //funcion revisa si la celda dada es una isla o no
    private static boolean esIsla(String celda) {
        return celda != null && celda.matches("[1-8](\\s[-=|║]*)?");
    }



    private static int contarPuentesIsla(List<List<String>> mapa, int x, int y) {

        String cel = mapa.get(x).get(y);
        if (cel == null) return 0;
        cel = cel.trim();

        int total = 0;

        // horizontales pegados
        if (cel.contains("=")) total += 2;
        else if (cel.contains("-")) total += 1;
        // verticales pegados
        if (cel.contains("║")) total += 2;
        else if (cel.contains("|")) total += 1;

        // izquierda
        if (!cel.contains("-") && !cel.contains("=")) {
            if (enRango(x,y-1,mapa)){
                String celda = mapa.get(x).get(y-1);

                celda = celda.trim();
                // si la primera celda no-vacía contiene puente horizontal -> cuenta y para
                if (celda.contains("=")) {
                    total += 2;
                }
                if (celda.contains("-")) {
                    total += 1;
                }
            }
        }
        // derecha
        if (!cel.contains("-") && !cel.contains("=")) {
            if (enRango(x,y+1,mapa)){
                String celda = mapa.get(x).get(y+1);
                celda = celda.trim();
                if (celda.contains("=")) {
                    total += 2;
                }
                if (celda.contains("-")) {
                    total += 1;
                }
            }
        }
        // arriba
        if (!cel.contains("|") && !cel.contains("║")) {
            if (enRango(x-1,y,mapa)){
                String celda = mapa.get(x-1).get(y);
                celda = celda.trim();
                if (celda.contains("║")) {
                    total += 2;
                }
                if (celda.contains("|")) {
                    total += 1;
                }
            }
        }
        // abajo
        if (!cel.contains("|") && !cel.contains("║")) {
            if (enRango(x + 1, y, mapa)) {
                String celda = mapa.get(x + 1).get(y);

                celda = celda.trim();
                if (celda.contains("║")) {
                    total += 2;
                }
                if (celda.contains("|")) {
                    total += 1;
                }

            }
        }
            return total;
    }


    //si la isla completo puentes
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
        if (!enRango(x1, y1, mapa) || !enRango(x2, y2, mapa)) {
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


            int actuales = Math.max(contarPuentesIsla(mapa,x1,y1), contarPuentesIsla(mapa,x2,y2));
            int nuevas = actuales + cantidad;

            if (nuevas > 2) {
                System.out.println(" Demasiados puentes entre estas islas (máx 2).");
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


            System.out.println(" Puente agregado entre islas adyacentes. Total: " + nuevas);
            return true;
        }





        int inicio, fin;
        int existentes = 0;
        boolean unico = false, doble = false;

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
                if (cel.equals("=")) doble = true;
                if (cel.equals("-")) unico = true;
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
                if (cel.equals("║")) doble = true;
                if (cel.equals("|")) unico = true;
            }
        }

        if (doble) existentes = 2;
        else if (unico) existentes = 1;
        else existentes = 0;

        if (existentes + cantidad > 2) {
            System.out.println(" Demasiados puentes entre estas islas (max 2).");
            return false;
        }

        int target = existentes + cantidad;
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
