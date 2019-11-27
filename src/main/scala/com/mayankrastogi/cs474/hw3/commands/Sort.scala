package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Sort.SortParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

object Sort {

  def apply(): SortBuilder[Empty] = SortBuilder(Seq.empty, Seq.empty)

  private[Sort] case class SortBuilder[I <: SortParameters](private val files: Seq[String], private val options: Seq[String]) {

    def addFile(path: String): SortBuilder[I with FilePath] = addAllFiles(Seq(path))

    def addAllFiles(filePaths: Seq[String]): SortBuilder[I with FilePath] = {
      val existingFiles = if (files == null) Seq.empty else files
      copy(files = existingFiles ++ filePaths)
    }

    def ignoreCase: SortBuilder[I] = copy(options = options ++ Seq("-f"))

    def reverse: SortBuilder[I] = copy(options = options ++ Seq("-r"))

    def build(implicit ev: I =:= WithoutPipeParameters): Command[String] =
      Command[String](s"sort ${options.mkString(" ")} ${files.map("'" + _ + "'").mkString(" ")}")

    def buildForPipe(implicit ev: I =:= WithPipeParameters): Command[PipeReceiver] = Command("sort " + options.mkString(" "))
  }

  sealed trait SortParameters

  object SortParameters {

    type WithoutPipeParameters = Empty with FilePath
    type WithPipeParameters = Empty

    sealed trait Empty extends SortParameters

    sealed trait FilePath extends SortParameters

  }

}
