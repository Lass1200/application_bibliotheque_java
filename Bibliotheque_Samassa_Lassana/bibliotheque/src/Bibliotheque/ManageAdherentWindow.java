package Bibliotheque;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageAdherentWindow extends JFrame {
    private JComboBox<String> adherentsCombo;
    private JTextField nomField;
    private JTextField prenomField;
    private JTextField emailField;

    public ManageAdherentWindow() {
        super("Gérer les Adhérents");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Sélectionner ou ajouter un adhérent"), gbc);


        gbc.gridx = 1;
        gbc.gridy = 0;
        adherentsCombo = new JComboBox<>();
        loadAdherentsCombo();
        adherentsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fillFieldsWithSelectedAdherent();
            }
        });
        panel.add(adherentsCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Nom:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        nomField = new JTextField(20); // Largeur fixe pour le champ nom
        panel.add(nomField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Prénom:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        prenomField = new JTextField(20); // Largeur fixe pour le champ prénom
        panel.add(prenomField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        emailField = new JTextField(20); // Largeur fixe pour le champ email
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JButton ajouterButton = new JButton("Ajouter");
        ajouterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ajouterAdherent();
            }
        });
        panel.add(ajouterButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        JButton modifierButton = new JButton("Modifier");
        modifierButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modifierAdherent();
            }
        });
        panel.add(modifierButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 4;
        JButton supprimerButton = new JButton("Supprimer");
        supprimerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                supprimerAdherent();
            }
        });
        panel.add(supprimerButton, gbc);

        add(panel);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    private void loadAdherentsCombo() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id_adherent, nom, prenom FROM adherent");

            while (rs.next()) {
                adherentsCombo.addItem(rs.getInt("id_adherent") + ": " + rs.getString("nom") + " " + rs.getString("prenom"));
            }

            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void fillFieldsWithSelectedAdherent() {
        String selectedAdherent = (String) adherentsCombo.getSelectedItem();
        if (selectedAdherent != null) {
            String[] parts = selectedAdherent.split(":");
            int idAdherent = Integer.parseInt(parts[0].trim());

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");
                PreparedStatement stmt = con.prepareStatement("SELECT nom, prenom, email FROM adherent WHERE id_adherent = ?");
                stmt.setInt(1, idAdherent);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    nomField.setText(rs.getString("nom"));
                    prenomField.setText(rs.getString("prenom"));
                    emailField.setText(rs.getString("email"));
                }

                con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean isAdherentAvailable(String nom, String prenom) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");
            PreparedStatement stmt = con.prepareStatement("SELECT COUNT(*) AS count FROM adherent WHERE nom = ? AND prenom = ?");
            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                return count == 0; // Si count == 0, l'adhérent n'existe pas
            }

            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void ajouterAdherent() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isAdherentAvailable(nom, prenom)) {
            JOptionPane.showMessageDialog(this, "Cet adhérent existe déjà.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");

            PreparedStatement insertStmt = con.prepareStatement("INSERT INTO adherent (nom, prenom, email) VALUES (?, ?, ?)");
            insertStmt.setString(1, nom);
            insertStmt.setString(2, prenom);
            insertStmt.setString(3, email);
            insertStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "<html><font color='green'>Adherent ajouté avec succès.</font></html>");
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierAdherent() {
        String selectedAdherent = (String) adherentsCombo.getSelectedItem();
        if (selectedAdherent != null) {
            String[] parts = selectedAdherent.split(":");
            int idAdherent = Integer.parseInt(parts[0].trim());
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String email = emailField.getText();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");

                PreparedStatement updateStmt = con.prepareStatement("UPDATE adherent SET nom = ?, prenom = ?, email = ? WHERE id_adherent = ?");
                updateStmt.setString(1, nom);
                updateStmt.setString(2, prenom);
                updateStmt.setString(3, email);
                updateStmt.setInt(4, idAdherent);
                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "<html><font color='green'>Adherent mis à jour avec succès.</font></html>");
                } else {
                    JOptionPane.showMessageDialog(this, "Aucun adhérent trouvé avec ces informations.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void supprimerAdherent() {
        String selectedAdherent = (String) adherentsCombo.getSelectedItem();
        if (selectedAdherent != null) {
            String[] parts = selectedAdherent.split(":");
            int idAdherent = Integer.parseInt(parts[0].trim());

            int option = JOptionPane.showConfirmDialog(this, "Êtes-vous sûr de vouloir supprimer cet adhérent ?", "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:8889/biblio", "root", "root");

                    PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM adherent WHERE id_adherent = ?");
                    deleteStmt.setInt(1, idAdherent);
                    int rowsAffected = deleteStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "<html><font color='green'>Adherent supprimé avec succès.</font></html>");
                    } else {
                        JOptionPane.showMessageDialog(this, "Aucun adhérent trouvé avec ces informations.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }

                    con.close();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ManageAdherentWindow();
            }
        });
    }
}
