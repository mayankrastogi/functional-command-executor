package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Cat.CatParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

/**
 * Provides a functional, type-safe interface for building the Unix `cat` command.
 *
 * This command can be used to display (and concatenate) the contents of a single or multiple files. A file path must be
 * specified by using `addFile` and/or `addFiles` methods.
 */
object Cat {

  /**
   * Creates an empty cat command builder.
   *
   * @return A type-safe cat command builder.
   */
  def apply(): CatBuilder[Empty] = CatBuilder(Seq.empty, Seq.empty)

  /**
   * Builds a cat command for execution.
   *
   * The builder is private and can only be instantiated by the user by invoking the `apply` method of
   * [[com.mayankrastogi.cs474.hw3.commands.Cat]] object.
   *
   * @param files   A sequence of files (paths) that will be read and concatenated.
   * @param options The options for the cat command.
   * @tparam I Phantom types for building a type-safe builder.
   */
  private[Cat] case class CatBuilder[I <: CatParameters](private val files: Seq[String], private val options: Seq[String]) {

    /**
     * Add a file to be read as input.
     *
     * This appends to the list of files that have already been added previously.
     *
     * @param path The path to the file.
     * @return A re-configured type-safe builder.
     */
    def addFile(path: String): CatBuilder[I with FilePath] = addAllFiles(Seq(path))

    /**
     * Add multiple files to be read as input.
     *
     * This appends to the list of files that have already been added previously.
     *
     * @param filePaths The paths to the files.
     * @return A re-configured type-safe builder.
     */
    def addAllFiles(filePaths: Seq[String]): CatBuilder[I with FilePath] = {
      val existingFiles = if (files == null) Seq.empty else files
      copy(files = existingFiles ++ filePaths)
    }

    /**
     * Displays all non-printing characters, including line-endings and tabs.
     *
     * Equivalent to calling both `showEnds` and `showTabs`, or the `-A` option of the cat command.
     *
     * @return A re-configured type-safe builder.
     */
    def showAll: CatBuilder[I with Options] = copy(options = options ++ Seq("-A"))

    /**
     * Adds a `$` symbol before all `LF` (line feed) characters in the output.
     *
     * Equivalent to the `-E` option of the cat command.
     *
     * @return A re-configured type-safe builder.
     */
    def showEnds: CatBuilder[I with Options] = copy(options = options ++ Seq("-E"))

    /**
     * Shows all non-printing characters, except `LF` (line feed) and `TAB` characters.
     *
     * Equivalent to the `-v` option of the cat command.
     *
     * @return A re-configured type-safe builder.
     */
    def showAllExceptEndsAndTab: CatBuilder[I with Options] = copy(options = options ++ Seq("-v"))

    /**
     * Replaces the `TAB` character with `^I` for all occurrences in the output.
     *
     * Equivalent to the `-T` option of the cat command.
     *
     * @return A re-configured type-safe builder.
     */
    def showTabs: CatBuilder[I with Options] = copy(options = options ++ Seq("-T"))

    /**
     * Shows line numbers in the output of concatenation.
     *
     * Equivalent to the `-n` option of the cat command.
     *
     * @return A re-configured type-safe builder.
     */
    def numberOutputLines: CatBuilder[I with Options] = copy(options = options ++ Seq("-n"))

    /**
     * Builds the `cat` command for execution.
     *
     * @param ev Implicit evidence that proves that the builder has all the necessary parameters supplied to it for
     *           building the command.
     * @return A command that reads the supplied files and returns the concatenated output as a [[String]].
     */
    def build(implicit ev: I <:< MandatoryParameters): Command[String] =
      Command[String](s"cat ${files.map("'" + _ + "'").mkString(" ")} ${options.mkString(" ")}".strip())
  }

  /**
   * Phantom types for the type-safe builder.
   */
  object CatParameters {

    type MandatoryParameters = Empty with FilePath

    sealed trait CatParameters

    sealed trait Empty extends CatParameters

    sealed trait FilePath extends CatParameters

    sealed trait Options extends CatParameters

  }

}