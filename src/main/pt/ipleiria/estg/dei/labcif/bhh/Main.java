package main.pt.ipleiria.estg.dei.labcif.bhh;


import main.pt.ipleiria.estg.dei.labcif.bhh.panels.mainPanel.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = MainFrame.getInstance();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
