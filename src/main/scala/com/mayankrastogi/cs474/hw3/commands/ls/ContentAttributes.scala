package com.mayankrastogi.cs474.hw3.commands.ls

/**
 * Desired attributes of the files to be listed by executing the ls command.
 */
sealed trait ContentAttributes

/**
 * Companion object to [[com.mayankrastogi.cs474.hw3.commands.ls.ContentAttributes]]
 */
object ContentAttributes {

  /**
   * Show all directory contents that do not start with the `.` character.
   */
  final case object AllMinusHidden extends ContentAttributes

  /**
   * Show all directory contents with any attribute.
   */
  final case object All extends ContentAttributes

}