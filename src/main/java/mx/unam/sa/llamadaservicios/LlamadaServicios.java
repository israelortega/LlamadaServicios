/*
 * (c) UNAM, 2023
 */
package mx.unam.sa.llamadaservicios;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Se reccore un directorio para procesar los documentos en serviciosweb
 * @author israel1971
 */
public class LlamadaServicios {

    double media = 0;
    double desviacionSt = 0;

    public static void main(String[] args) {
        String path = "c:/Temp/oficios"; // Reemplaza esto con la ruta de tu directorio

        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            List<File> files = paths.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());

            List<Long> valoresEnMilisegundosJava = new ArrayList<>();
            List<Long> valoresEnMilisegundosPython = new ArrayList<>();

            String urlJava = "http://localhost:7050/process";
            String urlPython = "http://localhost:5000/pdf";

            for (File file : files) {

//                //Lamada servicio Java
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", new FileSystemResource(file));
                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
                RestTemplate restTemplate = new RestTemplate();
                long tInicio = System.currentTimeMillis();
                ResponseEntity<String> response = restTemplate
                        .postForEntity(urlJava, requestEntity, String.class);
                long tFinal = System.currentTimeMillis();
                long timeElapsed = tFinal - tInicio;
                valoresEnMilisegundosJava.add(timeElapsed);
//                System.out.println(response.getBody());

                //Llamada Python
                HttpHeaders headers2 = new HttpHeaders();
                headers2.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> body2 = new LinkedMultiValueMap<>();
                body2.add("archivo_pdf", new FileSystemResource(file));
                HttpEntity<MultiValueMap<String, Object>> requestEntity2 = new HttpEntity<>(body2, headers2);

                RestTemplate restTemplate2 = new RestTemplate();
                //LLamada servicio Python
                long tInicio2 = System.currentTimeMillis();
                ResponseEntity<String> response2 = restTemplate2
                        .postForEntity(urlPython, requestEntity2, String.class);
                long tFinal2 = System.currentTimeMillis();
                long timeElapsed2 = tFinal2 - tInicio2;
                valoresEnMilisegundosPython.add(timeElapsed2);
//                System.out.println(response.getBody());
            }

            LlamadaServicios llamadaServicios = new LlamadaServicios();
            System.out.println("Valores Java: " + valoresEnMilisegundosJava);
            llamadaServicios.calculaMediaySD(valoresEnMilisegundosJava);
            System.out.println("Media Java: " + llamadaServicios.media);
            System.out.println("Desviación Estándar Java:" + llamadaServicios.desviacionSt);

            System.out.println("Valores Python: " + valoresEnMilisegundosPython);
            llamadaServicios.calculaMediaySD(valoresEnMilisegundosPython);
            System.out.println("Media Java: " + llamadaServicios.media);
            System.out.println("Desviación Estándar Java:" + llamadaServicios.desviacionSt);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Fallo al realizar la operación");
        }
    }

    public void calculaMediaySD(List<Long> arreglo) {
        long suma = 0;
        long valorCuadrado = 0;
        for (long valor : arreglo) {
            suma += valor;
            valorCuadrado += valor * valor;
        }
        int size = arreglo.size();
        media = (double) suma / size;
        desviacionSt = Math.sqrt((double) (valorCuadrado - suma * suma / size) / size);
    }
}
