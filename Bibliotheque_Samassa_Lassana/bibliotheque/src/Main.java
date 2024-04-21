import javax.swing.*;

import Bibliotheque.ManageAdherentWindow;
import Bibliotheque.ManageAuteurWindow;
import Bibliotheque.ManageEmpruntWindow;
import Bibliotheque.ManageLivreWindow;

import java.awt.*;
import java.awt.event.*;

public class Main {
    public Main() {
        JFrame frame = new JFrame("Application de Gestion de Bibliothèque");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(2, 2, 10, 10));

        JButton adherentButton = new JButton("Gestion des Adhérents");
        adherentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ManageAdherentWindow();
            }
        });
        frame.add(adherentButton);

        JButton auteurButton = new JButton("Gestion des Auteurs");
        auteurButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ManageAuteurWindow();
            }
        });
        frame.add(auteurButton);

        JButton empruntButton = new JButton("Gestion des Emprunts");
        empruntButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ManageEmpruntWindow();
            }
        });
        frame.add(empruntButton);

        JButton livreButton = new JButton("Gestion des Livres");
        livreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ManageLivreWindow();
            }
        });
        frame.add(livreButton);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }
}
