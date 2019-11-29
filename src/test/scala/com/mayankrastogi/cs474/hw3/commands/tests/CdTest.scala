package com.mayankrastogi.cs474.hw3.commands.tests

import com.mayankrastogi.cs474.hw3.commands.{Cd, Echo}
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers.stringParser
import org.scalatest.{Matchers, WordSpecLike}

class CdTest extends WordSpecLike with Matchers {

  val homeDirectory: Either[Throwable, String] = Echo().text("'$HOME'").build.execute
  val pwdCommand: Command[String] = Command("pwd")

  "Cd Command" must {
    "switch to home directory when home option is specified in builder" in {
      Cd().home.build.andThen(pwdCommand).execute shouldBe homeDirectory
    }

    "switch to home directory when `path` is empty" in {
      Cd().path("").build.andThen(pwdCommand).execute shouldBe homeDirectory
    }

    "switch to home directory when `path` is '~'" in {
      Cd().path("~").build.andThen(pwdCommand).execute shouldBe homeDirectory
    }

    "stay at present directory when `path` is '.'" in {
      val pwd = pwdCommand.execute
      Cd().path(".").build.andThen(pwdCommand).execute shouldBe pwd
    }

    "switch to root directory when `path` is '/'" in {
      Cd().path("/").build.andThen(pwdCommand).execute.map(_.strip) shouldBe Right("/")
    }
  }
}
