package de.vfh.paf.entity.texteditor;


/**
 * Class for the text editor client (with user actions) to demonstrate the Command Pattern: Client
 */
public class TextEditorClient {

  /**
   * Main method to demonstrate the Command Pattern: add, replace, undo, redo
   * @param args
   */
  public static void main(String[] args) {
    TextEditor editor = new TextEditor();
    TextCommandManager manager = new TextCommandManager();

    // Creating commands
    // Command 1: Add text
    TextCommand command1 = new TextCommandAdd(editor, "Good evening");
    TextCommand command2 = new TextCommandAdd(editor, " VFH students ! ");
    TextCommand command3 = new TextCommandAdd(editor, "It's a wonderful evening.");

    // Execute commands
    manager.executeCommand(command1);
    manager.executeCommand(command2);
    manager.executeCommand(command3);

    // Undo the last command
    manager.undo();

    // Redo the last undone command
    manager.redo();

    // Replace evening with morning
    TextCommand command4 = new TextCommandReplace(editor, "evening", "morning");
    manager.executeCommand(command4);

    // Undo the last command: replace
    manager.undo();

    // Undo the last 3 commands
    manager.undo();
    manager.undo();
    manager.undo();

    // Try redo after all undos
    manager.undo();

    // Redo the last undone command
    manager.redo();
  }
}
