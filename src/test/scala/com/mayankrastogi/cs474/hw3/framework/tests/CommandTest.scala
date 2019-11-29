package com.mayankrastogi.cs474.hw3.framework.tests

import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.Command.PipeReceiver
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers._
import com.mayankrastogi.cs474.hw3.utils.StringExtensions._
import org.scalatest.{Matchers, WordSpecLike}

class CommandTest extends WordSpecLike with Matchers {

  val oneCommand: Command[Unit] = Command[Unit]("1")
  val twoCommands: Command[Unit] = oneCommand.andThen(Command[Unit]("2"))
  val threeCommands: Command[Unit] = twoCommands.andThen(Command[Unit]("3"))
  val fiveCommands: Command[Unit] = twoCommands.andThen(threeCommands)

  "A Command" must {
    "use Windows Subsystem for Linux (WSL) if running on Windows" in {
      if (Command.isWindows) {
        oneCommand.toString should startWith("wsl bash -c \"")
      }
      else {
        oneCommand.toString should startWith("bash -c \"")
      }
    }

    "have no fragments if it's the only command in chain" in {
      oneCommand.fragments shouldBe empty
    }

    "put the result of execution in `Right` if executed successfully" in {
      Command[String]("whoami").execute shouldBe a[Right[_, _]]
    }

    "put the exception details in `Left` if execution fails" in {
      Command[String]("thisIsCertainlyNotAValidCommand").execute shouldBe a[Left[_, _]]
    }
  }

  "A Command, when combined using `andThen`" must {

    "contain the last command in `cmd` and all prior commands in `fragments`" in {
      twoCommands.cmd shouldBe "2"
      twoCommands.fragments.map(_.cmd) should contain only "1"

      threeCommands.cmd shouldBe "3"
      threeCommands.fragments.map(_.cmd) should contain inOrderOnly("1", "2")

      fiveCommands.cmd shouldBe "3"
      fiveCommands.fragments.map(_.cmd) should contain theSameElementsInOrderAs Seq("1", "2", "1", "2")
    }

    "have at least (n - 1) `&&` operators for n commands" in {
      oneCommand.toString.occurrencesOf("&&") should be >= 0
      twoCommands.toString.occurrencesOf("&&") should be >= 1
      threeCommands.toString.occurrencesOf("&&") should be >= 2
      fiveCommands.toString.occurrencesOf("&&") should be >= 4
    }

    "be in the same order as they were combined" in {
      twoCommands.toString.indexOf("1") should be < twoCommands.toString.indexOf("2")

      threeCommands.toString.indexOf("1") should be < threeCommands.toString.indexOf("2")
      threeCommands.toString.indexOf("2") should be < threeCommands.toString.indexOf("3")

      fiveCommands.toString.indexOf("1") should be < fiveCommands.toString.indexOf("2")
      fiveCommands.toString.indexOf("2") should be < fiveCommands.toString.lastIndexOf("1")
      fiveCommands.toString.lastIndexOf("1") should be < fiveCommands.toString.lastIndexOf("2")
      fiveCommands.toString.lastIndexOf("2") should be < fiveCommands.toString.lastIndexOf("3")
    }

    "suppress output from all commands but the last, unless it redirects its output" in {
      threeCommands.toString.occurrencesOf("> /dev/null") shouldBe 2

      val redirectingCommand = Command[Unit]("redir > testFile", redirectsOutput = true)
      val commandWithPriorRedirectingCommand = oneCommand.andThen(redirectingCommand).andThen(twoCommands)

      commandWithPriorRedirectingCommand.toString.occurrencesOf("> /dev/null") shouldBe 2
    }
  }

  "A Command, when combined using `pipeTo`" must {
    val pipedCommand = threeCommands.pipeTo(twoCommands.copy[PipeReceiver]())

    "keep all `fragments` from the lhs command and discard all `fragments` from the rhs command" in {
      pipedCommand.fragments.map(_.cmd) should contain inOrderOnly("1", "2")
    }

    "have the `cmd` of the first command piped with the `cmd` of the second command" in {
      pipedCommand.cmd shouldBe "3 | 2"
      pipedCommand.toString should include("1 > /dev/null && 2 > /dev/null && 3 | 2")
    }
  }

  "A Command, when combined using `writeTo`" must {
    val writeToCommand = threeCommands.writeTo("testFile")

    "set the `redirectsOutput` flag to `true`" in {
      writeToCommand.redirectsOutput shouldBe true
    }

    "preserve the `fragments` of the original command" in {
      writeToCommand.fragments should contain theSameElementsInOrderAs threeCommands.fragments
    }

    "add a `>` operator to the `cmd` of the command followed by the file path in single-quotes" in {
      writeToCommand.cmd shouldBe "3 > 'testFile'"
      writeToCommand.toString should include("1 > /dev/null && 2 > /dev/null && 3 > 'testFile'")
    }
  }

  "A Command, when combined using `appendTo`" must {
    val appendToCommand = threeCommands.appendTo("testFile")

    "set the `redirectsOutput` flag to `true`" in {
      appendToCommand.redirectsOutput shouldBe true
    }

    "preserve the `fragments` of the original command" in {
      appendToCommand.fragments should contain theSameElementsInOrderAs threeCommands.fragments
    }

    "add a `>>` operator to the `cmd` of the command followed by the file path in single-quotes" in {
      appendToCommand.cmd shouldBe "3 >> 'testFile'"
      appendToCommand.toString should include("1 > /dev/null && 2 > /dev/null && 3 >> 'testFile'")
    }
  }
}
