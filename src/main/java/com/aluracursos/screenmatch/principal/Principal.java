package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    String apiKey = System.getenv("OMDB_APIKEY");
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private  List <Serie> series;
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar serie por titulo
                    5 - Top 5 mejores series
                    6 - Buscar series por categoria
                    7 - Buscar series por temporada y evaluacion
                    8 - Buscar episodios por titulo
                    9 - Top 5 episodios por serie
                                  
                    0 - Salir
                    
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    buscarSeriesPorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodioPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }




    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + apiKey);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        //DatosSerie datosSerie = getDatosSerie();
        mostrarSeriesBuscadas();
        System.out.println("De que serie quieres buscar los episodios?");
        var nombreSerie= teclado.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if(serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + apiKey);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }


    }

    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
//        datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
        series = repositorio.findAll(); //para traer todo lo de la lista


        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSeriesPorTitulo() {
        System.out.println("Que serie buscas?");
        var nombreSerie= teclado.nextLine();
        serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);
        if (serieBuscada.isPresent()){
            System.out.println("Encontre esta serie"+serieBuscada);
        }else {
            System.out.println("Serie no encontrada");
        }
    }

    private void buscarTop5Series() {
        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s ->
                System.out.println("Serie: "+s.getTitulo() + " Evaluacion: "+s.getEvaluacion()));

    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Escribe el genero/categoria de la serie: ");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspanol(genero);
        List <Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las series de la categoria "+genero);
        seriesPorCategoria.forEach(System.out::println);

    }

    private void buscarSeriesPorTemporadaYEvaluacion() {
        System.out.println("cuantas temporadas maximo? ");
        var temporadas = teclado.nextInt();
        System.out.println("que evaluacion minima?");
        var evaluacion = teclado.nextDouble();
        List <Serie> seriesPorTemporadaYEvaluacion = repositorio.seriesPorTemporadaYEvaluacion(temporadas, evaluacion);
        seriesPorTemporadaYEvaluacion.forEach(s ->
                System.out.println(s.getTitulo() + " -evaluacion: "+ s.getEvaluacion()));


    }

    private void buscarEpisodioPorTitulo() {
        System.out.println("Escribe el nombre del episodio que deseas buscar: ");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie: %s Temporada %s Episodio %s Evaluacion %s",
                        e.getSerie().getTitulo(),e.getTemporada(), e.getNumeroEpisodio(),e.getEvaluacion()));
    }

    private void buscarTop5Episodios() {
        buscarSeriesPorTitulo();
        if (serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.top5Episodios(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Serie: %s - Temporada %s - Episodio %s - Evaluación %s\n",
                            e.getSerie().getTitulo(),e.getTemporada(), e.getNumeroEpisodio(),e.getEvaluacion()));
        }

    }


}

