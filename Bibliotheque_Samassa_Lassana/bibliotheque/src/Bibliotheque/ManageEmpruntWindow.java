package Bibliotheque;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ManageEmpruntWindow extends JFrame {
    private JComboBox<String> adherentsCombo;
    private JComboBox<String> livresCombo;
    private JLabel livreDetailsLabel;
    private JButton emprunterButton;
    private Component libraryManagementPage;

    public ManageEmpruntWindow(Component libraryManagementPage) {
        super("Emprunter un Livre");
        this.libraryManagementPage = libraryManagementPage;

        JPanel panel = new JPanel();
        JButton backButton = new JButton("Retour à la Bibliothèque");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                goBackToLibraryManagementPage();
            }
        });
        panel.add(backButton);
        add(panel);

        setLocationRelativeTo(libraryManagementPage);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    private void goBackToLibraryManagementPage() {
        libraryManagementPage.setVisible(true);
        dispose();
    }

    public ManageEmpruntWindow() {
        super("Emprunter un livre");

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Sélectionner un adhérent:"));
        adherentsCombo = new JComboBox<>();
        loadAdherentsCombo();
        panel.add(adherentsCombo);

        panel.add(new JLabel("Sélectionner un livre:"));
        livresCombo = new JComboBox<>();
        loadLivresCombo();
        livresCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateLivreDetails();
            }
        });
        panel.add(livresCombo);

        panel.add(new JLabel("Détails du livre:"));
        livreDetailsLabel = new JLabel();
        panel.add(livreDetailsLabel);

        emprunterButton = new JButton("Emprunter");
        emprunterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                emprunterLivre();
            }
        });
        panel.add(emprunterButton);

        add(panel);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void loadAdherentsCombo() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CONCAT(nom, ' ', prenom) AS nom_complet FROM adherent");

            while (rs.next()) {
                adherentsCombo.addItem(rs.getString("nom_complet"));
            }

            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadLivresCombo() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_livre, titre FROM livre WHERE disponibilite > 0");

            while (rs.next()) {
                livresCombo.addItem(rs.getInt("id_livre") + ": " + rs.getString("titre"));
            }

            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateLivreDetails() {
        String selectedLivre = (String) livresCombo.getSelectedItem();
        if (selectedLivre != null) {
            String[] parts = selectedLivre.split(":");
            int idLivre = Integer.parseInt(parts[0].trim());

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");
                PreparedStatement stmt = con.prepareStatement("SELECT titre, prix, id_auteur FROM livre WHERE id_livre = ?");
                stmt.setInt(1, idLivre);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String titre = rs.getString("titre");
                    String prix = rs.getString("prix");
                    int id_auteur = rs.getInt("id_auteur");

                    String auteur = getAuteurName(id_auteur);

                    livreDetailsLabel.setText("Titre: " + titre + ", Prix: " + prix + ", Auteur: " + auteur);
                }

                con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String getAuteurName(int id_auteur) {
        String auteur = "";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");
            PreparedStatement stmt = con.prepareStatement("SELECT nom, prenom FROM auteur WHERE id_auteur = ?");
            stmt.setInt(1, id_auteur);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                auteur = nom + " " + prenom;
            }

            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return auteur;
    }

    private void emprunterLivre() {
        String selectedAdherent = (String) adherentsCombo.getSelectedItem();
        String selectedLivre = (String) livresCombo.getSelectedItem();
    
        if (selectedAdherent == null || selectedLivre == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un adhérent et un livre.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        String[] parts = selectedLivre.split(":");
        int idLivre = Integer.parseInt(parts[0].trim());
    
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");
    
            // Vérification de la disponibilité du livre
            PreparedStatement checkStmt = con.prepareStatement("SELECT disponibilite FROM livre WHERE id_livre = ?");
            checkStmt.setInt(1, idLivre);
            ResultSet rs = checkStmt.executeQuery();
    
            if (rs.next()) {
                int disponibilite = rs.getInt("disponibilite");
                if (disponibilite > 0) {
                    // Mise à jour de la disponibilité
                    PreparedStatement updateStmt = con.prepareStatement("UPDATE livre SET disponibilite = ? WHERE id_livre = ?");
                    updateStmt.setInt(1, disponibilite - 1);
                    updateStmt.setInt(2, idLivre);
                    updateStmt.executeUpdate();
    
                    // Calcul de la date de retour (4 semaines plus tard)
                    LocalDate dateEmprunt = LocalDate.now();
                    LocalDate dateRetour = dateEmprunt.plusWeeks(4);
    
                    // Insertion de l'emprunt dans la base de données
                    PreparedStatement insertStmt = con.prepareStatement("INSERT INTO emprunts (id_adherent, id_livre, date_emprunt, date_retour) VALUES (?, ?, ?, ?)");
                    insertStmt.setInt(1, getAdherentId(selectedAdherent));
                    insertStmt.setInt(2, idLivre);
                    insertStmt.setDate(3, Date.valueOf(dateEmprunt));
                    insertStmt.setDate(4, Date.valueOf(dateRetour));
                    insertStmt.executeUpdate();
    
                    JOptionPane.showMessageDialog(this, "Livre emprunté avec succès. Date de retour : " + dateRetour.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                } else {
                    JOptionPane.showMessageDialog(this, "Le livre sélectionné n'est pas disponible.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
    
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'emprunt du livre : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    

    private int getAdherentId(String nomComplet) {
        int adherentId = -1;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");
            PreparedStatement stmt = con.prepareStatement("SELECT id_adherent FROM adherent WHERE CONCAT(nom, ' ', prenom) = ?");
            stmt.setString(1, nomComplet);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                adherentId = rs.getInt("id_adherent");
            }
    
            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return adherentId;
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ManageEmpruntWindow();
            }
        });
    }
}
