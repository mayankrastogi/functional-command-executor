package com.mayankrastogi.cs474.hw3.framework

/**
 * Enables syntax like:
 * <pre>
 * Do(someCommand).andThen(someOtherCommand)
 * </pre>
 *
 * Leaves the supplied command untouched.
 */
object Do {

  /**
   * Enables syntax like:
   * <pre>
   * Do(someCommand).andThen(someOtherCommand)
   * </pre>
   *
   * Leaves the supplied command untouched.
   *
   * @param command The command to execute.
   * @tparam T The type of output produced by the supplied command.
   * @return The supplied command itself.
   */
  def apply[T](command: Command[T]): Command[T] = command
}
