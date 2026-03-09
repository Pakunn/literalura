package com.alura.name.Alura;

import com.alura.name.Alura.principal.Principal;
import com.alura.name.Alura.repository.AutorRepository;
import com.alura.name.Alura.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

	@Autowired
    private LibroRepository libroRepository;
    
    @Autowired
    private AutorRepository autorRepository;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
        
		Principal principal = new Principal(libroRepository, autorRepository);
		principal.muestraElMenu();
	}

}
