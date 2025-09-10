import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.lang.System.exit;

public class ManejadorArchivos {

    public static List<List<String>> Lectura(String nombre) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(nombre));
        String primera = br.readLine();
        List<List<String>> lectura = new ArrayList<List<String>>();
        String[] partes = primera.split(",");
        int filas = Integer.parseInt(partes[0]);
        int columnas = Integer.parseInt(partes[1]);

        lectura = new ArrayList<>(); // asegúrate de reiniciar la lista

        for (int i = 0; i < filas; i++) {
            String linea = br.readLine();
            List<String> fila = new ArrayList<>();
            for (int j = 0; j < columnas; j++) {
                if (linea.charAt(j)=='0'){
                    fila.add("  ");
                }else {
                    fila.add(String.valueOf(linea.charAt(j))+" ");
                }

            }
            lectura.add(fila); // añadimos la fila completa
        }
        br.close();
        return lectura;
    }



}
