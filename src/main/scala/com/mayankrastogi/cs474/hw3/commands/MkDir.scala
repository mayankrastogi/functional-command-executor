package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.MkDir.MkDirParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

object MkDir {

  def apply(): MkDirBuilder[Empty] = MkDirBuilder(".", createParents = false)

  case class MkDirBuilder[I <: MkDirParameters](private val dirName: String, private val createParents: Boolean) {

    def name(dirName: String): MkDirBuilder[I with FilePath] = copy(dirName = dirName)

    def createMissingDirectoriesInPath(flag: Boolean): MkDirBuilder[I with ParentFlag] = copy(createParents = flag)

    def build(implicit ev: I =:= MandatoryParameters): Command[Unit] =
      Command(s"mkdir '$dirName'${if (createParents) " -p" else ""}")
  }

  sealed trait MkDirParameters

  object MkDirParameters {
    type MandatoryParameters = Empty with FilePath with ParentFlag

    sealed trait Empty extends MkDirParameters

    sealed trait FilePath extends MkDirParameters

    sealed trait ParentFlag extends MkDirParameters

  }

}