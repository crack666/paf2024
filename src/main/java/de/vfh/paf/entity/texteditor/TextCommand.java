package de.vfh.paf.entity.texteditor;

import java.util.Stack;

/**
 * Interface TextCommand to demonstrate the Command Pattern: Command Interface
 * methods like undo can be included in the interface to make it more powerful
 */
interface TextCommand {
  /**
   * Execute the command
   */
  void execute();

  /**
   * Undo the command
   */
  void undo();
}
