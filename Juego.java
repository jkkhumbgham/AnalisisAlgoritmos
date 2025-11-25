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

        final int filasLogicas = mapa.size() / 2;
        final int columnasLogicas = mapa.get(0).size() / 2;

        while (true) {
            System.out.print(" Ingrese puente (x1 y1 x2 y2 cantidad), -2 para resolver automático o -1 para salir: ");
            int x1 = sc.nextInt();
            if (x1 == -1) break;
            if (x1 == -2) {
                boolean resuelto = FuncionesJuego.resolverAutomaticamente(mapa);
                boolean victoria = FuncionesJuego.verificarVictoria(mapa);
                vista.mostrar();
                if (resuelto || victoria) {
                    vista.mostrarMensaje(" Resuelto automáticamente.");
                    break;
                } else {
                    vista.mostrarMensaje(" No se encontró solución automática desde este estado. Victoria detectada: " + victoria);
                    vista.mostrarMensaje(" Pasos realizados por el solver:");
                    continue;
                }
            }
            int y1 = sc.nextInt();
            int x2 = sc.nextInt();
            int y2 = sc.nextInt();
            int c  = sc.nextInt();

            if (x1 < 0 || x1 >= filasLogicas || x2 < 0 || x2 >= filasLogicas
                    || y1 < 0 || y1 >= columnasLogicas || y2 < 0 || y2 >= columnasLogicas) {
                vista.mostrarMensaje(" Coordenadas fuera de rango lógico (use 0.." + (filasLogicas-1)
                        + ", 0.." + (columnasLogicas-1) + ").");
                continue;
            }
            int ix1 = toFila(x1), iy1 = toCol(y1);
            int ix2 = toFila(x2), iy2 = toCol(y2);

            if (FuncionesJuego.agregarPuente(ix1, iy1, ix2, iy2, c, mapa)) {
                vista.mostrar();
                if (FuncionesJuego.verificarVictoria(mapa)) {
                    vista.mostrarMensaje(" ¡Victoria! Todas las islas están conectadas correctamente.");
                    break;
                }
                if (FuncionesJuego.verificarDerrota(mapa)) {
                    vista.mostrarMensaje(" Derrota: el estado del tablero es inválido.");
                    break;
                }
            } else {
                vista.mostrarMensaje(" Movimiento inválido");
            }
        }

        sc.close();
        System.out.println(" Juego terminado.");
    }

    private int toFila(int xLogico) { return 2 * xLogico + 1; }
    private int toCol(int yLogico)  { return 2 * yLogico; }

    public static void main(String[] args) {
        try {
            Juego j = new Juego("Tablero.txt");
            j.iniciar();
        } catch (IOException e) {
            System.out.println(" Error al leer el archivo: " + e.getMessage());
        }
    }
}
