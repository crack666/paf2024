package de.vfh.paf.entity.texteditor;

/**
 * Class for the concrete Command: add text command
 */
class TextCommandAdd implements TextCommand {
  private TextEditor editor; // Receiver object = text editor
  private String text; // text to be added

  /**
   * Constructor
   * @param editor text editor
   * @param text text to be added
   */
  public TextCommandAdd(TextEditor editor, String text) {
    this.editor = editor;
    this.text = text;
  }

  /**
   * Execute the command: add the text
   */
  @Override
  public void execute() {
    editor.addText(text);
  }

  /**
   * Undo the command: remove the text that was added
   */
  @Override
  public void undo() {
    editor.removeText(text.length());
  }

}
