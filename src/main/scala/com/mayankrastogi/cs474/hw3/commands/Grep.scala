package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Grep.GrepParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

object Grep {

  def apply(): GrepBuilder[Empty] = GrepBuilder("", Seq.empty, Seq.empty)

  case class GrepBuilder[I <: GrepParameters](private val pattern: String, private val files: Seq[String], private val options: Seq[String]) {

    def pattern(pattern: String): GrepBuilder[I with Pattern] = copy(pattern = pattern)

    def addFile(path: String): GrepBuilder[I with FilePath] = addAllFiles(Seq(path))

    def addAllFiles(filePaths: Seq[String]): GrepBuilder[I with FilePath] = {
      val existingFiles = if (files == null) Seq.empty else files
      copy(files = existingFiles ++ filePaths)
    }

    def ignoreCase: GrepBuilder[I] = copy(options = options ++ Seq("-i"))

    def invertMatch: GrepBuilder[I] = copy(options = options ++ Seq("-v"))

    def build(implicit ev: I =:= WithoutPipeParameters): Command[String] =
      Command[String](s"grep $pattern ${options.mkString(" ")} ${files.map("'" + _ + "'").mkString(" ")}".strip())

    def buildForPipe(implicit ev: I =:= WithPipeParameters): Command[PipeReceiver] =
      Command[PipeReceiver](s"grep '$pattern' ${options.mkString(" ")}".strip())
  }

  sealed trait GrepParameters

  object GrepParameters {

    type WithoutPipeParameters = Empty with Pattern with FilePath
    type WithPipeParameters = Empty with Pattern

    sealed trait Empty extends GrepParameters

    sealed trait Pattern extends GrepParameters

    sealed trait FilePath extends GrepParameters

  }

}