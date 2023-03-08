package org.sonarlint.intellij.ui;

import com.google.common.util.concurrent.AtomicDouble;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class TestNewUiPanelPanel extends JPanel {

  private boolean isCollapsed;
  private final JPanel panLabel;
  private static final int DELAY = 1;
  private final double targetBase;

  public JPanel getPanLabel() {
    return panLabel;
  }

  public TestNewUiPanelPanel() {
    super();
    isCollapsed = true;

    setLayout(new BorderLayout());
    panLabel = new JPanel(new BorderLayout());

    var label = new JLabel("Test Notif");
    add(label, BorderLayout.NORTH);
    add(panLabel, BorderLayout.CENTER);
    panLabel.setVisible(!isCollapsed);

    targetBase = getPreferredSize().getHeight() + label.getPreferredSize().getHeight();

    resizeOnCreation();

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        isCollapsed = !isCollapsed;
        if (!isCollapsed) {
          panLabel.setVisible(true);
        }

        var targetHeight = targetBase + (int) panLabel.getPreferredSize().getHeight() + 10;
        var currentStep = new AtomicInteger();
        var heightStep = !isCollapsed ? 1 : -1;

        var timer = new Timer(DELAY, actionEvent -> {
          var newHeight = getHeight() + heightStep;
          setPreferredSize(new Dimension(getWidth(), newHeight));
          currentStep.getAndIncrement();
          if (currentStep.get() >= targetHeight) {
            ((Timer) actionEvent.getSource()).stop();
            if (isCollapsed) {
              panLabel.setVisible(false);
            }
          }
          revalidate();
          repaint();
        });
        timer.start();
      }
    });
  }

  private void resizeOnCreation() {
    var currentStep = new AtomicInteger();
    var heightStep = 1;
    var currentHeight = new AtomicDouble();

    var timer = new Timer(DELAY * 5, actionEvent -> {
      var newHeight = currentHeight.get() + heightStep;
      currentHeight.addAndGet(heightStep);
      setPreferredSize(new Dimension(getWidth(), (int) Math.round(newHeight)));
      currentStep.getAndIncrement();
      if (currentStep.get() >= targetBase) {
        ((Timer) actionEvent.getSource()).stop();
      }
      revalidate();
      repaint();
    });
    timer.start();
  }

  public void resizeOnDeletion(JPanel main) {
    var currentStep = new AtomicInteger();
    var heightStep = 1;
    var baseHeight = getHeight();
    var currentHeight = new AtomicDouble();
    currentHeight.addAndGet(getHeight());

    var realDelay = isCollapsed ? (DELAY * 5) : DELAY;

    var timer = new Timer(realDelay, actionEvent -> {
      var newHeight = currentHeight.get() - heightStep;
      currentHeight.addAndGet(-heightStep);
      setPreferredSize(new Dimension(getWidth(), (int) Math.round(newHeight)));
      currentStep.getAndIncrement();
      if (currentStep.get() >= baseHeight) {
        ((Timer) actionEvent.getSource()).stop();
        main.remove(this);
        main.revalidate();
        main.repaint();
      }
      revalidate();
      repaint();
    });
    timer.start();
  }

}
