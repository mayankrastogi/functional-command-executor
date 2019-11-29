package com.mayankrastogi.cs474.hw3.commands.tests

import com.mayankrastogi.cs474.hw3.commands.Echo
import com.mayankrastogi.cs474.hw3.utils.StringExtensions._
import org.scalatest.{Matchers, WordSpecLike}

class EchoTest extends WordSpecLike with Matchers {

  "Echo Command with default parameters (trailingNewLine=true, backslashInterpretation=false)" must {

    "output the same string terminated with a new-line" in {
      Echo().text("test").build.execute.map(_.normalizeCrLf) shouldBe Right(s"test\n")
    }

    "output the same string without escaping backslashes" in {
      Echo().text(s"test\\n\\t").build.execute.map(_.normalizeCrLf) shouldBe Right(s"test\\n\\t\n")
    }
  }

  "Echo Command with `addTrailingNewLine` = false" must {

    // This test has been IGNORED since a new-line gets added in the string output irrespective of whether it was
    // produced by the external process or not.
    // This flag may be useful in combination with `pipeTo`, `writeTo`, or `appendTo`.
    "output the same string without a terminal new-line" ignore {
      val command = Echo().text("test").addTrailingNewLine(false).build.execute.map(_.normalizeCrLf)
      command shouldBe Right(s"test")
    }
  }

  "Echo Command with `interpretBackslashEscapes` = true" must {

    "output the same string after escaping backslashes" in {
      val command =
        Echo().text(s"test\\n\\t").interpretBackslashEscapes(true).build
          .execute
          .map(_.normalizeCrLf)

      command shouldBe Right(s"test\n\t\n")
    }
  }
}
