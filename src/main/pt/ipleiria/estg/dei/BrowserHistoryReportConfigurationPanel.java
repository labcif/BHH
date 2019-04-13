/*
 * Autopsy Forensic Browser
 *
 * Copyright 2012-2018 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package main.pt.ipleiria.estg.dei;

import main.pt.ipleiria.estg.dei.db.DatasetRepository;
import main.pt.ipleiria.estg.dei.exceptions.BrowserHistoryReportModuleExpection;
import main.pt.ipleiria.estg.dei.exceptions.ConnectionException;
import main.pt.ipleiria.estg.dei.utils.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * The panel shown for all TableReportModules when configuring report modules.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
public class BrowserHistoryReportConfigurationPanel extends javax.swing.JPanel {
    private List<String> usersSelected;
    private Logger logger = new Logger<>(BrowserHistoryReportConfigurationPanel.class);

    /**
     * Creates new form BrowserHistoryReportConfigurationPanel
     */
    public BrowserHistoryReportConfigurationPanel() {
        initComponents();
        users.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) {
                usersSelected= users.getSelectedValuesList();
            }
        });
        fillUsers();
    }
    private void fillUsers() {
        DefaultListModel dlm = new DefaultListModel();
        try {
            List<String> users = DatasetRepository.getInstance().getUsers();
            //TODO: check list is not empty

            users.forEach(dlm::addElement);
            this.users.setModel(dlm);
            this.users.setSelectedIndex(0);
        } catch (ConnectionException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();//TODO: If the ingest module has been run yet it can't be executed
        }
    }

    public boolean isMostVisitedSitesEnabled() {
        return mostVisitedSites.isSelected();
    }
    public boolean isBlokedSitesEnabled() {
        return blokedSites.isSelected();
    }
    public boolean isWordsSearchEnabled() {
        return wordsSearch.isSelected();
    }
    public boolean isDomainDailyVisitsEnabled() {
        return domainDailyVisits.isSelected();
    }

    public String getGraphType() {
        return buttonGroup2.getSelection().toString();
    }

    public List<String> getUsersSelected() {
        return usersSelected;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        users = new javax.swing.JList<>();
        selectDefaultUsers = new javax.swing.JRadioButton();
        selectAllUsers = new javax.swing.JRadioButton();
        panel1 = new java.awt.Panel();
        histogram = new javax.swing.JRadioButton();
        barGraf = new javax.swing.JRadioButton();
        pieChart = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        mostVisitedSites = new javax.swing.JCheckBox();
        blokedSites = new javax.swing.JCheckBox();
        wordsSearch = new javax.swing.JCheckBox();
        domainDailyVisits = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        querieTextArea = new javax.swing.JTextArea();
        importFilterButton = new javax.swing.JButton();
        exportFilterButton = new javax.swing.JButton();
        querieFilePath = new javax.swing.JTextField();

        setFont(getFont().deriveFont(getFont().getStyle() & ~java.awt.Font.BOLD, 11));
        setLayout(null);
        add(filler1);
        filler1.setBounds(483, 401, 0, 0);

        jScrollPane1.setViewportView(users);

        buttonGroup1.add(selectDefaultUsers);
        selectDefaultUsers.setSelected(true);
        selectDefaultUsers.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.selectDefaultUsers.text")); // NOI18N
        selectDefaultUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usersSelected(evt);
            }
        });

        buttonGroup1.add(selectAllUsers);
        selectAllUsers.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.selectAllUsers.text")); // NOI18N
        selectAllUsers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usersSelected(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectDefaultUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectAllUsers))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(selectDefaultUsers)
                        .addGap(18, 18, 18)
                        .addComponent(selectAllUsers))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(77, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        buttonGroup2.add(histogram);
        histogram.setSelected(true);
        histogram.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.histogram.text")); // NOI18N
        histogram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                histogramActionPerformed(evt);
            }
        });

        buttonGroup2.add(barGraf);
        barGraf.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.barGraf.text")); // NOI18N

        buttonGroup2.add(pieChart);
        pieChart.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.pieChart.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.jLabel1.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.jLabel3.text")); // NOI18N

        mostVisitedSites.setSelected(true);
        mostVisitedSites.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.mostVisitedSites.text")); // NOI18N

        blokedSites.setSelected(true);
        blokedSites.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.blokedSites.text")); // NOI18N
        blokedSites.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blokedSitesActionPerformed(evt);
            }
        });

        wordsSearch.setSelected(true);
        wordsSearch.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.wordsSearch.text")); // NOI18N

        domainDailyVisits.setSelected(true);
        domainDailyVisits.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.domainDailyVisits.text")); // NOI18N

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(104, 104, 104)
                        .addComponent(jLabel3))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(histogram)
                            .addComponent(barGraf, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pieChart, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(66, 66, 66)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mostVisitedSites)
                            .addComponent(blokedSites)
                            .addComponent(wordsSearch)
                            .addComponent(domainDailyVisits))))
                .addContainerGap(53, Short.MAX_VALUE))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(mostVisitedSites)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(blokedSites)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(wordsSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(domainDailyVisits))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(histogram)
                        .addGap(18, 18, 18)
                        .addComponent(barGraf)
                        .addGap(18, 18, 18)
                        .addComponent(pieChart)))
                .addContainerGap(60, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.panel1.TabConstraints.tabTitle"), panel1); // NOI18N

        querieTextArea.setColumns(20);
        querieTextArea.setRows(5);
        jScrollPane2.setViewportView(querieTextArea);

        importFilterButton.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.importFilterButton.text")); // NOI18N
        importFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importQuerieButtonActionPerformed(evt);
            }
        });

        exportFilterButton.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.exportFilterButton.text")); // NOI18N
        exportFilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportQuerieButtonActionPerformed(evt);
            }
        });

        querieFilePath.setText(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.querieFilePath.text")); // NOI18N
        querieFilePath.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(querieFilePath, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addComponent(importFilterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(exportFilterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(47, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(querieFilePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(importFilterButton)
                    .addComponent(exportFilterButton))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(BrowserHistoryReportConfigurationPanel.class, "BrowserHistoryReportConfigurationPanel.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        add(jTabbedPane1);
        jTabbedPane1.setBounds(0, 0, 410, 270);
    }// </editor-fold>//GEN-END:initComponents

    private void blokedSitesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blokedSitesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_blokedSitesActionPerformed

    private void histogramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_histogramActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_histogramActionPerformed

    private void usersSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usersSelected
        if(selectDefaultUsers.isSelected()){
            this.users.setSelectedIndex(0);
        }else{
            this.users.setSelectionInterval(0, users.getModel().getSize() -1 );
        }
    }//GEN-LAST:event_usersSelected

    private void importQuerieButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importQuerieButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(this);
        String chosenFile;
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            chosenFile = chooser.getSelectedFile().getAbsolutePath();

            querieFilePath.setText(chosenFile);

            String line;
            String[] word;

            try(BufferedReader buffer = new BufferedReader(new FileReader(chosenFile))) {
                while ((line = buffer.readLine()) != null) {
                    word = line.split(",");
                    querieTextArea.append(word[0] + ";\n");
                }
            } catch (IOException e) {
                logger.error("File couldn't be read. Please look at the logs for more information!");
                throw new BrowserHistoryReportModuleExpection(e.getMessage());
            }
        }else{
            JOptionPane.showMessageDialog(this, "Something went wrong");
        }

    }//GEN-LAST:event_importQuerieButtonActionPerformed

    private void exportQuerieButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportQuerieButtonActionPerformed
        if(!querieTextArea.getText().isEmpty()){
            try (PrintWriter writer = new PrintWriter(new File("queries.csv"))) {
                writer.write(querieTextArea.getText());
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }else{
            JOptionPane.showMessageDialog(this, "Nothing to export");
        }
    }//GEN-LAST:event_exportQuerieButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton barGraf;
    private javax.swing.JCheckBox blokedSites;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JCheckBox domainDailyVisits;
    private javax.swing.JButton exportFilterButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JRadioButton histogram;
    private javax.swing.JButton importFilterButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JCheckBox mostVisitedSites;
    private java.awt.Panel panel1;
    private javax.swing.JRadioButton pieChart;
    private javax.swing.JTextField querieFilePath;
    private javax.swing.JTextArea querieTextArea;
    private javax.swing.JRadioButton selectAllUsers;
    private javax.swing.JRadioButton selectDefaultUsers;
    private javax.swing.JList<String> users;
    private javax.swing.JCheckBox wordsSearch;
    // End of variables declaration//GEN-END:variables

    
}
