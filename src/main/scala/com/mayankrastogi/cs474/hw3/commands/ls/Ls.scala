package com.mayankrastogi.cs474.hw3.commands.ls

import com.mayankrastogi.cs474.hw3.framework.Command

object Ls {

  import LsParameters._

  def apply(): LsBuilder[Empty] = LsBuilder(".", ContentAttributes.AllMinusHidden, ContentTypes.All)

  type LsResult = List[LsResultItem]

  private[Ls] case class LsBuilder[I <: LsParameters](private val path: String, private val contentAttributes: ContentAttributes, private val contentTypes: ContentTypes) {
    def homeDirectory: LsBuilder[I with Path] = directory("~")

    def currentDirectory: LsBuilder[I with Path] = directory(".")

    def directory(path: String): LsBuilder[I with Path] = {
      val absolutePath =
        if (path.startsWith("/"))
          path
        else if (path.startsWith("~"))
          "\"$HOME\"/" + path.stripPrefix("~")
        else if (path.startsWith(".") && !path.startsWith(".."))
          "\"$PWD\"/" + path.stripPrefix(".")
        else
          "\"$PWD\"/" + path

      copy(path = s"'${absolutePath.stripSuffix("/")}'")
    }

    def excludeHidden: LsBuilder[I with Attributes] = copy(contentAttributes = ContentAttributes.AllMinusHidden)

    def includeHidden: LsBuilder[I with Attributes] = copy(contentAttributes = ContentAttributes.All)

    def showFilesAndDirectories: LsBuilder[I with DirectoryContents] = copy(contentTypes = ContentTypes.All)

    def showDirectoriesOnly: LsBuilder[I with DirectoryContents] = copy(contentTypes = ContentTypes.Directories)

    def showFilesOnly: LsBuilder[I with DirectoryContents] = copy(contentTypes = ContentTypes.Files)

    def build(implicit ev: I <:< MandatoryParameters): Command[LsResult] = {
      val cmd =
        Seq("ls", "-ldN", "--time-style=long-iso") ++
          Seq(contentAttributes match {
            case ContentAttributes.AllMinusHidden => path + "/*"
            case ContentAttributes.All => path + "/{*,.*}"
          }) ++
          Seq(contentTypes match {
            case ContentTypes.Directories => "| grep ^d"
            case ContentTypes.Files => "| grep ^-"
            case ContentTypes.All => ""
          })

      Command(cmd.mkString(" "))(LsResultParser)
    }
  }

  sealed trait LsParameters

  object LsParameters {

    type MandatoryParameters = Empty with Path

    sealed trait Empty extends LsParameters

    sealed trait Path extends LsParameters

    sealed trait Attributes extends LsParameters

    sealed trait DirectoryContents extends LsParameters

  }

}