package com.mayankrastogi.cs474.hw3.commands.ls

import com.mayankrastogi.cs474.hw3.commands.ls.Ls.LsParameters._
import com.mayankrastogi.cs474.hw3.framework.Command

/**
 * Provides a functional, type-safe interface for building the Unix `ls` command.
 *
 * This command can be used to list files and directories at the supplied path. The output is parsed into a list of
 * [[com.mayankrastogi.cs474.hw3.commands.ls.LsResultItem]], which can be used to extract the desired information from
 * the listing.
 */
object Ls {

  /**
   * Type alias for `List[LsResultItem]`.
   */
  type LsResult = List[LsResultItem]

  /**
   * Creates an empty ls command builder.
   *
   * @return A type-safe ls command builder.
   */
  def apply(): LsBuilder[Empty] = LsBuilder(".", ContentAttributes.AllMinusHidden, ContentTypes.All)

  /**
   * Builds an ls command for execution.
   *
   * The builder is private and can only be instantiated by the user by invoking the `apply` method of
   * [[com.mayankrastogi.cs474.hw3.commands.ls.Ls]] object.
   *
   * @param path              The path whose contents are to be listed.
   * @param contentAttributes Desired attributes of the files to be listed.
   * @param contentTypes      The type of directory contents to list.
   * @tparam I Phantom types for building a type-safe builder.
   */
  private[Ls] case class LsBuilder[I <: LsParameters](private val path: String, private val contentAttributes: ContentAttributes, private val contentTypes: ContentTypes) {

    /**
     * List the contents of the logged-in user's home directory.
     *
     * Equivalent to calling the method `directory("~")`.
     *
     * @return A re-configured type-safe builder.
     */
    def homeDirectory: LsBuilder[I with Path] = directory("~")

    /**
     * List the contents of the present working directory.
     *
     * Equivalent to calling the method `directory(".")`.
     *
     * @return A re-configured type-safe builder.
     */
    def currentDirectory: LsBuilder[I with Path] = directory(".")

    /**
     * List the contents of the specified directory.
     *
     * @param path The path to a directory.
     * @return A re-configured type-safe builder.
     */
    def directory(path: String): LsBuilder[I with Path] = {
      // Build a string that will resolve the absolute path to the specified path during execution
      val absolutePath =
        if (path.startsWith("/"))
        // Path is already an absolute path
          path
        else if (path.startsWith("~"))
        // Resolve the absolute path of the home directory
          "'$HOME'/" + path.stripPrefix("~")
        // Resolve the absolute path to the present working directory
        else if (path.startsWith(".") && !path.startsWith(".."))
          "'$PWD'/" + path.stripPrefix(".")
        else
          "'$PWD'/" + path

      copy(path = s"'${absolutePath.stripSuffix("/")}'")
    }

    /**
     * Do NOT show hidden files and directories.
     *
     * @return A re-configured type-safe builder.
     */
    def excludeHidden: LsBuilder[I with Attributes] = copy(contentAttributes = ContentAttributes.AllMinusHidden)

    /**
     * Show hidden files and directories.
     *
     * @return A re-configured type-safe builder.
     */
    def includeHidden: LsBuilder[I with Attributes] = copy(contentAttributes = ContentAttributes.All)

    /**
     * Show all possible types of directory contents.
     *
     * @return A re-configured type-safe builder.
     */
    def showFilesAndDirectories: LsBuilder[I with DirectoryContents] = copy(contentTypes = ContentTypes.All)

    /**
     * Show only directories.
     *
     * @return A re-configured type-safe builder.
     */
    def showDirectoriesOnly: LsBuilder[I with DirectoryContents] = copy(contentTypes = ContentTypes.Directories)

    /**
     * Show only files.
     *
     * @return A re-configured type-safe builder.
     */
    def showFilesOnly: LsBuilder[I with DirectoryContents] = copy(contentTypes = ContentTypes.Files)

    /**
     * Builds the `ls` command for execution.
     *
     * @param ev Implicit evidence that proves that the builder has all the necessary parameters supplied to it for
     *           building the command.
     * @return A command that lists files and directories in the specified directory and returns a list of
     *         [[com.mayankrastogi.cs474.hw3.commands.ls.LsResultItem]]s.
     */
    def build(implicit ev: I <:< MandatoryParameters): Command[LsResult] = {
      val cmd =
      // List directory contents without quotes, in long-listing mode, and last modified time in long-iso format
        Seq("ls", "-ldN", "--time-style=long-iso") ++
          // Show/Hide hidden files
          Seq(contentAttributes match {
            case ContentAttributes.AllMinusHidden => path + "/*"
            case ContentAttributes.All => path + "/{*,.*}"
          }) ++
          // Filter files, directories, or all
          Seq(contentTypes match {
            case ContentTypes.Directories => "| grep ^d"
            case ContentTypes.Files => "| grep ^-"
            case ContentTypes.All => ""
          })

      Command(cmd.mkString(" "))(LsResultParser)
    }
  }

  /**
   * Phantom types for the type-safe builder.
   */
  object LsParameters {

    type MandatoryParameters = Empty with Path

    sealed trait LsParameters

    sealed trait Empty extends LsParameters

    sealed trait Path extends LsParameters

    sealed trait Attributes extends LsParameters

    sealed trait DirectoryContents extends LsParameters

  }

}