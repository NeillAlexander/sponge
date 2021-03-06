/**
 * Copyright (c) Neill Alexander. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software
 */
package com.nwalex.sponge.gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.JTextComponent;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author alexanc
 */
public class BodyPanel extends javax.swing.JPanel implements Searchable {

  private final SaveAction saveAction;
  private int displayedRow = -1;
  private int lastHighlight = -1;
  private Highlight[] highlights;

  public BodyPanel() {
    saveAction = null;
  }

  /** Creates new form BodyPanel */
  public BodyPanel(SaveAction saveAction) {
    this.saveAction = saveAction;
    initComponents();
    saveAction.setCallbacks(editingArea, this);
  }

  public void setText(String text, int row) {
    displayArea.setText(text);
    displayArea.setCaretPosition(0);
    displayedRow = row;
  }

  public String getText() {
    return displayArea.getText();
  }

  @Override
  public void highlightAll(String text, boolean caseSensitive) {
    highlightAll(text, getVisibleTextArea(), caseSensitive);
  }

  private void highlightAll(String pattern, JTextComponent textComp, boolean caseSensitive) {
    Highlighter hilite = textComp.getHighlighter();
    try {
      Document doc = textComp.getDocument();
      String text = doc.getText(0, doc.getLength());
      int pos = 0;

      // Create the regexp for doing the matching
      Pattern searchPattern = Pattern.compile(Pattern.quote(pattern),
              caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
      Matcher searchMatcher = searchPattern.matcher(text);

      // Search for pattern
      while (searchMatcher.find()) {
        doHighlight(hilite, searchMatcher.start(), searchMatcher.end(), Color.YELLOW);
      }

    } catch (BadLocationException e) {
      e.printStackTrace();
    } finally {
      updateHighlights(hilite);
      this.lastHighlight = -1;
    }
  }

  private void resetPreviousHighlight(Highlighter hilite) {
    // reset the previous hilite
    if (lastHighlight > -1) {
      hilite.removeHighlight(highlights[lastHighlight]);
      doHighlight(hilite, highlights[lastHighlight].getStartOffset(), highlights[lastHighlight].getEndOffset(), Color.YELLOW);
    }
  }

  private void updateHighlights(Highlighter hilite) {
    this.highlights = hilite.getHighlights();
    Arrays.sort(this.highlights, new Comparator<Highlight>() {
      @Override
      public int compare(Highlight o1, Highlight o2) {
        return o1.getStartOffset() - o2.getStartOffset();
      }
    });
  }

  private void doHighlight(Highlighter hilite, int start, int end, Color color) {
    try {
      hilite.addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(color));
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void registeredBy(FindDialogController controller) {
    controller.initFindActionOn(displayArea);
    controller.initFindActionOn(editingArea);
  }

  @Override
  public void clearHighlights() {
    clearHighlights(getVisibleTextArea());
  }

  private JTextArea getVisibleTextArea() {
    return editContainer.isVisible() ? editingArea : displayArea;
  }

  private void clearHighlights(JTextComponent textComp) {
    Highlighter hilite = textComp.getHighlighter();
    Highlighter.Highlight[] hilites = hilite.getHighlights();
    for (int i = 0; i < hilites.length; i++) {
      hilite.removeHighlight(hilites[i]);
    }
  }

  @Override
  public boolean findNext() {
    int nextHighlight = lastHighlight + 1;

    Highlighter hilite = getVisibleTextArea().getHighlighter();
    boolean found = false;
    
    if (nextHighlight < highlights.length) {
      getVisibleTextArea().setCaretPosition(highlights[nextHighlight].getStartOffset());

      // change the highlight colour      
      hilite.removeHighlight(highlights[nextHighlight]);
      doHighlight(hilite, highlights[nextHighlight].getStartOffset(),
              highlights[nextHighlight].getEndOffset(), Color.PINK);
      resetPreviousHighlight(hilite);

      lastHighlight = nextHighlight;
      found = true;
    } else {
      // wrap around
      resetPreviousHighlight(hilite);
      lastHighlight = -1;
    }

    updateHighlights(hilite);
    return found;
  }

  public void toggleView() {
    ((CardLayout) getLayout()).next(this);
  }

  public void displayReadOnlyView() {
    ((CardLayout) getLayout()).show(this, "readOnlyCard");
  }

  public void displayEditView() {
    ((CardLayout) getLayout()).show(this, "editCard");
  }

  public int getDisplayedRow() {
    return displayedRow;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    readOnlyContainer = new javax.swing.JPanel();
    readOnlyPanel = new javax.swing.JScrollPane();
    displayArea = new javax.swing.JTextArea();
    editContainer = new javax.swing.JPanel();
    editPanel = new javax.swing.JScrollPane();
    editingArea = new javax.swing.JTextArea();
    buttonPanel = new javax.swing.JPanel();
    saveButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();

    setPreferredSize(new java.awt.Dimension(400, 280));
    setLayout(new java.awt.CardLayout());

    readOnlyContainer.setName("read"); // NOI18N

    displayArea.setColumns(20);
    displayArea.setEditable(false);
    displayArea.setFont(new java.awt.Font("Monospaced", 0, 12));
    displayArea.setRows(5);
    displayArea.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        displayAreaMouseClicked(evt);
      }
    });
    readOnlyPanel.setViewportView(displayArea);

    javax.swing.GroupLayout readOnlyContainerLayout = new javax.swing.GroupLayout(readOnlyContainer);
    readOnlyContainer.setLayout(readOnlyContainerLayout);
    readOnlyContainerLayout.setHorizontalGroup(
      readOnlyContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 400, Short.MAX_VALUE)
      .addGroup(readOnlyContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(readOnlyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
    );
    readOnlyContainerLayout.setVerticalGroup(
      readOnlyContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 327, Short.MAX_VALUE)
      .addGroup(readOnlyContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(readOnlyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
    );

    add(readOnlyContainer, "readOnlyCard");

    editContainer.setName("write"); // NOI18N

    editingArea.setColumns(20);
    editingArea.setFont(new java.awt.Font("Monospaced", 0, 12));
    editingArea.setRows(5);
    editPanel.setViewportView(editingArea);

    saveButton.setAction(this.saveAction);
    saveButton.setText("Save");
    buttonPanel.add(saveButton);

    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });
    buttonPanel.add(cancelButton);

    javax.swing.GroupLayout editContainerLayout = new javax.swing.GroupLayout(editContainer);
    editContainer.setLayout(editContainerLayout);
    editContainerLayout.setHorizontalGroup(
      editContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
      .addGroup(editContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
    );
    editContainerLayout.setVerticalGroup(
      editContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(editContainerLayout.createSequentialGroup()
        .addContainerGap(294, Short.MAX_VALUE)
        .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addGroup(editContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editContainerLayout.createSequentialGroup()
          .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
          .addGap(41, 41, 41)))
    );

    add(editContainer, "editCard");
  }// </editor-fold>//GEN-END:initComponents

  private void displayAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_displayAreaMouseClicked
    if (evt.getClickCount() > 1 && displayArea.getText().trim().length() > 0) {
      Rectangle visibleArea = displayArea.getVisibleRect();
      editingArea.setText(displayArea.getText());
      editingArea.setCaretPosition(editingArea.viewToModel(evt.getPoint()));
      displayEditView();
      editingArea.scrollRectToVisible(visibleArea);
      editingArea.requestFocusInWindow();
    }
  }//GEN-LAST:event_displayAreaMouseClicked

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    toggleView();
  }//GEN-LAST:event_cancelButtonActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel buttonPanel;
  private javax.swing.JButton cancelButton;
  private javax.swing.JTextArea displayArea;
  private javax.swing.JPanel editContainer;
  private javax.swing.JScrollPane editPanel;
  private javax.swing.JTextArea editingArea;
  private javax.swing.JPanel readOnlyContainer;
  private javax.swing.JScrollPane readOnlyPanel;
  private javax.swing.JButton saveButton;
  // End of variables declaration//GEN-END:variables

  public static abstract class SaveAction extends JXTableSingleRowAction {

    private JTextArea textArea;
    private BodyPanel bodyPanel;

    public SaveAction(JXTable table) {
      super(table);
    }

    @Override
    protected void singleRowActionPerformed(int index) {
      saveText(textArea.getText(), bodyPanel.getDisplayedRow());
      bodyPanel.toggleView();
    }

    protected void setCallbacks(JTextArea textArea, BodyPanel bp) {
      this.bodyPanel = bp;
      this.textArea = textArea;
    }

    public abstract void saveText(String text, int row);
  }
}
