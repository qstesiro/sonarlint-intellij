package org.sonarlint.intellij.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TestNewUiPanelPanel extends JPanel {

  private boolean isContentVisible;
  private final JPanel panLabel;
  private static final long DELAY = 10;
  private final double targetBase;
  private ScheduledFuture<?> myTicker;

  public JPanel getPanLabel() {
    return panLabel;
  }

  public TestNewUiPanelPanel() {
    super();
    isContentVisible = false;

    setLayout(new BorderLayout());
    panLabel = new JPanel(new BorderLayout());

    var label = new JLabel("Test Notif");
    add(label, BorderLayout.NORTH);
    add(panLabel, BorderLayout.CENTER);
    panLabel.setVisible(isContentVisible);

    targetBase = getPreferredSize().getHeight() + label.getPreferredSize().getHeight();

    resizeOnCreation();

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        panLabel.setVisible(!isContentVisible);

        var targetHeight = isContentVisible ? targetBase : (targetBase + (int) panLabel.getPreferredSize().getHeight() + 10);
        var heightStep = isContentVisible ? -1 : 1;

        myTicker = Executors.newSingleThreadScheduledExecutor()
          .scheduleWithFixedDelay(() -> onTick(heightStep, targetHeight, !isContentVisible, false, null), 0, DELAY, TimeUnit.MICROSECONDS);
      }
    });
  }

  private void onTick(int heightStep, double targetHeight, boolean finalState, boolean deletion, JPanel main) {
    var newHeight = getHeight() + heightStep;
    setPreferredSize(new Dimension(getWidth(), newHeight));
    revalidate();
    repaint();
    if ((isContentVisible && getPreferredSize().getHeight() <= targetHeight) ||
      (!isContentVisible && getPreferredSize().getHeight() >= targetHeight)) {
      stopTicker(finalState, deletion, main);
    }
  }

  private void stopTicker(boolean isContentVisible, boolean deletion, JPanel main) {
    if (myTicker != null) {
      myTicker.cancel(false);
      myTicker = null;
    }
    this.isContentVisible = isContentVisible;
    if (deletion) {
      main.remove(this);
      main.revalidate();
      main.repaint();
    }
  }

  private void resizeOnCreation() {
    myTicker = Executors.newSingleThreadScheduledExecutor()
      .scheduleWithFixedDelay(() -> onTick(1, targetBase, isContentVisible, false, null), 0, DELAY * 5, TimeUnit.MICROSECONDS);
  }

  public void resizeOnDeletion(JPanel main) {
    var realDelay = isContentVisible ? DELAY : (DELAY * 5);
    myTicker = Executors.newSingleThreadScheduledExecutor()
      .scheduleWithFixedDelay(() -> onTick(-1, 0, isContentVisible, true, main), 0, realDelay, TimeUnit.MICROSECONDS);
  }

}
