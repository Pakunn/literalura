package com.alura.name.Alura.principal;

import com.alura.name.Alura.model.Autor;
import com.alura.name.Alura.model.DatosLibro;
import com.alura.name.Alura.model.DatosResultados;
import com.alura.name.Alura.model.Libro;
import com.alura.name.Alura.repository.AutorRepository;
import com.alura.name.Alura.repository.LibroRepository;
import com.alura.name.Alura.service.ConsumoAPI;
import com.alura.name.Alura.service.ConvierteDatos;

import java.util.Optional;
import java.util.Scanner;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    
    private LibroRepository libroRepositorio;
    private AutorRepository autorRepositorio;

    public Principal(LibroRepository libroRepositorio, AutorRepository autorRepositorio) {
        this.libroRepositorio = libroRepositorio;
        this.autorRepositorio = autorRepositorio;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    
                    === LITERALURA ===
                    Elija la opción a través de su número:
                    1 - Buscar libro por título
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en un determinado año
                    5 - Listar libros por idioma
                    0 - Salir
                    """;
            
            System.out.println(menu);
            if (teclado.hasNextInt()) {
                opcion = teclado.nextInt();
                teclado.nextLine();

                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        listarLibrosRegistrados();
                        break;
                    case 3:
                        listarAutoresRegistrados();
                        break;
                    case 4:
                        listarAutoresVivosEnUnAnio();
                        break;
                    case 5:
                        listarLibrosPorIdioma();
                        break;
                    case 0:
                        System.out.println("Cerrando la aplicación...");
                        break;
                    default:
                        System.out.println("Opción inválida. Intente de nuevo.");
                }
            } else {
                System.out.println("Por favor, ingrese un número válido.");
                teclado.nextLine();
            }
        }
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Ingrese el nombre del libro que desea buscar:");
        var tituloLibro = teclado.nextLine();
        
        var json = consumoAPI.obtenerDatos(URL_BASE + tituloLibro.replace(" ", "%20"));
        var datosBusqueda = conversor.obtenerDatos(json, DatosResultados.class);

        if (datosBusqueda.resultados().isEmpty()) {
            System.out.println("El libro no fue encontrado.");
        } else {
            DatosLibro datosLibro = datosBusqueda.resultados().get(0);
            
            // verifica si exisxte libro
            Optional<Libro> libroExistente = libroRepositorio.findByTituloIgnoreCase(datosLibro.titulo());
            
            if (libroExistente.isPresent()) {
                System.out.println("\nNo se puede insertar el mismo libro más de una vez.");
                System.out.println("El libro '" + datosLibro.titulo() + "' ya está en la base de datos.");
            } else {
                // autor
                Autor autor;
                if (!datosLibro.autor().isEmpty()) {
                    var datosAutor = datosLibro.autor().get(0);
                    // busca al autor y si no existe se crea
                    autor = autorRepositorio.findByNombre(datosAutor.nombre())
                            .orElseGet(() -> {
                                Autor nuevoAutor = new Autor(datosAutor);
                                return autorRepositorio.save(nuevoAutor);
                            });
                } else {
                    // libro sin autor o dato vacio
                    autor = autorRepositorio.findByNombre("Desconocido")
                            .orElseGet(() -> {
                                Autor autorDesconocido = new Autor();
                                autorDesconocido.setNombre("Desconocido");
                                return autorRepositorio.save(autorDesconocido);
                            });
                }
                
                // se crea el libro y se guarda en la base de datos
                Libro libro = new Libro(datosLibro, autor);
                libroRepositorio.save(libro);
                
                System.out.println("\n----- LIBRO REGISTRADO EXITOSAMENTE -----");
                System.out.println(libro);
                System.out.println("-----------------------------------------\n");
            }
        }
    }
    private void listarLibrosRegistrados() {
        System.out.println("\n--- LIBROS REGISTRADOS ---");
        var libros = libroRepositorio.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en la base de datos.");
        } else {
            libros.forEach(System.out::println);
        }
        System.out.println("--------------------------\n");
    }

    private void listarAutoresRegistrados() {
        System.out.println("\n--- AUTORES REGISTRADOS ---");
        var autores = autorRepositorio.findAll();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados en la base de datos.");
        } else {
            autores.forEach(a -> {
                System.out.println("Autor: " + a.getNombre());
                System.out.println("Fecha de nacimiento: " + (a.getFechaDeNacimiento() != null ? a.getFechaDeNacimiento() : "Desconocida"));
                System.out.println("Fecha de fallecimiento: " + (a.getFechaDeFallecimiento() != null ? a.getFechaDeFallecimiento() : "Desconocida"));
                
                
                var librosDelAutor = a.getLibros().stream()
                        .map(Libro::getTitulo)
                        .toList();
                System.out.println("Libros: " + librosDelAutor + "\n");
            });
        }
        System.out.println("---------------------------\n");
    }

    private void listarAutoresVivosEnUnAnio() {
        System.out.println("Ingrese el año que desea consultar:");
        if (teclado.hasNextInt()) {
            var anio = teclado.nextInt();
            teclado.nextLine(); // Limpiar el buffer

            var autoresVivos = autorRepositorio.autoresVivosEnUnAnio(anio);

            System.out.println("\n--- AUTORES VIVOS EN " + anio + " ---");
            if (autoresVivos.isEmpty()) {
                System.out.println("No se encontraron autores vivos en nuestra base de datos para ese año.");
            } else {
                autoresVivos.forEach(a -> {
                    System.out.println("Autor: " + a.getNombre());
                    System.out.println("Nacimiento: " + a.getFechaDeNacimiento() + " - Fallecimiento: " + (a.getFechaDeFallecimiento() != null ? a.getFechaDeFallecimiento() : "Desconocida"));
                    System.out.println();
                });
            }
            System.out.println("--------------------------------\n");

        } else {
            System.out.println("Año inválido. Por favor, ingrese un número entero.");
            teclado.nextLine();
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.println("""
                Ingrese el idioma para buscar los libros:
                es - Español
                en - Inglés
                fr - Francés
                pt - Portugués
                """);
        var idioma = teclado.nextLine();
        
        var librosPorIdioma = libroRepositorio.findByIdioma(idioma);
        
        System.out.println("\n--- LIBROS EN EL IDIOMA '" + idioma.toUpperCase() + "' ---");
        if (librosPorIdioma.isEmpty()) {
            System.out.println("No se encontraron libros registrados en ese idioma.");
        } else {
            librosPorIdioma.forEach(System.out::println);
        }
        System.out.println("----------------------------------\n");
    }
}