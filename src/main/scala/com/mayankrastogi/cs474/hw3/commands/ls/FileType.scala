package com.mayankrastogi.cs474.hw3.commands.ls

/**
 * The type of directory content.
 */
sealed trait FileType

/**
 * Companion object to [[com.mayankrastogi.cs474.hw3.commands.ls.FileType]]
 */
object FileType {

  /**
   * Gets the [[com.mayankrastogi.cs474.hw3.commands.ls.FileType]] corresponding to the specified character.
   *
   * @param char The character denoting the file type.
   * @return
   */
  def parseFrom(char: Char): FileType = char match {
    case 'd' => FileType.Directory
    case 'l' => FileType.SymbolicLink
    case _ => FileType.File
  }

  case object Directory extends FileType

  case object File extends FileType

  case object SymbolicLink extends FileType

}