/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package unimet.proyectoso2;

import javax.swing.UIManager;
import unimet.proyectoso2.gui.MainFrame;

/**
 *
 * @author diego
 */
public class ProyectoSO2 {

    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el LookAndFeel del sistema.");
        }
        
        java.awt.EventQueue.invokeLater(() -> {
            MainFrame ventanaPrincipal = new MainFrame();
            ventanaPrincipal.setVisible(true);
        });
    }
}