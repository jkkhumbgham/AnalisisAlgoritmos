import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AsignarAristasDFSTest {

    public static void main(String[] args) {
        int total = 0;
        int exitos = 0;

        total++; if (pruebaPuenteSimpleHorizontal()) exitos++;
        total++; if (pruebaPuenteDobleHorizontal()) exitos++;
        total++; if (pruebaCasoSinSolucionPorCapacidad()) exitos++;
        total++; if (pruebaCruceImposible()) exitos++;

        System.out.println();
        System.out.println("Pruebas completadas: " + exitos + "/" + total);
        if (exitos == total) {
            System.out.println("pasó");
        } else {
            System.out.println("falló");
        }
    }


    private static List<String> fila(String... celdas) {
        return new ArrayList<>(Arrays.asList(celdas));
    }

    @SafeVarargs
    private static List<List<String>> mapa(List<String>... filas) {
        return new ArrayList<>(Arrays.asList(filas));
    }

    private static List<List<String>> copia(List<List<String>> original) {
        List<List<String>> clone = new ArrayList<>();
        for (List<String> fila : original) {
            clone.add(new ArrayList<>(fila));
        }
        return clone;
    }

    private static boolean check(boolean condition, String mensaje) {
        if (!condition) throw new AssertionError(mensaje);
        return true;
    }

    private static boolean ejecutar(String nombre, Runnable prueba) {
        System.out.print(nombre + " ... ");
        try {
            prueba.run();
            System.out.println("OK");
            return true;
        } catch (AssertionError e) {
            System.out.println("FALLO -> " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("ERROR -> " + e.getMessage());
            e.printStackTrace(System.out);
            return false;
        }
    }

    private static boolean pruebaPuenteSimpleHorizontal() {
        return ejecutar("Puente simple horizontal", () -> {
            List<List<String>> mapa = mapa(
                    fila("1", " ", "1")
            );

            boolean exito = FuncionesJuego.resolverAutomaticamente(mapa);
            check(exito, "El solver debería encontrar solución");
            check("-".equals(mapa.get(0).get(1).trim()), "Se esperaba un puente '-' en el centro");
        });
    }

    private static boolean pruebaPuenteDobleHorizontal() {
        return ejecutar("Puente doble horizontal", () -> {
            List<List<String>> mapa = mapa(
                    fila("2", " ", "2")
            );

            boolean exito = FuncionesJuego.resolverAutomaticamente(mapa);
            check(exito, "El solver debería construir el puente doble");
            check("=".equals(mapa.get(0).get(1).trim()), "Se esperaba un puente '=' en el centro");
            check(FuncionesJuego.verificarVictoria(mapa), "El tablero debe quedar en estado de victoria");
        });
    }

    private static boolean pruebaCasoSinSolucionPorCapacidad() {
        return ejecutar("Tablero sin solución por capacidad insuficiente", () -> {
            List<List<String>> mapa = mapa(
                    fila("1", " ", "1", " ", "1")
            );
            List<List<String>> original = copia(mapa);

            boolean exito = FuncionesJuego.resolverAutomaticamente(mapa);
            check(!exito, "El solver no debería encontrar solución");
            check(mapa.equals(original), "El mapa no debe modificarse tras fallar");
        });
    }

    private static boolean pruebaCruceImposible() {
        return ejecutar("Cruce imposible (sin solución)", () -> {
            List<List<String>> mapa = mapa(
                    fila(" ", "1", " "),
                    fila("1", " ", "1"),
                    fila(" ", "1", " ")
            );
            List<List<String>> original = copia(mapa);

            boolean exito = FuncionesJuego.resolverAutomaticamente(mapa);
            check(!exito, "El solver debería detectar que no hay solución sin cruces");
            check(mapa.equals(original), "El mapa debe permanecer igual al no haber solución");
        });
    }
}
