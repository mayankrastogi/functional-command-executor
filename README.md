## CS 474 - Object Oriented Languages and Environments
## Homework 3 - Type-safe Linux Command Execution Framework

---

### Overview

The objective of this homework was to design a type-safe, functional, external command processing framework in Scala.

This framework allows you to build and execute Linux commands from Scala programs in a type-safe way.

Here is how you say "Hello, World" with this framework:

```scala

Echo().text("Hello, World!").build.execute.map(println)

```

Here is an example of how you can check whether a file exists in a directory using this framework:

```scala

Do(Cd().path("an/interesting/directory").build)
  .andThen(Ls().currentDirectory.build)
  .execute
  .map(_.filter(_.name == "anInterestingFile"))
  .exists(_.nonEmpty)

```

### Features

- **Functional**, type-safe interface for running Linux commands through Scala programs
- **Type-safe builders** for supported commands using Scala **phantom types**
- Chaining and **conditional execution** of multiple commands, provided previous commands in the chain succeeded
- **Piping** of output of one command to the input of another
- **Output redirection** for writing or appending the standard output of commands to files
- Automatic **parsing** of command execution results
- **Extensible** framework design for supporting more type-safe commands
- Ability to execute **arbitrary commands** so that the user is not restricted to the supported commands
- **Exhaustive test suite** of over 70 test cases to avoid unexpected errors

The following commands are currently supported:

 # | Command | Description                                                     | Type                                  
---|---------|-----------------------------------------------------------------|----------------------------------------
 1 | `Echo`  | Prints the specified text to standard output.                   | `Command[String]`                        
 2 | `MkDir` | Creates a directory.                                            | `Command[Unit]`
 3 | `Cd`    | Switches the working directory.                                 | `Command[Unit]`
 4 | `Ls`    | Lists files and directories inside the specified directory.     | `Command[LsResult]`
 5 | `Cat`   | Concatenates specified files.                                   | `Command[String]`
 6 | `Grep`  | Finds lines matching the specified patterns in the given files. | `Command[String]` / `Command[PipeReceiver]`
 7 | `Sort`  | Sorts lines in the specified files.                             | `Command[String]` / `Command[PipeReceiver]`

### Running Built-In Commands

Type-safe builders are provided for all the commands in `com.mayankrastogi.cs474.hw3.commands` package. The builder instance can be obtained using the `apply` method of the `object` of the corresponding command, namely: `Echo()`, `MkDir()`, `Cd()`, `Ls()`, `Cat()`, `Grep()`, and `Sort()`.

There are 3 types of commands:

1. **Commands that don't produce an output**. These commands are `MkDir()` and `Cd()` and are of type `Command[Unit]`.
2. **Commands that produce an output**. All the commands except `MkDir()` and `Cd()` produce an output. All these commands produce an output of type `String`, with an exception of the `Ls()` command, which produces an output of type `LsResult` (type alias for `List[LsResultItem]`)
3. **Commands that can receive input from pipe**. The `Grep()` and `Sort()` commands are two such commands which can either work stand-alone by reading from files and also as a pipe receiver. In the latter mode, they operate on the standard output produced by some other command and are of type `Command[PipeReceiver]`.

Commands belonging to the **first two categories** are built by invoking the **`build`** method once the builder has been configured. Commands of the third category are built using the **`buildForPipe`** method for piped mode and by the **`build`** method for normal mode. 

#### 1. The `Echo` Command

This command returns the same text that was provided to it. Useful for writing to files by invoking the `writeTo`/`appendTo` methods on the built command.

See [Echo.scala](src/main/scala/com/mayankrastogi/cs474/hw3/commands/Echo.scala) for supported options.

**Example 1: Writing a file using `Echo` command**

```scala

Echo()
  .text(
    """
      |Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut 
      | labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco
      | laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in
      | voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat
      | cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
      |""".stripMargin)
  .build
  .writeTo("de Finibus Bonorum et Malorum.txt")
  .execute
match {
    case Right(_) => println("File written successfully")
    case Left(e) => logger.error("Command failed!", e)
  }

```

#### 2. The `MkDir` Command

This command can be used to create directories at a specified path.

See [MkDir.scala](src/main/scala/com/mayankrastogi/cs474/hw3/commands/MkDir.scala) for more details.

**Example 2: Creating a directory using `MkDir()` command**

```scala

val success = 
    MkDir()
      .name("an/interesting/directory")
      .createMissingDirectoriesInPath(true)
      .build
      .execute
      .isRight

``` 

#### 3. The `Cd` Command

This command can be used to change the working directory of the subsequent commands.

See [Cd.scala](src/main/scala/com/mayankrastogi/cs474/hw3/commands/Cd.scala) for more details and supported options.

**Example 3: Switch to home directory**

```scala

val success =
    Cd()
      .home
      .build
      .execute
      .isRight

```

#### 4. The `Ls` Command

This command can be used to list files and directories at the supplied path. The output is parsed into a list of [LsResultItem](src/main/scala/com/mayankrastogi/cs474/hw3/commands/ls/LsResultItem.scala), which can be used to extract the desired information from the listing.

See [Ls.scala](src/main/scala/com/mayankrastogi/cs474/hw3/commands/ls/Ls.scala) for more details and supported options.

**Example 4: Get the full paths to all hidden files in the home directory for which the user has write permissions**

```scala

Ls()
  .homeDirectory
  .includeHidden
  .showFilesOnly
  .build
  .execute
  .map(
    _.filter(
      _.permissions
        .user
        .contains(PermissionFlag.Write)
    )
  )
  .getOrElse(List.empty)

```

#### 5. The `Cat` Command

This command can be used to display (and concatenate) the contents of a single or multiple files. A file path must be specified by using `addFile` and/or `addFiles` methods.

See [Cat.scala](src/main/scala/com/mayankrastogi/cs474/hw3/commands/Cat.scala) for more details and supported options.

**Example 5: Concatenate 3 log files from 3 different locations and append to the master log file**

```scala

val success =
    Cat()
      .addFile("/logs/location1/log1.txt")
      .addAllFiles(Seq("/logs/location2/log2.txt", "/logs/location3/log3.txt"))
      .build
      .writeTo("/logs/master_log.txt")
      .execute
      .isRight

```

#### 6. The `Grep` Command

This command can be used to filter lines of text in a single or multiple files that match the supplied pattern. This command can also be a candidate for the `Command.pipeTo()` method when built using the `buildForPipe()` method.

See [Grep.scala](src/main/scala/com/mayankrastogi/cs474/hw3/commands/Grep.scala) for more details and supported options.

**Example 6.1: Read 3 log files and search for any errors using `Grep()` in stand-alone mode**

```scala

Grep()
  .pattern("[ERROR]")
  .addFile("/logs/location1/log1.txt")
  .addAllFiles(Seq("/logs/location2/log2.txt", "/logs/location3/log3.txt"))
  .build
  .execute
  .map(println)

```

**Example 6.2: Read 3 log files and search for any errors using a combination of `Cat` and `Grep()` commands**

```scala

Do(
  Cat()
    .addFile("/logs/location1/log1.txt")
    .addAllFiles(Seq("/logs/location2/log2.txt", "/logs/location3/log3.txt"))
    .build
  )
  .pipeTo(Grep().pattern("[ERROR]").buildForPipe)
  .execute
  .map(println)

```

#### 7. The `Sort` Command

This command can be used to sort lines of text in a single or multiple files. This command can also be a candidate for the `Command.pipeTo()` method when built using the `buildForPipe()` method.

See [Sort.scala](src/main/scala/com/mayankrastogi/cs474/hw3/commands/Sort.scala) for more details and supported options.

**Example 7.1: Read 3 log files and sort them using `Sort()` in stand-alone mode**

```scala

Sort()
  .addFile("/logs/location1/log1.txt")
  .addAllFiles(Seq("/logs/location2/log2.txt", "/logs/location3/log3.txt"))
  .build
  .execute
  .map(println)

```

**Example 7.2: Read 3 log files and sort them using a combination of `Cat` and `Sort()` commands**

```scala

Do(
  Cat()
    .addFile("/logs/location1/log1.txt")
    .addAllFiles(Seq("/logs/location2/log2.txt", "/logs/location3/log3.txt"))
    .build
  )
  .pipeTo(Sort().buildForPipe)
  .execute
  .map(println)

```

### Running Arbitrary Commands

Arbitrary commands can be run using the [Command](src/main/scala/com/mayankrastogi/cs474/hw3/framework/Command.scala) framework directly.

First, you will need to import the default implicit parsers

```scala

import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._

```

- Use `Command[Unit]("your_command").execute` for executing a command that produces no output, or the output is of no interest.
- Use `Command[String]("your_command").execute` for executing a command that writes to the standard output.
- Use `Command[PipeReceiver]("your_command")` for a command that accepts input from a pipe.

**Example 1: Print the current working directory**

```scala

Command[String]("pwd").execute.map(println)

```

**Example 2: Print details of a running process**

```scala

Do(Command[String]("ps -aux"))
    .pipeTo(Command[PipeReceiver]("grep interesting_process_name"))
    .execute
    .map(println)

```

### Build Instructions

#### Prerequisites

- [Java 11](https://www.oracle.com/technetwork/java/javase/downloads/index.html) or above
- [SBT](https://www.scala-sbt.org/) installed on your system
- [Windows Subsystem for Linux (WSL)](https://docs.microsoft.com/en-us/windows/wsl/install-win10), if running on Windows

#### Building the framework

1. Clone or download this repository onto your system
2. Open the Command Prompt (if using Windows) or the Terminal (if using Linux/Mac) and browse to the project directory
3. Build the project using SBT
    
    ```
    sbt clean compile
    ```
4. Run the test cases

    ```
    sbt test
    ```
   
### Framework Design

#### The `Command` Class

Provides a functional interface for executing Unix commands using the *Bourne-again shell* or `bash`. 

It supports **conditional execution** of multiple commands using the exit code for the previous command in the chain. A functional interface for the `&&` conditional is provided using the `andThen()` method. When adding a command using `andThen()`, any output to the **standard output**, produced by the commands prior to the last command, will be suppressed. *The framework design assumes that the user will always be interested in the output of the last command executed*, and that all the previous commands do the job of setting up the environment required for the last command to work, e.g. creating necessary files and directories using the `MkDir` command, changing the working directory using the `Cd`, etc. 

Likewise, **piping** is supported using the `pipeTo` method. The framework design assumes that the *piped receiver* (the command to which the output of the previous command is piped), will perform some operations on its input and produce an output that leaves the structure of the input command intact, e.g. `Grep()` and `Sort()` either filter or re-order the output of the input command while leaving the format of individual lines intact. This allows us to filter the output of `Ls()` command using `Grep()` and obtain a list of `LsResultItem`s so that the user may extract the desired information from the output in a type-safe way. 

The framework also supports **redirection of standard output** to files using the `writeTo` and `appendTo` methods. This allows creation of files using the `Echo()` command. 

The final **execution** is triggered by the `execute()` method which prepares the final command for execution, executes it using `bash`, and returns `Either` an error if the command execution fails, or an output of type specified in the type parameter of the `Command`. The actual command string that will be executed under the hood can be obtained using the `toString` method.

If a command executes successfully, everything written to the standard output during the execution, will be sent to a `CommandResultParser` to **parse** the `String` output to the type specified in the type parameter of the Command. Default implicit parsers for `Unit`, `String`, and `PipeReceiver` are provided in `CommandResultParser.DefaultParsers` object. 

Apart from the in-built commands provided in the `com.mayankrastogi.cs474.hw3.commands` package, a user can **run arbitrary commands** using `Command[Unit]("a_command")`, if the command is expected to produce no output (or the output is not interesting), or by using `Command[String]("a_command")` for a command that produces string output. The parsers for these commands can be provided implicitly by adding `import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._`.

**Note:** If the JVM is running on Microsoft Windows, command execution is delegated to Bash via the **Windows Subsystem for Linux (WSL)**. The framework will not work if WSL is not installed on Windows.

#### The `CommandResultParser` Trait

The `CommandResultParser` trait defines the contract for implementing command output parsers for parsing the output received by executing a `Command`.

Parsers must implement the `parseFrom()` method to to define how the `String` output obtained from the command execution should be converted to the desired type.

To make it easier for users and developers of the framework to work with the most common output formats, three implicit parsers are provided in the `CommandResultParser.DefaultParsers` object:

- **`unitParser`:** Parses string output to nothing by doing nothing.
- **`stringParser`:** Parses string output to the same string.
- **`pipeReceiverParser`:** Parses string output to a dummy pipe receiver that does nothing.

### Test Case Execution Report

```

[info] Loading global plugins from C:\Users\send2\.sbt\1.0\plugins
[info] Loading project definition from D:\Projects\cs474\mayank_k_rastogi_cs474_hw3\project
[info] Loading settings for project mayank_k_rastogi_cs474_hw3 from build.sbt ...
[info] Set current project to mayank_k_rastogi_cs474_hw3 (in build file:/D:/Projects/cs474/mayank_k_rastogi_cs474_hw3/)
[info] Compiling 12 Scala sources to D:\Projects\cs474\mayank_k_rastogi_cs474_hw3\target\scala-2.13\test-classes ...
[warn] there were two feature warnings; re-run with -feature for details
[warn] one warning found
[info] Done compiling.
[info] MkDirTest:
[info] MkDir Command with `createMissingDirectoriesInPath` = `false`
[info] - must be able to create a new directory when parent exists
[info] - must fail to create a new directory when any parents in the path don't exist
[info] - must fail if the directory already exists
[info] MkDir Command with `createMissingDirectoriesInPath` = `true`
[info] - must be able to create a new directory when parent exists
[info] - must be able to create the new directory while creating any missing directories in the parent path
[info] - must succeed even if the directory already exists
[info] LsTest:
[info] Ls Command
[info] - must contain the test directory when listing the current directory
[info] - must list only non-hidden files and directories with default options
[info] - must list only non-hidden files and directories when `excludeHidden` is switched on
[info] SortTest:
[info] - must list non-hidden files and directories when `showFilesAndDirectories is switched on
[info] Sort Command in normal mode
[info] - must list all files and directories when `includeHidden` is switched on
[info] - must sort the lines in a single file
[info] - must list only directories when `showDirectoriesOnly` is switched on
[info] - must sort the lines in multiple files
[info] - must list only files when `showFilesOnly` is switched on
[info] - must sort the lines ignoring case when `ignoreCase` is switched on
[info] LsResultParser
[info] - must sort the lines in reverse order when `reverse` option is switched on
[info] - must parse an output line denoting a directory listing correctly
[info] - must sort the lines in reverse order while ignoring case when both `reverse` and `ignoreCase` are switched on
[info] - must parse an output line correctly for a directory having spaces in its name
[info] Sort Command in piped mode
[info] - must parse an output line correctly for a symbolic link listing
[info] - must sort the lines without ignoring case
[info] - must parse an output line correctly for a file listing
[info] - must sort the lines ignoring case when `ignoreCase` is switched on
[info] - must parse the permission as `Execute` when sticky bit 't' is present
[info] - must sort the lines in reverse order when `reverse` option is switched on
[info] CommandTest:
[info] - must sort the lines in reverse order while ignoring case when both `reverse` and `ignoreCase` are switched on
[info] A Command
[info] - must use Windows Subsystem for Linux (WSL) if running on Windows
[info] - must have no fragments if it's the only command in chain
[info] - must put the result of execution in `Right` if executed successfully
[info] - must put the exception details in `Left` if execution fails
[info] A Command, when combined using `andThen`
[info] - must contain the last command in `cmd` and all prior commands in `fragments`
[info] - must have at least (n - 1) `&&` operators for n commands
[info] - must be in the same order as they were combined
[info] - must suppress output from all commands but the last, unless it redirects its output
[info] A Command, when combined using `pipeTo`
[info] - must keep all `fragments` from the lhs command and discard all `fragments` from the rhs command
[info] - must have the `cmd` of the first command piped with the `cmd` of the second command
[info] A Command, when combined using `writeTo`
[info] - must set the `redirectsOutput` flag to `true`
[info] - must preserve the `fragments` of the original command
[info] - must add a `>` operator to the `cmd` of the command followed by the file path in single-quotes
[info] A Command, when combined using `appendTo`
[info] - must set the `redirectsOutput` flag to `true`
[info] - must preserve the `fragments` of the original command
[info] - must add a `>>` operator to the `cmd` of the command followed by the file path in single-quotes
[info] CommandResultParserTest:
[info] - `unitParser` must do nothing for any input
[info] - `stringParser` must return the same string unchanged
[info] - `pipeReceiverParser` must always return the same dummy receiver instance, irrespective of the input
[info] GrepTest:
[info] Grep Command in normal mode
[info] - must fail if no match is found
[info] - must return the lines that match the pattern in a single file without ignoring case
[info] - must return the lines that match the pattern in multiple files without ignoring case
[info] - must return the lines that match the pattern ignoring case when `ignoreCase` is switched on
[info] - must return the lines that do not match the pattern when `invertMatch` is switched on
[info] Grep Command in piped mode
[info] - must fail if no match is found
[info] - must return the lines that match the pattern without ignoring case
[info] - must return the lines that match the pattern ignoring case when `ignoreCase` is switched on
[info] - must return the lines that do not match the pattern when `invertMatch` is switched on
[info] CatTest:
[info] Cat Command with default options
[info] - must return an empty string when reading an empty file
[info] - must return the file contents when reading an individual file
[info] - must return concatenated file contents when multiple files are specified, in the same order
[info] Cat Command with `showEnds`
[info] - must return the file contents with all 'LF' characters prepended with '$'
[info] Cat Command with `showTabs`
[info] - must return the file contents with all 'TAB' characters replaced with '^I'
[info] Cat Command with `showAll`
[info] - must return the file contents with all 'LF' and 'TAB' characters replaced with '$' and '^I' respectively
[info] DoTest:
[info] - A Do() must return the same command that was passed to it
[info] EchoTest:
[info] Echo Command with default parameters (trailingNewLine=true, backslashInterpretation=false)
[info] - must output the same string terminated with a new-line
[info] - must output the same string without escaping backslashes
[info] Echo Command with `addTrailingNewLine` = false
[info] - must output the same string without a terminal new-line !!! IGNORED !!!
[info] Echo Command with `interpretBackslashEscapes` = true
[info] - must output the same string after escaping backslashes
[info] CdTest:
[info] Cd Command
[info] - must switch to home directory when home option is specified in builder
[info] - must switch to home directory when `path` is empty
[info] - must switch to home directory when `path` is '~'
[info] - must stay at present directory when `path` is '.'
[info] - must switch to root directory when `path` is '/'
[info] Run completed in 1 second, 766 milliseconds.
[info] Total number of tests run: 70
[info] Suites: completed 10, aborted 0
[info] Tests: succeeded 70, failed 0, canceled 0, ignored 1, pending 0
[info] All tests passed.
[success] Total time: 9 s, completed Nov 29, 2019, 6:49:30 PM

```