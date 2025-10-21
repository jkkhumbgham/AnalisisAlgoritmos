import java.util.ArrayList;
import java.util.List;

public class VistaJuego {
    private List<List<String>> mapa = new ArrayList<>();
    public VistaJuego(List<List<String>> mapa){
        this.mapa=mapa;
    }


    public void mostrarMensaje(String msg) {
        System.out.println(msg);
    }

    private static final int tamCelda = 3;

    private String centrar(String s, int w) {
        if (s == null) s = "";
        int padding = Math.max(0, w - s.length());
        int izq = padding / 2;
        int der = padding - izq;
        return " ".repeat(izq) + s + " ".repeat(der);
    }

    public void mostrar() {
        int filas = mapa.size();
        int columnas = mapa.get(0).size();
        int columnasLogicas = columnas / 2;

        String izqMargen = " ".repeat(tamCelda + 1);
        System.out.print(izqMargen);
        for (int c = 0; c < columnasLogicas; c++) {
            System.out.print(centrar(String.valueOf(c), tamCelda * 2));
        }
        System.out.println();

        for (int i = 0; i < filas; i++) {
            if (i % 2 == 1) System.out.print(centrar(String.valueOf(i / 2), tamCelda) + " ");
            else            System.out.print(izqMargen);

            for (int j = 0; j < columnas; j++) {
                String celda = mapa.get(i).get(j);
                System.out.print(centrar(celda, tamCelda));
            }
            System.out.println();
        }

    }

}
