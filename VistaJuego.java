import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VistaJuego {
    private List<List<String>> mapa = new ArrayList<>();
    public VistaJuego(List<List<String>> mapa){
        this.mapa=mapa;
    }

    public void mostrar() {
        int filas = mapa.size();
        int columnas = mapa.get(0).size();



        // Cada fila
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                System.out.print(mapa.get(i).get(j) + " +");
            }
            System.out.println();
        }
    }
    public void mostrarMensaje(String msg) {
        System.out.println(msg);
    }
}
