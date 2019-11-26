package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Cd.CdParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

object Cd {

  def apply(): CdBuilder[Empty] = CdBuilder(".")

  case class CdBuilder[I <: CdParameters](private val path: String) {

    def home: CdBuilder[I with Path] = CdBuilder("")

    def path(path: String): CdBuilder[I with Path] = CdBuilder(path)

    def build(implicit ev: I =:= MandatoryParameters): Command[Unit] = Command("cd " + path)
  }

  sealed trait CdParameters

  object CdParameters {
    type MandatoryParameters = Empty with Path

    sealed trait Empty extends CdParameters

    sealed trait Path extends CdParameters

  }

}