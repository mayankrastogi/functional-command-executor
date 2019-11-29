package com.mayankrastogi.cs474.hw3.commands.ls

/**
 * The type of directory contents to list using the Ls command.
 */
sealed trait ContentTypes

/**
 * The companion object to [[com.mayankrastogi.cs474.hw3.commands.ls.ContentTypes]]
 */
object ContentTypes {

  /**
   * Show all possible types of directory contents.
   */
  final case object All extends ContentTypes

  /**
   * Show only directories.
   */
  final case object Directories extends ContentTypes

  /**
   * Show only files.
   */
  final case object Files extends ContentTypes

}
