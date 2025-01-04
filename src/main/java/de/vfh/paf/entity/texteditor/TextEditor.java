package de.vfh.paf.entity.texteditor;

/**
 * Class for the text editor to demonstrate the Command Pattern: Receiver
 */
class TextEditor {
  private StringBuilder text = new StringBuilder();

  /**
   * Add text to the editor
   * @param newText new test to be added
   */
  public void addText(String newText) {
    text.append(newText);
    System.out.println("Text after add: '" + text.toString() + "'");
  }

  /**
   * Remove the last n characters from the text
   * @param length
   */
  public void removeText(int length) {
    if (length > 0 && length <= text.length()) {
      text.delete(text.length() - length, text.length());
    }
    System.out.println("Text after remove: '" + text.toString() + "'");
  }

  /**
   * Get the current text
   * @return test stored in the editor
   */
  public String getText() {
    return text.toString();
  }

  /**
   * Set the text = replaces the current text
   * @param text test to be set
   */
  public void setText(String text) {

    this.text = new StringBuilder(text);
    System.out.println("Text after set: '" + text.toString() + "'");
  }
}
