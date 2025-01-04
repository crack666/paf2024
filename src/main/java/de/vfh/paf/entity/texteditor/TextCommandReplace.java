package de.vfh.paf.entity.texteditor;

/**
 * Class for the concrete Command: replace text command
 */
class TextCommandReplace implements TextCommand {
  private TextEditor editor; // Receiver object = text editor
  private String text; // text to be replaced
  private String replacement; // replacement text

  /**
   * Constructor
   * @param editor text editor
   * @param text text to be replaced
   * @param replacement replacement text
   */
  public TextCommandReplace(TextEditor editor, String text, String replacement) {
    this.editor = editor;
    this.text = text;
    this.replacement = replacement;
  }

  /**
   * Execute the command: replace the text
   */
  @Override
  public void execute() {
    String newText = editor.getText().replaceAll(text, replacement);
    editor.setText(newText);
  }

  /**
   * Undo the command: replace the replacement text with the original text
   */
  @Override
  public void undo() {
    String newText = editor.getText().replaceAll(replacement, text);
    editor.setText(newText);
  }

}
