import java.util.List;
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
}