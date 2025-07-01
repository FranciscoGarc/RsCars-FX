package com.rscars.taller.rscarsfx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.Properties;

public class ConexionDB {

    // 1. Instancia única de la clase (Singleton)
    private static ConexionDB instancia;

    // 2. La conexión a la base de datos
    private Connection cnx;

    // 3. Constructor privado para evitar que se creen instancias directamente
    private ConexionDB() {
        Properties props = new Properties();
        try (InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("config.properties")) {
            // Cargar el archivo de propiedades
            if (input == null) {
                System.out.println("Error: No se encontró el archivo config.properties");
                return;
            }
            props.load(input);

            // Obtener las propiedades
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            // Validar que las propiedades no sean nulas
            if (url == null || user == null || password == null) {
                System.out.println("Error: Faltan propiedades en config.properties");
                return;
            }

            // Cargar el driver de SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // Establecer la conexión
            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("¡Conexión a la base de datos exitosa!");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Driver no encontrado.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error al leer el archivo config.properties.");
            e.printStackTrace();
        }
    }

    // 4. Método público para obtener la instancia única
    public static synchronized ConexionDB obtenerInstancia() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    // 5. Método para obtener el objeto de conexión
    public Connection getCnx() {
        return cnx;
    }

    // 6. Método para cerrar la conexión
    public void cerrarConexion() {
        if (cnx != null) {
            try {
                cnx.close();
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                System.out.println("Error al cerrar la conexión.");
                e.printStackTrace();
            }
        }
    }
}