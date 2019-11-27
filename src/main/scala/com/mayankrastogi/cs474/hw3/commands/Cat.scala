package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Cat.CatParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

object Cat {

  def apply(): CatBuilder[Empty] = CatBuilder(Seq.empty, Seq.empty)


  private[Cat] case class CatBuilder[I <: CatParameters](private val files: Seq[String], private val options: Seq[String]) {

    def addFile(path: String): CatBuilder[I with FilePath] = addAllFiles(Seq(path))

    def addAllFiles(filePaths: Seq[String]): CatBuilder[I with FilePath] = {
      val existingFiles = if (files == null) Seq.empty else files
      copy(files = existingFiles ++ filePaths)
    }

    def showAll: CatBuilder[I with Options] = copy(options = options ++ Seq("-A"))

    def showEnds: CatBuilder[I with Options] = copy(options = options ++ Seq("-E"))

    def showNonPrinting: CatBuilder[I with Options] = copy(options = options ++ Seq("-v"))

    def showTabs: CatBuilder[I with Options] = copy(options = options ++ Seq("-T"))

    def numberOutputLines: CatBuilder[I with Options] = copy(options = options ++ Seq("-n"))

    def build(implicit ev: I <:< MandatoryParameters): Command[String] =
      Command[String](s"cat ${files.map("'" + _ + "'").mkString(" ")} ${options.mkString(" ")}".strip())
  }

  object CatParameters {

    sealed trait CatParameters

    type MandatoryParameters = Empty with FilePath

    sealed trait Empty extends CatParameters

    sealed trait FilePath extends CatParameters

    sealed trait Options extends CatParameters

  }


}