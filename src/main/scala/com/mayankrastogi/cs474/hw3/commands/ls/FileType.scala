package com.mayankrastogi.cs474.hw3.commands.ls

sealed trait FileType

object FileType {

  def parseFrom(string: String): FileType = string match {
    case "d" => FileType.Directory
    case "l" => FileType.SymbolicLink
    case _ => FileType.File
  }

  case object Directory extends FileType

  case object File extends FileType

  case object SymbolicLink extends FileType

}