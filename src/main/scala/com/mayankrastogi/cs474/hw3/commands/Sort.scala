package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.Sort.SortParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

/**
 * Provides a functional, type-safe interface for building the Unix `sort` command.
 *
 * This command can be used to sort lines of text in a single or multiple files. This command can also be a candidate
 * for the `Command.pipeTo()` method when built using the `buildForPipe()` method.
 */
object Sort {

  /**
   * Creates an empty sort command builder.
   *
   * @return A type-safe sort command builder.
   */
  def apply(): SortBuilder[Empty] = SortBuilder(Seq.empty, Seq.empty)

  /**
   * Builds a sort command for execution.
   *
   * The builder is private and can only be instantiated by the user by invoking the `apply` method of
   * [[com.mayankrastogi.cs474.hw3.commands.Sort]] object.
   *
   * @param files   A sequence of files (paths) that will be read for sorting.
   * @param options The options for the sort command.
   * @tparam I Phantom types for building a type-safe builder.
   */
  private[Sort] case class SortBuilder[I <: SortParameters](private val files: Seq[String], private val options: Seq[String]) {

    /**
     * Add a file to be read as input.
     *
     * This appends to the list of files that have already been added previously.
     *
     * @param path The path to the file.
     * @return A re-configured type-safe builder.
     */
    def addFile(path: String): SortBuilder[I with FilePath] = addAllFiles(Seq(path))

    /**
     * Add multiple files to be read as input.
     *
     * This appends to the list of files that have already been added previously.
     *
     * @param filePaths The paths to the files.
     * @return A re-configured type-safe builder.
     */
    def addAllFiles(filePaths: Seq[String]): SortBuilder[I with FilePath] = {
      val existingFiles = if (files == null) Seq.empty else files
      copy(files = existingFiles ++ filePaths)
    }

    /**
     * Perform a case-insensitive comparison while sorting.
     *
     * Equivalent to the `-f` option of the sort command.
     *
     * @return A re-configured type-safe builder.
     */
    def ignoreCase: SortBuilder[I] = copy(options = options ++ Seq("-f"))

    /**
     * Sorts the lines in reverse (descending) order.
     *
     * Equivalent to the `-r` option of the sort command.
     *
     * @return A re-configured type-safe builder.
     */
    def reverse: SortBuilder[I] = copy(options = options ++ Seq("-r"))

    /**
     * Builds the `sort` command for execution.
     *
     * @param ev Implicit evidence that proves that the builder has all the necessary parameters supplied to it for
     *           building the command.
     * @return A command that reads the supplied files and sorts the lines.
     */
    def build(implicit ev: I =:= WithoutPipeParameters): Command[String] =
      Command[String](s"sort ${options.mkString(" ")} ${files.map("'" + _ + "'").mkString(" ")}")

    /**
     * Builds the `sort` command that can be passed to the `Command.pipeTo()` method. Requires that any input file is
     * NOT specified.
     *
     * @param ev Implicit evidence that proves that the builder has all the necessary parameters supplied to it for
     *           building the command.
     * @return A command that reads the standard input and sorts the lines.
     */
    def buildForPipe(implicit ev: I =:= WithPipeParameters): Command[PipeReceiver] = Command("sort " + options.mkString(" "))
  }

  /**
   * Phantom types for the type-safe builder.
   */
  object SortParameters {

    type WithoutPipeParameters = Empty with FilePath
    type WithPipeParameters = Empty

    sealed trait SortParameters

    sealed trait Empty extends SortParameters

    sealed trait FilePath extends SortParameters

  }

}
