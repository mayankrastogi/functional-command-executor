package com.mayankrastogi.cs474.hw3.commands

import com.mayankrastogi.cs474.hw3.commands.MkDir.MkDirParameters._
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

/**
 * Provides a functional, type-safe interface for building the Unix `mkdir` command.
 *
 * This command can be used to create directories at a specified path.
 */
object MkDir {

  /**
   * Creates an empty mkdir command builder.
   *
   * @return A type-safe mkdir command builder.
   */
  def apply(): MkDirBuilder[Empty] = MkDirBuilder(".", createParents = false)

  /**
   * Builds a mkdir command for execution.
   *
   * The builder is private and can only be instantiated by the user by invoking the `apply` method of
   * [[com.mayankrastogi.cs474.hw3.commands.MkDir]] object.
   *
   * @param dirName       The path at which to create the directory.
   * @param createParents Should missing directories in the path be created.
   * @tparam I Phantom types for building a type-safe builder.
   */
  private[MkDir] case class MkDirBuilder[I <: MkDirParameters](private val dirName: String, private val createParents: Boolean) {

    /**
     * The name or path of the directory to be created.
     *
     * @return A re-configured type-safe builder.
     */
    def name(dirName: String): MkDirBuilder[I with FilePath] = copy(dirName = dirName)

    /**
     * Create parent directories as needed and raise no error if directory already exists, when set to `true`. An error
     * is raised if a parent directory is missing or if the directory being created already exists, when reset to
     * `false` (default).
     *
     * Equivalent to the `-p` option of the mkdir command if the flag is set to `true`.
     *
     * @return A re-configured type-safe builder.
     */
    def createMissingDirectoriesInPath(flag: Boolean): MkDirBuilder[I with ParentFlag] = copy(createParents = flag)

    /**
     * Builds the `mkdir` command for execution.
     *
     * @param ev Implicit evidence that proves that the builder has all the necessary parameters supplied to it for
     *           building the command.
     * @return A command that creates directory(ies) at the specified path and generates no output.
     */
    def build(implicit ev: I =:= MandatoryParameters): Command[Unit] =
      Command(s"mkdir '$dirName'${if (createParents) " -p" else ""}")
  }

  /**
   * Phantom types for the type-safe builder.
   */
  object MkDirParameters {

    type MandatoryParameters = Empty with FilePath with ParentFlag

    sealed trait MkDirParameters

    sealed trait Empty extends MkDirParameters

    sealed trait FilePath extends MkDirParameters

    sealed trait ParentFlag extends MkDirParameters

  }

}