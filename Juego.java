import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Juego {
    private List<List<String>> mapa;
    private VistaJuego vista;

    public Juego(String archivo) throws IOException {
        this.mapa = ManejadorArchivos.Lectura(archivo);
        this.vista = new VistaJuego(mapa);

    }

    public void iniciar() {
        Scanner sc = new Scanner(System.in);
        vista.mostrar();

        while (true) {
            System.out.print(" Ingrese puente (x1 y1 x2 y2 cantidad) o -1 para salir: ");
            int x1 = sc.nextInt();
            if (x1 == -1) break;
            int y1 = sc.nextInt();
            int x2 = sc.nextInt();
            int y2 = sc.nextInt();
            int c = sc.nextInt();


            if (FuncionesJuego.agregarPuente(x1, y1, x2, y2, c, mapa)) {
                vista.mostrar();
            } else {
                vista.mostrarMensaje(" Movimiento inválido");
            }
        }

        sc.close();
        System.out.println(" Juego terminado.");
    }

    public static void main(String[] args) {
        try {
            Juego j = new Juego("Tablero.txt");
            j.iniciar();
        } catch (IOException e) {
            System.out.println(" Error al leer el archivo: " + e.getMessage());
        }
    }
}
