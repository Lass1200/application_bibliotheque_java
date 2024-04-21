package Bibliotheque;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:8889/bibliothequesio";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static void afficherNomAuteur(int idAuteur) {
        String sql = "SELECT nom FROM auteur WHERE id_auteur = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idAuteur);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String nomAuteur = rs.getString("nom");
                System.out.println("Nom de l'auteur : " + nomAuteur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Établir une connexion à la base de données
            Connection connection = DatabaseManager.getConnection();
            if (connection != null) {
                System.out.println("Connexion à la base de données établie avec succès !");
                
                // Afficher le nom d'un auteur spécifique (ID = 1 dans cet exemple)
                afficherNomAuteur(1);
            } else {
                System.out.println("Impossible de se connecter à la base de données.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
        }
    }
}
