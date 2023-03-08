/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015-2023 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.util.ui.JBUI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TestNewUiPanel extends SimpleToolWindowPanel implements Disposable {
  private final JPanel mainPanel;
  private final Project project;
  private final List<TestNewUiPanelPanel> listNotif;

  public TestNewUiPanel(Project project) {
    super(false, true);
    this.project = project;
    mainPanel = new JPanel(new VerticalFlowLayout());

    listNotif = new ArrayList<>();

    var addButton = new JButton("Add notification");
    var removeButton = new JButton("Remove random notification");
    var addAndRemoveButton = new JButton("Add and Remove random notification");

    addButton.addActionListener(actionEvent -> {
      var notif = new TestNewUiPanelPanel();
      notif.setBorder(JBUI.Borders.empty(10));
      notif.setBackground(new Color((int)(Math.random() * 0x1000000)));
      var labelCenter = new JLabel("<html>Random text<br/><br/>New line<br/>New line<br/>New line<br/>New line</html>");
      notif.getPanLabel().add(labelCenter);
      mainPanel.add(notif);
      listNotif.add(notif);
    });

    removeButton.addActionListener(actionEvent -> {
      if (!listNotif.isEmpty()) {
        var index = new Random().nextInt(listNotif.size());
        listNotif.get(index).resizeOnDeletion(mainPanel);
        listNotif.remove(index);
      }
    });

    addAndRemoveButton.addActionListener(actionEvent -> {
      var index = new Random().nextInt(listNotif.size());
      listNotif.get(index).resizeOnDeletion(mainPanel);
      listNotif.remove(index);

      var notif = new TestNewUiPanelPanel();
      notif.setBorder(JBUI.Borders.empty(10));
      notif.setBackground(new Color((int)(Math.random() * 0x1000000)));
      var labelCenter = new JLabel("<html>Random text<br/><br/>New line<br/>New line<br/>New line<br/>New line</html>");
      notif.getPanLabel().add(labelCenter);
      mainPanel.add(notif);
      listNotif.add(notif);
    });

    mainPanel.add(addButton);
    mainPanel.add(removeButton);
    mainPanel.add(addAndRemoveButton);
    super.setContent(mainPanel);
  }

  public JComponent getPanel() {
    return mainPanel;
  }

  @Override
  // called automatically because the panel is one of the content of the tool window
  public void dispose() {
    // Nothing to do
  }
}
