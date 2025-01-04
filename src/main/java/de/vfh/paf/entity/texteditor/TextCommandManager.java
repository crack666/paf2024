package de.vfh.paf.entity.texteditor;

import java.util.Stack;

/**
 * Class to manage text commands to demonstrate the Command Pattern: Invoker
 */
class TextCommandManager {
  private Stack<TextCommand> undoStack = new Stack<>();
  private TextCommand lastCommand;

  /**
   * Execute a command
   * @param command command to be executed
   */
  public void executeCommand(TextCommand command) {
    command.execute();
    undoStack.push(command);
    lastCommand = command;
  }

  /**
   * Undo the last command
   */
  public void undo() {
    if (undoStack.isEmpty()) {
      System.out.println("Nothing to undo");
      return;
    }
    TextCommand command = undoStack.pop(); // Stack contains command objects
    command.undo();
    lastCommand = command;
  }

  /**
   * Redo the last undone command
   */
  public void redo() {
    if (lastCommand == null) {
      System.out.println("Nothing to redo");
      return;
    }
    lastCommand.execute();
    undoStack.push(lastCommand);
  }
}
